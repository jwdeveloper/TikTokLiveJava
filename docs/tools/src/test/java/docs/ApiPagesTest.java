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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApiPagesTest {

    static final Path REPO = Path.of(System.getProperty("repo.root")).toAbsolutePath().normalize();
    static final String VERSION = "1.13.1-Release";
    static final String SOURCE_BASE = "https://github.com/jwdeveloper/TikTokLiveJava/blob/master";

    @Test
    void slugLowercasesAndNestsPackages() {
        assertEquals("io/github/jwdeveloper/tiktok/api/live/liveclient", ApiPages.slug("io.github.jwdeveloper.tiktok.api.live.LiveClient"));
    }

    @Test
    void nestedTypeSlugJoinsOuterAndInnerWithHyphen() {
        assertEquals("io/github/jwdeveloper/tiktok/api/data/requests/livedata-request",
                ApiPages.slug("io.github.jwdeveloper.tiktok.api.data.requests.LiveData$Request"));
    }

    @Test
    void liveClientSettingsPageListsFields(@TempDir Path out) throws IOException {
        var units = JavaSources.load(REPO);
        var paths = ApiPages.render(units, VERSION, SOURCE_BASE, out);
        var page = out.resolve("io/github/jwdeveloper/tiktok/api/data/settings/liveclientsettings.md");
        assertTrue(Files.exists(page), "page exists");
        var md = Files.readString(page);
        assertTrue(md.startsWith("---\ntitle: LiveClientSettings\n"), "frontmatter starts with the title");
        assertTrue(md.contains("\nsidebar:\n  label: LiveClientSettings\n"), "sidebar label");
        for (var f : List.of("offline", "fetchGifts", "retryOnConnectionFailure", "pingInterval", "apiKey")) {
            assertTrue(md.contains("`" + f + "`"), f);
        }
        assertTrue(md.contains("@Data"), "Lombok annotation kept in the signature");
        assertTrue(md.contains("getter"), "Lombok accessors noted");
        assertTrue(md.contains("## Source"), "source section");
        assertTrue(md.contains(SOURCE_BASE + "/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/settings/LiveClientSettings.java#L"), "source link");
        assertTrue(paths.size() > 150, "pages: " + paths.size());
        assertTrue(Files.exists(out.resolve("index.md")), "root index");
        assertTrue(Files.readString(out.resolve("index.md")).startsWith("---\ntitle: TikTokLiveJava " + VERSION + "\n"), "root index title");
        assertTrue(Files.exists(out.resolve("io/github/jwdeveloper/tiktok/api/data/settings/index.md")), "package index");
        assertTrue(Files.exists(out.resolve("io/github/jwdeveloper/tiktok/api/data/requests/livedata-request.md")), "nested type page");
    }

    @Test
    void typeWithoutJavadocStillRenders(@TempDir Path out) throws IOException {
        ApiPages.render(JavaSources.load(REPO), VERSION, SOURCE_BASE, out);
        var page = out.resolve("io/github/jwdeveloper/tiktok/api/annotations/eventtype.md");
        assertTrue(Files.exists(page), "page exists");
        var md = Files.readString(page);
        assertTrue(md.startsWith("---\ntitle: EventType\n"), "title");
        assertTrue(md.contains("\ndescription: \"enum io.github.jwdeveloper.tiktok.api.annotations.EventType\"\n"), "fallback description");
        assertTrue(md.contains("`Control`"), "enum constants listed");
    }

    @Test
    void everyPageHasFrontmatter(@TempDir Path out) throws IOException {
        var paths = ApiPages.render(JavaSources.load(REPO), VERSION, SOURCE_BASE, out);
        for (var p : paths) {
            var md = Files.readString(p);
            assertTrue(md.startsWith("---\ntitle: "), p + " starts with frontmatter");
            assertTrue(md.contains("\nsidebar:\n  label: "), p + " has a sidebar label");
        }
    }

    @Test
    void outputIsDeterministic(@TempDir Path a, @TempDir Path b) throws IOException {
        var units = JavaSources.load(REPO);
        var first = ApiPages.render(units, VERSION, SOURCE_BASE, a);
        var second = ApiPages.render(JavaSources.load(REPO), VERSION, SOURCE_BASE, b);
        assertEquals(relative(a, first), relative(b, second), "same file list in the same order");
        for (var p : first) {
            var twin = b.resolve(a.relativize(p));
            assertEquals(Files.readString(p), Files.readString(twin), "same content for " + a.relativize(p));
        }
        try (Stream<Path> files = Files.walk(a)) {
            assertEquals(first.size(), files.filter(Files::isRegularFile).count(), "render returns every file it wrote");
        }
    }

    private static List<String> relative(Path root, List<Path> paths) {
        return paths.stream().map(p -> root.relativize(p).toString()).toList();
    }
}
