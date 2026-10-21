/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package docs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A hand-written recursive-descent parser for the proto3 subset the library's schema uses: {@code message},
 * {@code enum}, nested types, {@code repeated}/{@code optional} labels, {@code map<K, V>}, {@code oneof}, and
 * both comment styles. {@code option}, {@code reserved}, {@code extensions}, {@code import} and {@code syntax}
 * statements are skipped (the {@code java_package} option is kept).
 */
final class ProtoParser {

    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_.][A-Za-z0-9_.]*");

    private record Token(String text, int line) {}

    private final List<Token> tokens;
    private final String origin;
    private int pos;

    private ProtoParser(List<Token> tokens, String origin) {
        this.tokens = tokens;
        this.origin = origin;
    }

    static ProtoFile parse(Path protoFile) {
        String source;
        try {
            source = Files.readString(protoFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return parse(source, protoFile.getFileName().toString());
    }

    /** Parses proto source text; {@code origin}, a file name or any label, names it in error messages and gives the file its stem. */
    static ProtoFile parse(String source, String origin) {
        return new ProtoParser(lex(source, origin), origin).file();
    }

    /** {@code webcast} for {@code API/src/main/proto/webcast.proto} or {@code webcast.proto}. */
    static String stem(String origin) {
        var name = origin.substring(Math.max(origin.lastIndexOf('/'), origin.lastIndexOf('\\')) + 1);
        if (name.endsWith(".proto")) {
            name = name.substring(0, name.length() - ".proto".length());
        }
        return name.toLowerCase(java.util.Locale.ROOT);
    }

    // --- Grammar ----------------------------------------------------------------------------

    private ProtoFile file() {
        var pkg = "";
        var javaPackage = "";
        var messages = new ArrayList<ProtoMessage>();
        var enums = new ArrayList<ProtoEnum>();
        while (!eof()) {
            var t = peek().text();
            switch (t) {
                case "syntax", "import" -> skipStatement();
                case "package" -> {
                    next();
                    pkg = identifier();
                    expect(";");
                }
                case "option" -> {
                    next();
                    var name = identifier();
                    expect("=");
                    var value = peekIs("{") ? "" : next().text();
                    if (name.equals("java_package")) {
                        javaPackage = unquote(value);
                    }
                    skipStatement();
                }
                case "message" -> messages.add(message(null));
                case "enum" -> enums.add(enumeration());
                case ";" -> next();
                default -> throw error("unexpected '" + t + "' at top level");
            }
        }
        return new ProtoFile(stem(origin), pkg, javaPackage, List.copyOf(messages), List.copyOf(enums));
    }

    private ProtoMessage message(String parent) {
        expect("message");
        var name = identifier();
        var qualified = parent == null ? name : parent + "." + name;
        expect("{");
        var fields = new ArrayList<ProtoField>();
        var nested = new ArrayList<ProtoMessage>();
        var nestedEnums = new ArrayList<ProtoEnum>();
        while (!peekIs("}")) {
            var t = peek().text();
            switch (t) {
                case "message" -> nested.add(message(qualified));
                case "enum" -> nestedEnums.add(enumeration());
                case "oneof" -> {
                    next();
                    identifier();
                    expect("{");
                    while (!peekIs("}")) {
                        if (peekIs("option")) {
                            skipStatement();
                        } else if (peekIs(";")) {
                            next();
                        } else {
                            fields.add(field("oneof"));
                        }
                    }
                    expect("}");
                }
                case "option", "reserved", "extensions", "extend" -> skipStatement();
                case ";" -> next();
                case "map" -> fields.add(mapField());
                case "repeated", "optional", "required" -> {
                    next();
                    fields.add(field(t));
                }
                default -> fields.add(field(""));
            }
        }
        expect("}");
        return new ProtoMessage(name, parent, List.copyOf(fields), List.copyOf(nested), List.copyOf(nestedEnums));
    }

    private ProtoField field(String label) {
        var type = identifier();
        var name = identifier();
        expect("=");
        var number = number();
        options();
        expect(";");
        return new ProtoField(label, type, name, number);
    }

    private ProtoField mapField() {
        expect("map");
        expect("<");
        var key = identifier();
        expect(",");
        var value = identifier();
        expect(">");
        var name = identifier();
        expect("=");
        var number = number();
        options();
        expect(";");
        return new ProtoField("map", "map<" + key + ", " + value + ">", name, number);
    }

    private ProtoEnum enumeration() {
        expect("enum");
        var name = identifier();
        expect("{");
        var values = new ArrayList<ProtoEnumValue>();
        while (!peekIs("}")) {
            if (peekIs("option") || peekIs("reserved")) {
                skipStatement();
            } else if (peekIs(";")) {
                next();
            } else {
                var valueName = identifier();
                expect("=");
                var number = number();
                options();
                expect(";");
                values.add(new ProtoEnumValue(valueName, number));
            }
        }
        expect("}");
        return new ProtoEnum(name, List.copyOf(values));
    }

    /** Skips a {@code [ ... ]} option list when one follows. */
    private void options() {
        if (!peekIs("[")) {
            return;
        }
        int depth = 0;
        do {
            var t = next().text();
            if (t.equals("[")) {
                depth++;
            } else if (t.equals("]")) {
                depth--;
            }
        } while (depth > 0 && !eof());
        if (depth != 0) {
            throw error("unterminated field options");
        }
    }

    /** Skips through the next top-level {@code ;}, stepping over any braced aggregate on the way. */
    private void skipStatement() {
        int depth = 0;
        while (!eof()) {
            var t = next().text();
            if (t.equals("{")) {
                depth++;
            } else if (t.equals("}")) {
                depth--;
            } else if (t.equals(";") && depth == 0) {
                return;
            }
        }
        throw error("unterminated statement");
    }

    // --- Tokens -----------------------------------------------------------------------------

    private String identifier() {
        var t = next();
        if (!IDENTIFIER.matcher(t.text()).matches()) {
            throw error("expected an identifier, found '" + t.text() + "'", t);
        }
        return t.text();
    }

    private int number() {
        var t = next();
        try {
            return Integer.decode(t.text());
        } catch (NumberFormatException e) {
            throw error("expected a number, found '" + t.text() + "'", t);
        }
    }

    private void expect(String text) {
        var t = next();
        if (!t.text().equals(text)) {
            throw error("expected '" + text + "', found '" + t.text() + "'", t);
        }
    }

    private boolean eof() {
        return pos >= tokens.size();
    }

    private Token peek() {
        if (eof()) {
            throw error("unexpected end of file");
        }
        return tokens.get(pos);
    }

    private boolean peekIs(String text) {
        return peek().text().equals(text);
    }

    private Token next() {
        var t = peek();
        pos++;
        return t;
    }

    private IllegalArgumentException error(String message) {
        var line = eof() ? (tokens.isEmpty() ? 0 : tokens.get(tokens.size() - 1).line()) : tokens.get(pos).line();
        return new IllegalArgumentException(origin + ":" + line + ": " + message);
    }

    private IllegalArgumentException error(String message, Token at) {
        return new IllegalArgumentException(origin + ":" + at.line() + ": " + message);
    }

    private static String unquote(String literal) {
        if (literal.length() >= 2 && (literal.startsWith("\"") || literal.startsWith("'"))) {
            return literal.substring(1, literal.length() - 1);
        }
        return literal;
    }

    /** Splits source into identifiers, numbers, string literals and single-character punctuation; comments are dropped. */
    private static List<Token> lex(String source, String origin) {
        var tokens = new ArrayList<Token>();
        int i = 0;
        int line = 1;
        int n = source.length();
        while (i < n) {
            char c = source.charAt(i);
            if (c == '\n') {
                line++;
                i++;
            } else if (Character.isWhitespace(c)) {
                i++;
            } else if (source.startsWith("//", i)) {
                while (i < n && source.charAt(i) != '\n') {
                    i++;
                }
            } else if (source.startsWith("/*", i)) {
                int end = source.indexOf("*/", i + 2);
                if (end < 0) {
                    throw new IllegalArgumentException(origin + ":" + line + ": unterminated block comment");
                }
                for (int j = i; j < end; j++) {
                    if (source.charAt(j) == '\n') {
                        line++;
                    }
                }
                i = end + 2;
            } else if (c == '"' || c == '\'') {
                int j = i + 1;
                while (j < n && source.charAt(j) != c) {
                    j += source.charAt(j) == '\\' ? 2 : 1;
                }
                if (j >= n) {
                    throw new IllegalArgumentException(origin + ":" + line + ": unterminated string literal");
                }
                tokens.add(new Token(source.substring(i, j + 1), line));
                i = j + 1;
            } else if (Character.isLetter(c) || c == '_' || c == '.') {
                int j = i + 1;
                while (j < n && (Character.isLetterOrDigit(source.charAt(j)) || source.charAt(j) == '_' || source.charAt(j) == '.')) {
                    j++;
                }
                tokens.add(new Token(source.substring(i, j), line));
                i = j;
            } else if (Character.isDigit(c) || (c == '-' && i + 1 < n && Character.isDigit(source.charAt(i + 1)))) {
                int j = i + 1;
                while (j < n && (Character.isLetterOrDigit(source.charAt(j)) || source.charAt(j) == '.')) {
                    j++;
                }
                tokens.add(new Token(source.substring(i, j), line));
                i = j;
            } else {
                tokens.add(new Token(String.valueOf(c), line));
                i++;
            }
        }
        return tokens;
    }
}
