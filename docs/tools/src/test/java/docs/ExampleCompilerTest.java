package docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExampleCompilerTest {

    static final Path REPO = Path.of(System.getProperty("repo.root")).toAbsolutePath().normalize();
    static final String FRAGMENT = "TikTokLive.newClient(\"user\").onComment((client, event) -> System.out.println(event.getText())).buildAndConnect();";

    @Test
    void fragmentCompiles() {
        assertEquals(List.of(), ExampleCompiler.compile(List.of(new Example("p.md, example 1", FRAGMENT)), REPO));
    }

    @Test
    void fragmentWithUnknownMethodFails() {
        var f = ExampleCompiler.compile(List.of(new Example("p.md, example 1",
                "TikTokLive.newClient(\"user\").onComment((client, event) -> event.getComent());")), REPO);
        assertEquals(1, f.size(), f.toString());
        assertTrue(f.get(0).startsWith("p.md, example 1: line 1: "), f.get(0));
        assertTrue(f.get(0).contains("getComent"), f.get(0));
    }

    @Test
    void fragmentLineNumbersAreRelativeToTheFence() {
        var f = ExampleCompiler.compile(List.of(
                new Example("a.md, example 1", FRAGMENT),
                new Example("b.md, example 3", "int a = 1;\n\nString s = a;")), REPO);
        assertEquals(1, f.size(), f.toString());
        assertTrue(f.get(0).startsWith("b.md, example 3: line 3: "), f.get(0));
    }

    @Test
    void fragmentImportsAreHoisted() {
        var code = "import java.util.concurrent.TimeUnit;\n\nTimeUnit.SECONDS.sleep(1);\n" + FRAGMENT;
        assertEquals(List.of(), ExampleCompiler.compile(List.of(new Example("p.md, example 1", code)), REPO));
    }

    @Test
    void fullUnitCompilesAsIs() {
        var unit = """
                import io.github.jwdeveloper.tiktok.api.annotations.TikTokEventObserver;
                import io.github.jwdeveloper.tiktok.api.data.events.TikTokCommentEvent;
                import io.github.jwdeveloper.tiktok.api.listener.TikTokEventListenerBase;
                import io.github.jwdeveloper.tiktok.api.live.LiveClient;

                public class Demo extends TikTokEventListenerBase {

                    @TikTokEventObserver
                    public void onComment(LiveClient client, TikTokCommentEvent event) {
                        System.out.println(event.getText());
                    }
                }
                """;
        var broken = unit.replace("event.getText()", "event.getComent()");
        assertEquals(List.of(), ExampleCompiler.compile(List.of(new Example("l.md, example 1", unit)), REPO));
        var f = ExampleCompiler.compile(List.of(new Example("l.md, example 1", broken)), REPO);
        assertEquals(1, f.size(), f.toString());
        assertTrue(f.get(0).startsWith("l.md, example 1: line 10: "), f.get(0));
    }

    @Test
    void fullUnitsWithTheSameClassNameDoNotCollide() {
        var main = "import io.github.jwdeveloper.tiktok.client.TikTokLive;\n\npublic class Main {\n    public static void main(String[] args) {\n        "
                + FRAGMENT + "\n    }\n}\n";
        var other = main.replace("newClient(\"user\")", "newClient(\"other\")");
        assertEquals(List.of(), ExampleCompiler.compile(List.of(new Example("a.md, example 1", main), new Example("b.md, example 1", other)), REPO));
        var broken = other.replace("event.getText()", "event.getComent()");
        var f = ExampleCompiler.compile(List.of(new Example("a.md, example 1", main), new Example("b.md, example 1", broken)), REPO);
        assertEquals(1, f.size(), f.toString());
        assertTrue(f.get(0).startsWith("b.md, example 1: line 5: "), f.get(0));
    }

    @Test
    void canaryGuardsVacuousPass(@TempDir Path fake) throws IOException {
        for (var module : ExampleCompiler.MODULES) {
            Files.createDirectories(fake.resolve(module).resolve("target/classes"));
            Files.writeString(fake.resolve(module).resolve("target/classpath.txt"), "");
        }
        for (var module : JavaSources.MODULES) {
            Files.createDirectories(fake.resolve(module).resolve("src/main/java"));
        }
        var f = ExampleCompiler.compile(List.of(), fake);
        assertEquals(List.of("example compiler canary not flagged"), f);
    }

    @Test
    void missingClasspathFileFails(@TempDir Path fake) throws IOException {
        for (var module : JavaSources.MODULES) {
            Files.createDirectories(fake.resolve(module).resolve("src/main/java"));
        }
        var f = ExampleCompiler.compile(List.of(new Example("p.md, example 1", FRAGMENT)), fake);
        assertEquals(1, f.size(), f.toString());
        assertTrue(f.get(0).contains("API/target/classpath.txt"), f.get(0));
        assertTrue(f.get(0).contains(ExampleCompiler.BUILD_COMMAND), f.get(0));
        assertTrue(ExampleCompiler.BUILD_COMMAND.startsWith("mvn -q -pl API,Client,extension-recorder,extension-collector -am -DskipTests install dependency:build-classpath -Dmdep.outputFile=target/classpath.txt"));
    }
}
