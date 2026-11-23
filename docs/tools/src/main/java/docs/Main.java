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
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Entry point of the docs tool: {@code generate <repo root>} writes the API and schema reference pages under
 * {@code docs/src/content/docs}; {@code check <repo root>} validates the authored pages.
 */
public final class Main {

    static final String SOURCE_BASE_URL = "https://github.com/jwdeveloper/TikTokLiveJava/blob/master";
    static final Path CONTENT_DIR = Path.of("docs", "src", "content", "docs");
    static final Path PROTO_DIR = Path.of("API", "src", "main", "proto");
    private static final Pattern PARENT_BLOCK = Pattern.compile("(?s)<parent>.*?</parent>");
    private static final Pattern VERSION = Pattern.compile("<version>\\s*([^<\\s]+)\\s*</version>");

    private Main() {}

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    /** Runs a command; exit code 0 on success, 1 on failure, 2 on usage errors. */
    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 2) {
            err.println("usage: docs-tools <generate|check> <repository root>");
            return 2;
        }
        var repoRoot = Path.of(args[1]).toAbsolutePath().normalize();
        switch (args[0]) {
            case "generate":
                try {
                    return generate(repoRoot, out);
                } catch (RuntimeException e) {
                    err.println("generate failed: " + e.getMessage());
                    e.printStackTrace(err);
                    return 1;
                }
            case "check":
                try {
                    return ContentCheck.run(repoRoot, out);
                } catch (RuntimeException e) {
                    err.println("check failed: " + e.getMessage());
                    e.printStackTrace(err);
                    return 1;
                }
            default:
                err.println("unknown command: " + args[0]);
                return 2;
        }
    }

    /** Wipes and rewrites {@code api/} and {@code schema/} under the content directory. */
    static int generate(Path repoRoot, PrintStream out) {
        var version = projectVersion(repoRoot.resolve("pom.xml"));
        var units = JavaSources.load(repoRoot);
        var protoFiles = protoFiles(repoRoot.resolve(PROTO_DIR));
        var content = repoRoot.resolve(CONTENT_DIR);
        var apiDir = content.resolve("api");
        var schemaDir = content.resolve("schema");
        wipe(apiDir);
        wipe(schemaDir);
        var schemaPages = SchemaPages.render(protoFiles, version, schemaDir);
        var apiPages = ApiPages.render(units, version, SOURCE_BASE_URL, apiDir, SchemaPages.javaLinks(protoFiles));
        out.println("Wrote " + apiPages.size() + " API pages to " + apiDir + " and " + schemaPages.size() + " schema pages to " + schemaDir);
        return 0;
    }

    /** The project's own {@code <version>} in a pom.xml: the first one outside any {@code <parent>} block. */
    static String projectVersion(Path pom) {
        String text;
        try {
            text = Files.readString(pom);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        var m = VERSION.matcher(PARENT_BLOCK.matcher(text).replaceFirst(""));
        if (!m.find()) {
            throw new IllegalStateException("no <version> in " + pom);
        }
        return m.group(1);
    }

    private static List<ProtoFile> protoFiles(Path dir) {
        List<Path> paths;
        try (Stream<Path> files = Files.list(dir)) {
            paths = files.filter(p -> p.getFileName().toString().endsWith(".proto")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (paths.isEmpty()) {
            throw new IllegalStateException("no .proto files in " + dir);
        }
        var files = new ArrayList<ProtoFile>();
        for (var path : paths) {
            files.add(ProtoParser.parse(path));
        }
        return files;
    }

    private static void wipe(Path dir) {
        try {
            if (Files.exists(dir)) {
                try (Stream<Path> walk = Files.walk(dir)) {
                    for (var path : walk.sorted(Comparator.reverseOrder()).toList()) {
                        Files.delete(path);
                    }
                }
            }
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
