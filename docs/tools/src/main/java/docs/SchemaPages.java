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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Renders the protobuf schema reference: {@code index.md} at the root, {@code <file stem>/index.md} per
 * {@code .proto} file and {@code <file stem>/<slug>.md} per message and enum, nested ones as
 * {@code <outer>-<inner>}. Field types link to the page of the message or enum they name, across files.
 */
final class SchemaPages {

    static final String URL_ROOT = "/schema/";
    private static final Set<String> SCALARS = Set.of("double", "float", "int32", "int64", "uint32", "uint64", "sint32",
            "sint64", "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string", "bytes");

    private SchemaPages() {}

    /** A message or enum with the file it comes from and its qualified name ({@code Outer.Inner} when nested). */
    record Entry(ProtoFile file, String stem, String qualified, String parent, ProtoMessage message, ProtoEnum enumeration) {

        boolean isMessage() {
            return message != null;
        }

        String kind() {
            return isMessage() ? "message" : "enum";
        }

        String url() {
            return URL_ROOT + stem + "/" + slug(qualified) + "/";
        }

        /** The generated Java class, {@code pkg.Outer.Inner} when nested. */
        String javaName() {
            return file.javaPackage().isEmpty() ? qualified : file.javaPackage() + "." + qualified;
        }
    }

    /**
     * The output directory for a file: its name without the extension, so two files whose packages share a last
     * segment keep separate directories; a file without a name falls back to the last segment of its
     * {@code java_package} (or {@code package}).
     */
    static String fileStem(ProtoFile file) {
        if (!file.name().isEmpty()) {
            return file.name();
        }
        var pkg = file.javaPackage().isEmpty() ? file.pkg() : file.javaPackage();
        return pkg.substring(pkg.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    /** {@code Outer.Inner} to {@code outer-inner}. */
    static String slug(String qualifiedName) {
        return qualifiedName.toLowerCase(Locale.ROOT).replace('.', '-');
    }

    /** Root-relative page URLs keyed by generated Java class name, for linking API pages to the schema. */
    static Map<String, String> javaLinks(List<ProtoFile> files) {
        var links = new TreeMap<String, String>();
        for (var entry : entries(files)) {
            links.put(entry.javaName(), entry.url());
        }
        return links;
    }

    /** Renders every page and returns their paths, the root index first. */
    static List<Path> render(List<ProtoFile> files, String version, Path outDir) {
        var entries = entries(files);
        var index = new TreeMap<String, Entry>();
        for (var entry : entries) {
            if (index.put(entry.qualified(), entry) != null) {
                throw new IllegalStateException("duplicate type " + entry.qualified() + " across proto files");
            }
        }
        var byFile = new LinkedHashMap<String, List<Entry>>();
        for (var entry : entries) {
            byFile.computeIfAbsent(entry.stem(), k -> new ArrayList<>()).add(entry);
        }
        var written = new ArrayList<Path>();
        try {
            for (var fileEntries : byFile.values()) {
                var stem = fileEntries.get(0).stem();
                written.add(Markdown.write(outDir, stem + "/index.md", filePage(fileEntries)));
                for (var entry : fileEntries) {
                    written.add(Markdown.write(outDir, stem + "/" + slug(entry.qualified()) + ".md", entry.isMessage() ? messagePage(entry, index) : enumPage(entry)));
                }
            }
            written.add(0, Markdown.write(outDir, "index.md", rootPage(version, byFile.keySet(), entries)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return written;
    }

    // --- Model ------------------------------------------------------------------------------

    /** Every message and enum of every file, files by stem, entries by qualified name. */
    private static List<Entry> entries(List<ProtoFile> files) {
        var sortedFiles = new ArrayList<>(files);
        sortedFiles.sort(Comparator.comparing(SchemaPages::fileStem));
        var entries = new ArrayList<Entry>();
        for (var file : sortedFiles) {
            var stem = fileStem(file);
            var fileEntries = new ArrayList<Entry>();
            for (var message : file.messages()) {
                collect(file, stem, message, fileEntries);
            }
            for (var enumeration : file.enums()) {
                fileEntries.add(new Entry(file, stem, enumeration.name(), null, null, enumeration));
            }
            fileEntries.sort(Comparator.comparing(Entry::qualified));
            entries.addAll(fileEntries);
        }
        return entries;
    }

    private static void collect(ProtoFile file, String stem, ProtoMessage message, List<Entry> out) {
        var qualified = message.qualifiedName();
        out.add(new Entry(file, stem, qualified, message.parent(), message, null));
        for (var nested : message.nested()) {
            collect(file, stem, nested, out);
        }
        for (var nestedEnum : message.nestedEnums()) {
            out.add(new Entry(file, stem, qualified + "." + nestedEnum.name(), qualified, null, nestedEnum));
        }
    }

    /** The entry a type reference names from inside {@code scope}, searching enclosing messages first. */
    private static Entry resolve(String type, Entry scope, Map<String, Entry> index) {
        var name = type.startsWith(".") ? type.substring(1) : type;
        var pkg = scope.file().pkg();
        if (!pkg.isEmpty() && name.startsWith(pkg + ".")) {
            name = name.substring(pkg.length() + 1);
        }
        for (var s = scope.qualified(); s != null; s = parentOf(s)) {
            var candidate = index.get(s + "." + name);
            if (candidate != null) {
                return candidate;
            }
        }
        return index.get(name);
    }

    private static String parentOf(String qualified) {
        int dot = qualified.lastIndexOf('.');
        return dot < 0 ? null : qualified.substring(0, dot);
    }

    private static String typeMarkdown(String type, Entry scope, Map<String, Entry> index) {
        if (type.startsWith("map<") && type.endsWith(">")) {
            var parts = type.substring(4, type.length() - 1).split(",\\s*", 2);
            var value = resolve(parts[1], scope, index);
            if (value == null) {
                return Markdown.code(type);
            }
            return Markdown.code("map<" + parts[0] + ", ") + Markdown.link(Markdown.code(parts[1]), value.url()) + Markdown.code(">");
        }
        if (SCALARS.contains(type)) {
            return Markdown.code(type);
        }
        var target = resolve(type, scope, index);
        return target == null ? Markdown.code(type) : Markdown.link(Markdown.code(type), target.url());
    }

    // --- Pages ------------------------------------------------------------------------------

    private static String rootPage(String version, Set<String> stems, List<Entry> entries) {
        var blurb = "Every protobuf message and enum in the schema TikTokLiveJava " + version
                + " decodes events from, generated from the `.proto` files in `API/src/main/proto`.";
        var sb = new StringBuilder(Markdown.frontmatter("TikTok Webcast schema (" + version + ")", blurb, "Overview", 0));
        sb.append(blurb).append("\n\n## Files\n\n");
        for (var stem : stems) {
            sb.append("- ").append(Markdown.link(Markdown.code(stem + ".proto"), URL_ROOT + stem + "/")).append('\n');
        }
        sb.append('\n');
        var byName = new ArrayList<>(entries);
        byName.sort(Comparator.comparing(Entry::qualified).thenComparing(Entry::stem));
        sb.append(listing("## Messages", byName, true, true));
        sb.append(listing("## Enums", byName, false, true));
        return sb.toString().stripTrailing() + "\n";
    }

    private static String filePage(List<Entry> entries) {
        var first = entries.get(0);
        var file = first.file();
        var title = first.stem() + ".proto";
        var blurb = "Messages and enums declared in `" + title + "` (package `" + file.pkg() + "`, Java package `" + file.javaPackage() + "`).";
        var sb = new StringBuilder(Markdown.frontmatter(title, "Protobuf definitions in " + title, first.stem(), 0));
        sb.append(blurb).append("\n\n");
        sb.append(listing("## Messages", entries, true, false));
        sb.append(listing("## Enums", entries, false, false));
        return sb.toString().stripTrailing() + "\n";
    }

    private static String listing(String heading, List<Entry> entries, boolean messages, boolean qualified) {
        var lines = new ArrayList<String>();
        for (var entry : entries) {
            if (entry.isMessage() != messages) {
                continue;
            }
            var line = "- " + Markdown.link(Markdown.code(entry.qualified()), entry.url());
            if (qualified) {
                line += " (" + Markdown.code(entry.stem() + ".proto") + ")";
            }
            lines.add(line);
        }
        return lines.isEmpty() ? "" : heading + "\n\n" + String.join("\n", lines) + "\n\n";
    }

    private static String header(Entry entry, Map<String, Entry> index) {
        var kind = entry.kind();
        var sb = new StringBuilder(Markdown.frontmatter(entry.qualified(), "Protobuf " + kind + " " + entry.qualified() + " (" + entry.stem() + ".proto)", entry.qualified(), null));
        if (entry.parent() != null) {
            var parent = index == null ? null : index.get(entry.parent());
            sb.append("Nested in ").append(parent == null ? Markdown.code(entry.parent()) : Markdown.link(Markdown.code(entry.parent()), parent.url())).append(".\n\n");
        }
        sb.append("Protobuf ").append(kind).append(' ').append(Markdown.code(entry.qualified())).append(" declared in ")
                .append(Markdown.link(Markdown.code(entry.stem() + ".proto"), URL_ROOT + entry.stem() + "/"))
                .append("; generated Java ").append(kind.equals("enum") ? "enum" : "class").append(' ').append(Markdown.code(entry.javaName())).append(".\n\n");
        return sb.toString();
    }

    private static String messagePage(Entry entry, Map<String, Entry> index) {
        var sb = new StringBuilder(header(entry, index));
        var fields = entry.message().fields();
        sb.append("## Fields\n\n");
        if (fields.isEmpty()) {
            sb.append("This message has no fields.\n\n");
        } else {
            sb.append("| Field | Type | Number | Label |\n| --- | --- | --- | --- |\n");
            for (var field : fields) {
                sb.append("| ").append(Markdown.code(field.name())).append(" | ").append(typeMarkdown(field.type(), entry, index))
                        .append(" | ").append(field.number()).append(" | ").append(field.label()).append(" |\n");
            }
            sb.append('\n');
        }
        var nested = new ArrayList<Entry>();
        for (var child : entry.message().nested()) {
            nested.add(index.get(child.qualifiedName()));
        }
        for (var child : entry.message().nestedEnums()) {
            nested.add(index.get(entry.qualified() + "." + child.name()));
        }
        nested.sort(Comparator.comparing(Entry::qualified));
        if (!nested.isEmpty()) {
            sb.append("## Nested types\n\n");
            for (var child : nested) {
                sb.append("- ").append(Markdown.link(Markdown.code(child.qualified()), child.url())).append(" (").append(child.kind()).append(")\n");
            }
            sb.append('\n');
        }
        return sb.toString().stripTrailing() + "\n";
    }

    private static String enumPage(Entry entry) {
        var sb = new StringBuilder(header(entry, null));
        sb.append("## Values\n\n| Name | Number |\n| --- | --- |\n");
        for (var value : entry.enumeration().values()) {
            sb.append("| ").append(Markdown.code(value.name())).append(" | ").append(value.number()).append(" |\n");
        }
        return sb.toString().stripTrailing() + "\n";
    }
}
