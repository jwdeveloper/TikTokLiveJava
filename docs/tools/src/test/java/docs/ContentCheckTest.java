package docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ContentCheckTest {

    static final Path REPO = Path.of(System.getProperty("repo.root")).toAbsolutePath().normalize();

    @Test
    void eventsMapIncludesCategories() {
        var e = ContentCheck.events(REPO);
        assertEquals("Message", e.get("TikTokCommentEvent"));
        assertEquals("Control", e.get("TikTokConnectedEvent"));
        assertTrue(e.size() >= 73, "events: " + e.size());
        // Ruling J7: abstract classes are not events; concrete classes without @EventMeta have the category `none`.
        assertFalse(e.containsKey("TikTokEvent"), "abstract base class is excluded");
        assertEquals("none", e.get("TikTokHeaderEvent"));
    }

    @Test
    void unannotatedConcreteEventsGetCategoryNoneAndAbstractBaseIsExcluded() {
        var e = ContentCheck.events(REPO);
        assertEquals("none", e.get("TikTokHeaderEvent"));
        assertEquals("none", e.get("TikTokUnhandledEvent"));
        assertFalse(e.containsKey("TikTokEvent"), "abstract base class is not an event");
        assertFalse(e.containsKey("CaptionContent"), "nested types are not events");
        assertTrue(ContentCheck.eventClassNames(REPO).contains("TikTokEvent"), "README anchors may name the base class");
    }

    @Test
    void settingsHandlersExceptions() {
        assertTrue(ContentCheck.settingsFields(REPO).contains("retryConnectionTimeout"));
        assertTrue(ContentCheck.settingsFields(REPO).contains("timeout"), "HttpClientSettings field");
        assertTrue(ContentCheck.handlers(REPO).contains("onComment"));
        assertTrue(ContentCheck.handlers(REPO).contains("sendChat"), "LiveClient method");
        assertFalse(ContentCheck.builderHandlers(REPO).contains("sendChat"), "builder handlers are on* methods of EventsBuilder only");
        assertEquals(11, ContentCheck.exceptions(REPO).size());
    }

    @Test
    void checkEventsReportsMissingEvent(@TempDir Path site) throws IOException {
        write(site, "t.md", "The `TikTokCommentEvent` event.\n");
        write(site, "events/index.md", "| Event | Type | Description |\n|---|---|---|\n| `TikTokCommentEvent` | Message | A comment. |\n");
        var events = Map.of("TikTokCommentEvent", "Message", "TikTokNewShinyEvent", "Message");
        var failures = ContentCheck.checkEvents(events, ContentCheck.pages(site));
        assertTrue(failures.contains("Missing event documentation: TikTokNewShinyEvent"), failures.toString());
        assertTrue(failures.contains("Event catalog row missing: TikTokNewShinyEvent"), failures.toString());
        assertFalse(failures.stream().anyMatch(f -> f.contains("TikTokCommentEvent")), failures.toString());
    }

    @Test
    void catalogRowTypeMustMatchAnnotation(@TempDir Path site) throws IOException {
        write(site, "events/index.md", "| Event | Type | Description |\n|---|---|---|\n| `TikTokCommentEvent` | Control | x |\n");
        var failures = ContentCheck.checkEvents(Map.of("TikTokCommentEvent", "Message"), ContentCheck.pages(site));
        assertEquals(List.of("Event catalog row is stale: TikTokCommentEvent"), failures);
    }

    @Test
    void catalogRowAcceptsNoneForUnannotatedEvents(@TempDir Path site) throws IOException {
        write(site, "events/index.md", "| `TikTokHeaderEvent` | none | base |\n");
        assertEquals(List.of(), ContentCheck.checkEvents(Map.of("TikTokHeaderEvent", "none"), ContentCheck.pages(site)));
    }

    @Test
    void namesMustAppearInBackticks(@TempDir Path site) throws IOException {
        write(site, "t.md", "Set `retryConnectionTimeout`; plain pingInterval is not documentation.\n");
        var failures = ContentCheck.checkNames("settings field", List.of("retryConnectionTimeout", "pingInterval"), ContentCheck.pages(site));
        assertEquals(List.of("Missing settings field documentation: pingInterval"), failures);
    }

    @Test
    void generatedPagesDoNotCount(@TempDir Path site) throws IOException {
        write(site, "api/x.md", "`TikTokLiveException`\n");
        write(site, "schema/y.md", "`TikTokLiveException`\n");
        write(site, "z.txt", "`TikTokLiveException`\n");
        assertEquals(List.of("Missing exception documentation: TikTokLiveException"),
                ContentCheck.checkNames("exception", List.of("TikTokLiveException"), ContentCheck.pages(site)));
    }

    @Test
    void readmeHandlerWithoutClassFails() {
        var failures = ContentCheck.checkReadme("- [onComent](#oncoment-tiktokcomentevent)\n", Set.of("onComment"), Set.of("TikTokCommentEvent"));
        assertTrue(failures.contains("README handler has no event class: onComent"), failures.toString());
    }

    @Test
    void readmeHandlerNotOnBuilderFails() {
        var failures = ContentCheck.checkReadme("- [onCommentary](#oncommentary-tiktokcommentevent)\n", Set.of("onComment"), Set.of("TikTokCommentEvent"));
        assertEquals(List.of("README handler is not on EventsBuilder: onCommentary"), failures);
    }

    @Test
    void readmeItemWithTrailingTextPasses() {
        var readme = "**Message**:\n\n- [onComment](#oncomment-tiktokcommentevent) · [docs](https://example.com/events/chat/#tiktokcommentevent)\n"
                + "- [onWebsocketUnhandledMessage](#onwebsocketunhandledmessage-tiktokwebsocketunhandledmessageevent)\n";
        var events = Set.of("TikTokCommentEvent", "TikTokWebsocketUnhandledMessageEvent");
        assertEquals(List.of(), ContentCheck.checkReadme(readme, Set.of("onComment", "onWebsocketUnhandledMessage"), events));
    }

    @Test
    void repositoryReadmeItemsAreParsedAndPassAgainstTheSources() throws IOException {
        var readme = Files.readString(REPO.resolve("README.md"));
        var unresolved = ContentCheck.checkReadme(readme, Set.of(), Set.of());
        assertTrue(unresolved.contains("README handler has no event class: onComment"), unresolved.toString());
        assertTrue(unresolved.contains("README handler is not on EventsBuilder: onEvent"), unresolved.toString());
        assertTrue(unresolved.size() >= 40, "every item yields two failures when nothing resolves: " + unresolved.size());
        assertEquals(List.of(), ContentCheck.checkReadme(readme, ContentCheck.builderHandlers(REPO), ContentCheck.eventClassNames(REPO)));
    }

    @Test
    void discordLinkFails(@TempDir Path site) throws IOException {
        write(site, "t.md", "Join https://discord.gg/abc\n");
        write(site, "ok.md", "Nothing here.\n");
        assertEquals(List.of("t.md: published pages must not reference Discord"), ContentCheck.checkDiscord(ContentCheck.pages(site)));
    }

    @Test
    void indentedFencesAreExtractedAndDedented(@TempDir Path site) throws IOException {
        write(site, "a/b.md", """
                Intro.

                ```java
                int a = 1;
                ```

                1. Step
                   ```java title="Demo.java"
                   int b = 2;
                     int c = 3;
                   ```

                ```ts
                const notJava = ```;
                ```
                """);
        var examples = ContentCheck.examples(ContentCheck.pages(site));
        assertEquals(2, examples.size(), examples.toString());
        assertEquals("a/b.md, example 1", examples.get(0).label());
        assertEquals("int a = 1;", examples.get(0).code());
        assertEquals("a/b.md, example 2", examples.get(1).label());
        assertEquals("int b = 2;\n  int c = 3;", examples.get(1).code());
    }

    @Test
    void runPrintsEverySectionInOrderThenTheSummary() {
        var bytes = new ByteArrayOutputStream();
        var code = ContentCheck.run(REPO, new PrintStream(bytes, true, StandardCharsets.UTF_8));
        var lines = bytes.toString(StandardCharsets.UTF_8).strip().split("\n");
        var sections = List.of("events", "settings", "builder", "exceptions", "extensions", "discord", "readme", "examples");
        assertEquals(sections.size() + 1, lines.length, String.join("\n", lines));
        for (int i = 0; i < sections.size(); i++) {
            assertTrue(lines[i].matches("(PASS " + sections.get(i) + "|FAIL " + sections.get(i) + ": .+)"), lines[i]);
        }
        assertTrue(lines[sections.size()].matches("Checked \\d+ pages, \\d+ events, \\d+ settings fields, \\d+ handlers, \\d+ exceptions, \\d+ Java examples\\."), lines[sections.size()]);
        var anyFailed = String.join("\n", lines).contains("\nFAIL ") || lines[0].startsWith("FAIL ");
        assertEquals(anyFailed ? 1 : 0, code);
    }

    private static void write(Path root, String relative, String text) throws IOException {
        var path = root.resolve(relative);
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }
}
