package docs;

import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    /** The first sentence of the Javadoc description as one plain line, or an empty string. */
    static String summary(Optional<Javadoc> javadoc) {
        var text = javadoc.map(j -> description(j.getDescription())).orElse("");
        if (text.isEmpty()) {
            return "";
        }
        var line = oneLine(text.split("\\n\\s*\\n", 2)[0]);
        var m = FIRST_SENTENCE.matcher(line);
        var sentence = m.find() ? m.group(1) : line;
        return MARKDOWN_LINK.matcher(sentence).replaceAll("$1").replace("\\<", "<").replace("**", "");
    }

    /** The whole Javadoc description on one line, with {@code @apiNote} appended, or an empty string. */
    static String line(Optional<Javadoc> javadoc) {
        if (javadoc.isEmpty()) {
            return "";
        }
        var sb = new StringBuilder(oneLine(description(javadoc.get().getDescription())));
        for (var tag : javadoc.get().getBlockTags()) {
            if (isNote(tag)) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append("Note: ").append(oneLine(description(tag.getContent())));
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
        return oneLine(description(tag.getContent()));
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

    /** Javadoc description elements as Markdown: inline tags become code spans, HTML becomes Markdown. */
    static String description(JavadocDescription description) {
        var sb = new StringBuilder();
        for (var element : description.getElements()) {
            if (element instanceof JavadocInlineTag tag) {
                sb.append(inlineTag(tag));
            } else {
                sb.append(html(element.toText()));
            }
        }
        return tidy(sb.toString());
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

    /** The HTML subset Javadoc authors use, as Markdown; anything tag-like that remains is escaped. */
    static String html(String text) {
        var s = text.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#64;", "@")
                .replace("&nbsp;", " ").replace("&amp;", "&");
        s = A_TAG.matcher(s).replaceAll(m -> Matcher.quoteReplacement(link(oneLine(m.group(2)), m.group(1))));
        s = CODE_TAG.matcher(s).replaceAll(m -> Matcher.quoteReplacement(code(oneLine(m.group(2)))));
        s = s.replaceAll("(?i)<pre>\\s*", "\n\n```\n").replaceAll("(?i)\\s*</pre>", "\n```\n\n");
        s = s.replaceAll("(?i)</?p\\s*/?>", "\n\n");
        s = s.replaceAll("(?i)<br\\s*/?>", "\n");
        s = s.replaceAll("(?i)\\s*<li>\\s*", "\n- ").replaceAll("(?i)</li>\\s*", "\n");
        s = s.replaceAll("(?i)\\s*</?(ul|ol)>\\s*", "\n\n");
        s = s.replaceAll("(?i)</?(b|strong)>", "**").replaceAll("(?i)</?(i|em)>", "*");
        s = s.replaceAll("(?i)<h[1-6]>", "\n\n**").replaceAll("(?i)</h[1-6]>", "**\n\n");
        return escape(s);
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
