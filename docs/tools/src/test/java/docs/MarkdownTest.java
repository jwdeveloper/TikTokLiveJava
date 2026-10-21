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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Pins how Javadoc becomes Markdown: raw generics, {@code <pre>} blocks, links, pipes and {@code {@link}}. */
class MarkdownTest {

    /** A unit whose Javadoc carries each construct once; the library's own sources have none of the awkward ones. */
    static final String SOURCE = """
            package demo.pkg;

            import java.util.List;

            /**
             * Holds a List<Gift> per room, keyed by id. A second sentence with a pipe | and <b>bold</b>.
             * <p>
             * Example:
             * <pre>{@code
             * var holder = new Holder();
             * if (holder.size("a") < 2) {
             *     holder.gifts.add("x");
             * }
             * }</pre>
             * See <a href="https://example.com/docs">the docs</a> and {@link Holder#size(String)}.
             */
            public class Holder {
                /** The gifts, a List<String> of names | nothing else. <pre>{@code holder.gifts.get(0)}</pre> */
                public final List<String> gifts = List.of();

                /**
                 * Counts entries.
                 *
                 * @param filter a Predicate<Gift> text with a | pipe
                 * @return the count, a | b
                 */
                public int size(String filter) {
                    return 0;
                }

                /** A nested Map<K, V> holder. */
                public static class Nested {
                }
            }
            """;

    @Test
    void syntheticJavadocRendersGenericsPreLinksAndPipes(@TempDir Path out) throws IOException {
        StaticJavaParser.setConfiguration(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17));
        var unit = StaticJavaParser.parse(SOURCE);
        ApiPages.render(List.of(unit), "0.0", ApiPagesTest.SOURCE_BASE, out);

        var root = Files.readString(out.resolve("index.md"));
        assertTrue(root.contains("\n- [`Holder`](/api/demo/pkg/holder/) (`demo.pkg`) — Holds a List\\<Gift> per room, keyed by id.\n"),
                "root listing keeps the generic escaped:\n" + root);
        var pkg = Files.readString(out.resolve("demo/pkg/index.md"));
        assertTrue(pkg.contains("\n- [`Holder`](/api/demo/pkg/holder/) — Holds a List\\<Gift> per room, keyed by id.\n"),
                "package listing keeps the generic escaped:\n" + pkg);

        var page = Files.readString(out.resolve("demo/pkg/holder.md"));
        assertTrue(page.startsWith("---\ntitle: Holder\ndescription: \"Holds a List<Gift> per room, keyed by id.\"\n"),
                "description frontmatter is the plain sentence:\n" + page);
        assertTrue(page.contains("\nHolds a List\\<Gift> per room, keyed by id. A second sentence with a pipe | and **bold**.\n\nExample:\n\n"
                + "```\n"
                + "var holder = new Holder();\n"
                + "if (holder.size(\"a\") < 2) {\n"
                + "    holder.gifts.add(\"x\");\n"
                + "}\n"
                + "```\n\n"
                + "See [the docs](https://example.com/docs) and `Holder.size(String)`.\n"),
                "body: escaped generic, bold, raw fenced block with braces and indentation, link, {@link}:\n" + page);
        assertTrue(page.contains("\n- **`gifts`** (`List<String>`) — The gifts, a List\\<String> of names | nothing else. `holder.gifts.get(0)`\n"),
                "field bullet: generic escaped, pipe kept, <pre> as a code span:\n" + page);
        assertFalse(page.contains("```\n- ") || page.contains(" ```"), "no fence on a bullet:\n" + page);
        assertTrue(page.contains("\n- **`filter`** (`String`) — a Predicate\\<Gift> text with a | pipe\n"), "parameter bullet:\n" + page);
        assertTrue(page.contains("\n#### Returns\n\n`int` — the count, a | b\n"), "returns line:\n" + page);
        assertTrue(page.contains("\n- [`Holder.Nested`](/api/demo/pkg/holder-nested/) (class) — A nested Map\\<K, V> holder.\n"),
                "nested-type list keeps the generic escaped:\n" + page);

        var nested = Files.readString(out.resolve("demo/pkg/holder-nested.md"));
        assertTrue(nested.contains("\ndescription: \"A nested Map<K, V> holder.\"\n"), "nested description is plain:\n" + nested);
    }

    @Test
    void cellEscapesPipes() {
        assertEquals("the count, a \\| b", Markdown.cell(" the count,\n a | b "));
    }

    @Test
    void plainRemovesTheEscapeSummaryKeeps() {
        assertEquals("Holds a List<Gift>.", Markdown.plain("Holds a List\\<Gift>."));
        assertEquals("a `code` span", Markdown.plain("a `code` span"));
    }
}
