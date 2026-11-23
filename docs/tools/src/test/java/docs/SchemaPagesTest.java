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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SchemaPagesTest {

    static final Path REPO = Path.of(System.getProperty("repo.root")).toAbsolutePath().normalize();
    static final String VERSION = "1.13.1-Release";

    private static List<ProtoFile> files() {
        return List.of(
                ProtoParser.parse(REPO.resolve("API/src/main/proto/webcast.proto")),
                ProtoParser.parse(REPO.resolve("API/src/main/proto/data.proto")),
                ProtoParser.parse(REPO.resolve("API/src/main/proto/enums.proto")));
    }

    @Test
    void nestedMessageGetsPage(@TempDir Path out) throws IOException {
        var paths = SchemaPages.render(files(), VERSION, out);
        assertTrue(Files.exists(out.resolve("webcast/webcastchatmessage.md")));
        assertTrue(paths.stream().anyMatch(p -> p.getFileName().toString().contains("-")), "a nested type page named outer-inner");
        assertTrue(paths.size() > 300, "pages: " + paths.size());
        var index = Files.readString(out.resolve("index.md"));
        assertTrue(index.startsWith("---\ntitle: TikTok Webcast schema (" + VERSION + ")\n"), "index title");
        assertTrue(Files.exists(out.resolve("webcast/index.md")), "per-file index");
        assertTrue(Files.exists(out.resolve("enums/auditstatus.md")), "enum page");
    }

    @Test
    void fieldsTableLinksTypesAcrossFiles(@TempDir Path out) throws IOException {
        SchemaPages.render(files(), VERSION, out);
        var chat = Files.readString(out.resolve("webcast/webcastchatmessage.md"));
        assertTrue(chat.startsWith("---\ntitle: WebcastChatMessage\n"), "title");
        assertTrue(chat.contains("| Field | Type | Number | Label |"), "fields table header");
        assertTrue(chat.contains("[`CommonMessageData`](/schema/data/commonmessagedata/)"), "cross-file link to data.proto: " + chat);
        var pin = Files.readString(out.resolve("webcast/webcastroompinmessage.md"));
        assertTrue(pin.contains("| oneof |"), "oneof label column");
        assertTrue(pin.contains("[`WebcastChatMessage`](/schema/webcast/webcastchatmessage/)"), "same-file link");
    }

    @Test
    void nestedPageIsTitledOuterDotInner(@TempDir Path out) throws IOException {
        var paths = SchemaPages.render(files(), VERSION, out);
        var nested = paths.stream().filter(p -> p.getFileName().toString().contains("-")).findFirst().orElseThrow();
        var md = Files.readString(nested);
        assertTrue(md.startsWith("---\ntitle: "), "frontmatter");
        var title = md.lines().skip(1).findFirst().orElseThrow();
        assertTrue(title.matches("title: \"?[A-Za-z0-9_]+\\.[A-Za-z0-9_.]+\"?"), "Outer.Inner title: " + title);
        assertTrue(md.contains("Nested in ["), "link back to the enclosing message: " + nested);
    }

    @Test
    void outputIsDeterministic(@TempDir Path a, @TempDir Path b) throws IOException {
        var first = SchemaPages.render(files(), VERSION, a);
        var second = SchemaPages.render(files(), VERSION, b);
        assertEquals(first.stream().map(p -> a.relativize(p).toString()).toList(),
                second.stream().map(p -> b.relativize(p).toString()).toList());
        for (var p : first) {
            assertEquals(Files.readString(p), Files.readString(b.resolve(a.relativize(p))), a.relativize(p).toString());
        }
    }
}
