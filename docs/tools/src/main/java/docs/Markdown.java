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

import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Markdown, YAML and Javadoc-to-Markdown helpers shared by the page generators. */
final class Markdown {

    private static final Pattern PLAIN_YAML = Pattern.compile("[A-Za-z_][A-Za-z0-9_ .()\\-]*[A-Za-z0-9_.)]");
    private static final Set<String> YAML_RESERVED = Set.of("true", "false", "null", "yes", "no", "on", "off");
    /** A {@code <} that Markdown would read as the start of an HTML tag. */
    private static final Pattern TAG_LIKE = Pattern.compile("<(?=[A-Za-z/])");
    private static final Pattern CODE_SPAN = Pattern.compile("(`[^`]*`)");
    private static final Pattern FIRST_SENTENCE = Pattern.compile("^(.*?[.!?])(?:\\s|$)");
    private static final Pattern A_TAG = Pattern.compile("(?is)<a\\s+[^>]*href=\"([^\"]*)\"[^>]*>(.*?)</a>");
    private static final Pattern CODE_TAG = Pattern.compile("(?is)<(code|tt)>(.*?)</\\1>");
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[([^\\]]*)\\]\\([^)]*\\)");
    /** A {@code <pre>} or {@code </pre>} tag; group 1 is the slash of a closing tag. */
    private static final Pattern PRE_TAG = Pattern.compile("(?i)<(/?)pre>");
    /** Stands in for a {@code <pre>} block while the surrounding text is escaped and tidied; private-use characters survive {@code trim()}. */
    private static final String PRE_MARK = "\uE000PRE";

    private Markdown() {}

    // --- YAML and Markdown blocks ---------------------------------------------------------

    /** Starlight frontmatter: title, description, sidebar label and an optional sidebar order. */
    static String frontmatter(String title, String description, String label, Integer order) {
        var sb = new StringBuilder("---\n");
        sb.append("title: ").append(yamlScalar(title)).append('\n');
        sb.append("description: ").append(jsonString(description)).append('\n');
        sb.append("sidebar:\n  label: ").append(yamlScalar(label)).append('\n');
        if (order != null) {
            sb.append("  order: ").append(order).append('\n');
        }
        return sb.append("---\n\n").toString();
    }

    /** A plain YAML scalar when it is unambiguous, else a double-quoted one. */
    static String yamlScalar(String value) {
        if (PLAIN_YAML.matcher(value).matches() && !YAML_RESERVED.contains(value.toLowerCase(Locale.ROOT))) {
            return value;
        }
        return jsonString(value);
    }

    /** A JSON string literal, which is also a valid double-quoted YAML scalar. */
    static String jsonString(String value) {
        var sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    static String codeBlock(String language, String source) {
        return "```" + language + "\n" + source + "\n```\n\n";
    }

    /** An inline code span; text containing a backtick gets a double-backtick span. */
    static String code(String text) {
        return text.contains("`") ? "`` " + text + " ``" : "`" + text + "`";
    }

    static String link(String text, String url) {
        return "[" + text + "](" + url + ")";
    }

    /** Text for a Markdown table cell: no pipes, no line breaks. */
    static String cell(String text) {
        return oneLine(text).replace("|", "\\|");
    }

    static String oneLine(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    /** Escapes tag-like {@code <} outside code spans so generics in prose survive the Markdown renderer. */
    static String escape(String text) {
        var sb = new StringBuilder();
        var m = CODE_SPAN.matcher(text);
        int last = 0;
        while (m.find()) {
            sb.append(TAG_LIKE.matcher(text.substring(last, m.start())).replaceAll("\\\\<")).append(m.group(1));
            last = m.end();
        }
        return sb.append(TAG_LIKE.matcher(text.substring(last)).replaceAll("\\\\<")).toString();
    }

    // --- Javadoc ----------------------------------------------------------------------------

    /**
     * The first sentence of the Javadoc description as one Markdown line, or an empty string. Links are reduced to
     * their text and emphasis is dropped; tag-like {@code <} stays escaped, so the line can be written into a
     * listing as it is. {@link #plain} removes the escapes for a quoted frontmatter value.
     */
    static String summary(Optional<Javadoc> javadoc) {
        var text = javadoc.map(j -> description(j.getDescription(), false)).orElse("");
        if (text.isEmpty()) {
            return "";
        }
        var line = oneLine(text.split("\\n\\s*\\n", 2)[0]);
        var m = FIRST_SENTENCE.matcher(line);
        var sentence = m.find() ? m.group(1) : line;
        return MARKDOWN_LINK.matcher(sentence).replaceAll("$1").replace("**", "");
    }

    /** A {@link #summary} with its Markdown escapes removed, for a value {@link #jsonString} quotes. */
    static String plain(String summary) {
        return summary.replace("\\<", "<");
    }

    /**
     * The whole Javadoc description on one line, with {@code @apiNote} appended, or an empty string. A
     * {@code <pre>} block becomes a code span, since a fence cannot sit on a bullet.
     */
    static String line(Optional<Javadoc> javadoc) {
        if (javadoc.isEmpty()) {
            return "";
        }
        var sb = new StringBuilder(oneLine(description(javadoc.get().getDescription(), false)));
        for (var tag : javadoc.get().getBlockTags()) {
            if (isNote(tag)) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append("Note: ").append(oneLine(description(tag.getContent(), false)));
            }
        }
        return sb.toString();
    }

    /** The Javadoc description as Markdown paragraphs, followed by note, deprecation and see-also paragraphs. */
    static String body(Optional<Javadoc> javadoc) {
        if (javadoc.isEmpty()) {
            return "";
        }
        var sb = new StringBuilder(description(javadoc.get().getDescription()));
        for (var tag : javadoc.get().getBlockTags()) {
            var content = description(tag.getContent());
            if (isNote(tag)) {
                paragraph(sb, "**Note:** " + content);
            } else if (tag.getType() == JavadocBlockTag.Type.DEPRECATED) {
                paragraph(sb, "**Deprecated.** " + content);
            } else if (tag.getType() == JavadocBlockTag.Type.SEE && !content.isEmpty()) {
                paragraph(sb, "See also: " + (content.contains("`") || content.contains("[") ? content : code(oneLine(content))));
            }
        }
        return sb.toString().trim();
    }

    /** The text of a block tag ({@code @param}, {@code @return}, {@code @throws}) as one Markdown line. */
    static String tagText(JavadocBlockTag tag) {
        return oneLine(description(tag.getContent(), false));
    }

    private static boolean isNote(JavadocBlockTag tag) {
        return tag.getType() == JavadocBlockTag.Type.UNKNOWN
                && Set.of("apiNote", "implNote", "implSpec").contains(tag.getTagName());
    }

    private static void paragraph(StringBuilder sb, String text) {
        if (sb.length() > 0) {
            sb.append("\n\n");
        }
        sb.append(text);
    }

    /**
     * Javadoc description elements as Markdown: inline tags become code spans, HTML becomes Markdown, and each
     * {@code <pre>} block becomes a fenced block holding its text raw.
     */
    static String description(JavadocDescription description) {
        return description(description, true);
    }

    /**
     * As {@link #description(JavadocDescription)}; with {@code fences} false, for single-line contexts, each
     * {@code <pre>} block becomes a code span instead.
     */
    static String description(JavadocDescription description, boolean fences) {
        var sb = new StringBuilder();
        var blocks = new ArrayList<String>();
        PreBlock pre = null;
        for (var element : description.getElements()) {
            if (element instanceof JavadocInlineTag tag) {
                if (pre == null) {
                    sb.append(inlineTag(tag));
                } else {
                    pre.tag(tag);
                }
                continue;
            }
            var text = element.toText();
            var m = PRE_TAG.matcher(text);
            int last = 0;
            while (m.find()) {
                var chunk = text.substring(last, m.start());
                if (pre == null) {
                    sb.append(html(chunk));
                } else {
                    pre.text(chunk);
                }
                var closing = !m.group(1).isEmpty();
                if (closing && pre != null) {
                    blocks.add(pre.toString());
                    sb.append("\n\n").append(PRE_MARK).append(blocks.size() - 1).append("\uE001\n\n");
                    pre = null;
                } else if (!closing && pre == null) {
                    pre = new PreBlock();
                }
                last = m.end();
            }
            var tail = text.substring(last);
            if (pre == null) {
                sb.append(html(tail));
            } else {
                pre.text(tail);
            }
        }
        if (pre != null) {
            blocks.add(pre.toString());
            sb.append("\n\n").append(PRE_MARK).append(blocks.size() - 1).append('\uE001');
        }
        var out = tidy(sb.toString());
        for (int i = 0; i < blocks.size(); i++) {
            var block = preBlock(blocks.get(i));
            out = out.replace(PRE_MARK + i + '\uE001', fences ? "```\n" + block + "\n```" : code(oneLine(block)));
        }
        return out;
    }

    /**
     * The raw text of one {@code <pre>} block as it is read: snippets as they are, inline tags by their content.
     * JavaParser ends an inline tag at the first {@code }}, so a {@code {@code}} block containing braces arrives cut
     * short; the brace it consumed is restored and the tag's real closing brace, the first one that unbalances the
     * block, is dropped.
     */
    private static final class PreBlock {
        private final StringBuilder sb = new StringBuilder();
        /** Unmatched {@code {} of a cut-short {@code {@code}} tag still open, or -1 when none is pending. */
        private int openBraces = -1;

        void text(String text) {
            if (openBraces < 0) {
                sb.append(text);
                return;
            }
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '{') {
                    openBraces++;
                } else if (c == '}' && --openBraces < 0) {
                    sb.append(text, i + 1, text.length());
                    return;
                }
                sb.append(c);
            }
        }

        void tag(JavadocInlineTag tag) {
            var content = tag.getContent();
            switch (tag.getType()) {
                case LINK, LINKPLAIN -> {
                    var parts = content.trim().split("\\s+", 2);
                    sb.append(parts.length > 1 ? parts[1] : parts[0]);
                }
                case INHERIT_DOC -> {
                }
                default -> {
                    sb.append(content);
                    var open = content.chars().filter(c -> c == '{').count() - content.chars().filter(c -> c == '}').count();
                    if (open > 0) {
                        sb.append('}');
                        openBraces = (int) open - 1;
                    }
                }
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    /** A {@code <pre>} block's text for a fence: entities decoded, blank edges and common indentation removed, nothing escaped. */
    private static String preBlock(String raw) {
        var lines = new ArrayList<>(List.of(decode(raw).replace("\r", "").split("\n", -1)));
        while (!lines.isEmpty() && lines.get(0).isBlank()) {
            lines.remove(0);
        }
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
        var indent = Integer.MAX_VALUE;
        for (var line : lines) {
            if (!line.isBlank()) {
                indent = Math.min(indent, line.length() - line.stripLeading().length());
            }
        }
        var sb = new StringBuilder();
        for (var line : lines) {
            sb.append(line.isBlank() ? "" : line.substring(indent)).append('\n');
        }
        return sb.toString().stripTrailing();
    }

    private static String inlineTag(JavadocInlineTag tag) {
        var content = tag.getContent().trim();
        return switch (tag.getType()) {
            case CODE, VALUE -> code(oneLine(content));
            case LITERAL -> escape(content);
            case LINK, LINKPLAIN -> {
                var parts = content.split("\\s+", 2);
                var reference = parts[0].startsWith("#") ? parts[0].substring(1) : parts[0].replace('#', '.');
                yield code(parts.length > 1 ? parts[1] : reference);
            }
            case INHERIT_DOC -> "";
            default -> escape(content);
        };
    }

    /**
     * The HTML subset Javadoc authors use, as Markdown; anything tag-like that remains is escaped. {@code <pre>}
     * blocks are cut out before this runs, see {@link #description(JavadocDescription, boolean)}.
     */
    static String html(String text) {
        var s = decode(text);
        s = A_TAG.matcher(s).replaceAll(m -> Matcher.quoteReplacement(link(oneLine(m.group(2)), m.group(1))));
        s = CODE_TAG.matcher(s).replaceAll(m -> Matcher.quoteReplacement(code(oneLine(m.group(2)))));
        s = s.replaceAll("(?i)</?p\\s*/?>", "\n\n");
        s = s.replaceAll("(?i)<br\\s*/?>", "\n");
        s = s.replaceAll("(?i)\\s*<li>\\s*", "\n- ").replaceAll("(?i)</li>\\s*", "\n");
        s = s.replaceAll("(?i)\\s*</?(ul|ol)>\\s*", "\n\n");
        s = s.replaceAll("(?i)</?(b|strong)>", "**").replaceAll("(?i)</?(i|em)>", "*");
        s = s.replaceAll("(?i)<h[1-6]>", "\n\n**").replaceAll("(?i)</h[1-6]>", "**\n\n");
        return escape(s);
    }

    /** The HTML entities Javadoc authors use, decoded. */
    private static String decode(String text) {
        return text.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#64;", "@")
                .replace("&nbsp;", " ").replace("&amp;", "&");
    }

    /** Trims every line and collapses runs of blank lines. */
    private static String tidy(String text) {
        var lines = text.split("\n", -1);
        var sb = new StringBuilder();
        for (var line : lines) {
            sb.append(line.strip()).append('\n');
        }
        return sb.toString().replaceAll("\n{3,}", "\n\n").trim();
    }

    // --- Files ------------------------------------------------------------------------------

    /** Writes one page, refusing a {@code name.md} / {@code name/index.md} pair, which would share a URL. */
    static Path write(Path outDir, String relative, String content) throws IOException {
        var path = outDir.resolve(relative);
        var name = path.getFileName().toString();
        Path twin;
        if (name.equals("index.md")) {
            var dir = path.getParent();
            twin = dir.resolveSibling(dir.getFileName() + ".md");
        } else {
            twin = path.resolveSibling(name.substring(0, name.length() - ".md".length())).resolve("index.md");
        }
        if (Files.exists(path) || Files.exists(twin)) {
            throw new IllegalStateException(path + " would publish the same URL as an existing page");
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path;
    }
}
