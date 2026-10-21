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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * Compiles the Java examples of the authored pages against the built library. A fence that declares a top-level
 * type is compiled as-is (its own imports, a file named after its public type); any other fence is a fragment,
 * wrapped in a method with a wildcard import of every library package. A canary fragment with a misspelled
 * method is always compiled and must be flagged, so a broken classpath cannot make the check pass vacuously.
 */
final class ExampleCompiler {

    /** The modules whose {@code target/classes} and {@code target/classpath.txt} form the compile classpath. */
    static final List<String> MODULES = List.of("API", "Client", "extension-recorder", "extension-collector");
    /** The command, run from the repository root, that produces every file the compiler reads. */
    static final String BUILD_COMMAND = "mvn -q -pl API,Client,extension-recorder,extension-collector -am -DskipTests install"
            + " dependency:build-classpath -Dmdep.outputFile=target/classpath.txt";
    static final String CANARY_LABEL = "canary";
    static final String CANARY = "TikTokLive.newClient(\"x\").onComment((c, e) -> e.getComent());";
    static final String CANARY_NOT_FLAGGED = "example compiler canary not flagged";
    /** JDK packages imported into every fragment besides the library's own. */
    static final List<String> JDK_PACKAGES = List.of("java.util", "java.time", "java.util.function", "java.util.logging", "java.io");

    private static final Pattern TYPE_DECLARATION = Pattern.compile("^(public\\s+)?(final\\s+|abstract\\s+)?(class|interface|record|enum)\\s+(\\w+)");
    private static final Pattern PUBLIC_TYPE = Pattern.compile("^public\\s+(final\\s+|abstract\\s+)?(class|interface|record|enum)\\s+(\\w+)");
    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(static\\s+)?[\\w.]+(\\.\\*)?\\s*;\\s*$");

    private ExampleCompiler() {}

    /**
     * One source file handed to javac: which example it came from, the package-qualified types it declares (no two
     * files of one batch may share one), how many wrapper lines precede its body, and for a fragment the wrapper line
     * of each hoisted {@code import} mapped to the body line it came from.
     */
    private record Unit(Example example, Set<String> keys, Source source, int offset, int bodyLines, Map<Integer, Integer> hoisted) {}

    private static final class Source extends SimpleJavaFileObject {
        private final String code;

        Source(String path, String code) {
            super(URI.create("string:///" + path), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    /** Compiles the examples; returns one failure line per compiler error, or the reason the compiler could not run. */
    static List<String> compile(List<Example> examples, Path repoRoot) {
        return compile(examples, repoRoot, packages(JavaSources.load(repoRoot)));
    }

    /** As {@link #compile(List, Path)} with the library packages already known. */
    static List<String> compile(List<Example> examples, Path repoRoot, Collection<String> packages) {
        List<Path> classpath;
        try {
            classpath = classpath(repoRoot);
        } catch (IllegalStateException e) {
            return List.of(e.getMessage());
        }
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return List.of("no system Java compiler: run the docs tool on a JDK, not a JRE");
        }
        var units = new ArrayList<Unit>();
        units.add(unit(new Example(CANARY_LABEL, CANARY), 0, packages));
        for (int i = 0; i < examples.size(); i++) {
            units.add(unit(examples.get(i), i + 1, packages));
        }
        var failures = new ArrayList<String>();
        var canaryFlagged = false;
        Path outDir;
        try {
            outDir = Files.createTempDirectory("docs-examples");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            for (var batch : batches(units)) {
                var byUri = new HashMap<String, Unit>();
                for (var unit : batch) {
                    byUri.put(unit.source().toUri().toString(), unit);
                }
                for (var d : run(compiler, classpath, outDir, batch)) {
                    if (d.getKind() != Diagnostic.Kind.ERROR) {
                        continue;
                    }
                    var message = oneLine(d.getMessage(Locale.ROOT));
                    var unit = d.getSource() == null ? null : byUri.get(d.getSource().toUri().toString());
                    if (unit == null) {
                        failures.add("javac: " + message);
                    } else if (unit.example().label().equals(CANARY_LABEL)) {
                        canaryFlagged |= message.contains("getComent");
                    } else {
                        failures.add(unit.example().label() + ": line " + line(d, unit) + ": " + message);
                    }
                }
            }
        } finally {
            delete(outDir);
        }
        if (!canaryFlagged) {
            failures.add(0, CANARY_NOT_FLAGGED);
        }
        return failures;
    }

    /** Every package declared by the parsed library sources, sorted. */
    static Set<String> packages(List<CompilationUnit> units) {
        var packages = new TreeSet<String>();
        for (var unit : units) {
            unit.getPackageDeclaration().map(PackageDeclaration::getNameAsString).ifPresent(packages::add);
        }
        return packages;
    }

    /** True when the fence declares a top-level type and so compiles as its own file. */
    static boolean isFullUnit(String code) {
        return code.lines().anyMatch(line -> TYPE_DECLARATION.matcher(line).find());
    }

    // --- classpath ----------------------------------------------------------------------------

    private static List<Path> classpath(Path repoRoot) {
        var classpath = new LinkedHashSet<Path>();
        var missing = new ArrayList<String>();
        for (var module : MODULES) {
            var classes = repoRoot.resolve(module).resolve("target").resolve("classes");
            var file = repoRoot.resolve(module).resolve("target").resolve("classpath.txt");
            if (Files.isDirectory(classes)) {
                classpath.add(classes);
            } else {
                missing.add(module + "/target/classes");
            }
            if (!Files.isRegularFile(file)) {
                missing.add(module + "/target/classpath.txt");
                continue;
            }
            String text;
            try {
                text = Files.readString(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            for (var entry : text.split("[" + File.pathSeparator + "\\r\\n]+")) {
                if (!entry.isBlank()) {
                    classpath.add(Path.of(entry.strip()));
                }
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("missing " + String.join(", ", missing)
                    + "; build the library first, from the repository root: " + BUILD_COMMAND);
        }
        return new ArrayList<>(classpath);
    }

    // --- sources ------------------------------------------------------------------------------

    private static Unit unit(Example example, int index, Collection<String> packages) {
        var code = example.code();
        var dir = "ex" + index + "/";
        if (isFullUnit(code)) {
            var name = declaredType(code);
            var source = code.endsWith("\n") ? code : code + "\n";
            var keys = new HashSet<String>();
            for (var declared : declaredTypes(code)) {
                keys.add(packageOf(code) + "/" + declared);
            }
            return new Unit(example, keys, new Source(dir + name + ".java", source), 0, (int) code.lines().count(), Map.of());
        }
        var imports = new LinkedHashSet<String>();
        for (var pkg : packages) {
            imports.add("import " + pkg + ".*;");
        }
        for (var pkg : JDK_PACKAGES) {
            imports.add("import " + pkg + ".*;");
        }
        var hoistedLines = new LinkedHashMap<String, Integer>();
        var body = new StringBuilder();
        var bodyLines = 0;
        for (var line : code.split("\n", -1)) {
            if (IMPORT.matcher(line).matches()) {
                imports.add(line.strip());
                hoistedLines.putIfAbsent(line.strip(), bodyLines + 1);
                line = "";
            }
            body.append(line).append('\n');
            bodyLines++;
        }
        var name = "Example" + index;
        var source = new StringBuilder();
        var ordered = new ArrayList<>(imports);
        for (var line : ordered) {
            source.append(line).append('\n');
        }
        var hoisted = new HashMap<Integer, Integer>();
        hoistedLines.forEach((statement, bodyLine) -> hoisted.put(ordered.indexOf(statement) + 1, bodyLine));
        source.append("public class ").append(name).append(" {\n");
        source.append("    void run() throws Exception {\n");
        source.append(body);
        source.append("    }\n}\n");
        return new Unit(example, Set.of("/" + name), new Source(dir + name + ".java", source.toString()), imports.size() + 2, bodyLines, hoisted);
    }

    /** Every top-level type a full unit declares, the public one and any package-private helpers. */
    private static List<String> declaredTypes(String code) {
        var names = new ArrayList<String>();
        for (var line : code.split("\n")) {
            var m = TYPE_DECLARATION.matcher(line);
            if (m.find()) {
                names.add(m.group(4));
            }
        }
        return names;
    }

    /** The declared package of a full unit, or an empty string for the default package. */
    private static String packageOf(String code) {
        for (var line : code.split("\n")) {
            var m = PACKAGE.matcher(line);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "";
    }

    /** The public top-level type's name, else the first declared type's: the file must be named after it. */
    private static String declaredType(String code) {
        for (var line : code.split("\n")) {
            var m = PUBLIC_TYPE.matcher(line);
            if (m.find()) {
                return m.group(3);
            }
        }
        for (var line : code.split("\n")) {
            var m = TYPE_DECLARATION.matcher(line);
            if (m.find()) {
                return m.group(4);
            }
        }
        throw new IllegalArgumentException("no type declaration");
    }

    /** Splits the units into batches in which no two files declare the same package-qualified type, helpers included. */
    private static List<List<Unit>> batches(List<Unit> units) {
        var batches = new ArrayList<List<Unit>>();
        var keys = new ArrayList<Set<String>>();
        for (var unit : units) {
            int i = 0;
            while (i < batches.size() && !Collections.disjoint(keys.get(i), unit.keys())) {
                i++;
            }
            if (i == batches.size()) {
                batches.add(new ArrayList<>());
                keys.add(new HashSet<>());
            }
            batches.get(i).add(unit);
            keys.get(i).addAll(unit.keys());
        }
        return batches;
    }

    // --- javac --------------------------------------------------------------------------------

    private static List<Diagnostic<? extends JavaFileObject>> run(JavaCompiler compiler, List<Path> classpath, Path outDir, List<Unit> batch) {
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var sources = new ArrayList<JavaFileObject>();
        for (var unit : batch) {
            sources.add(unit.source());
        }
        try (var fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            fileManager.setLocationFromPaths(StandardLocation.CLASS_PATH, classpath);
            fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(outDir));
            // -Xmaxerrs lifts javac's default cap of 100 errors, so a batch with many broken examples reports them all.
            var options = List.of("--release", "17", "-proc:none", "-implicit:none", "-Xlint:none", "-Xmaxerrs", "10000");
            compiler.getTask(new StringWriter(), fileManager, diagnostics, options, null, sources).call();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return diagnostics.getDiagnostics();
    }

    /**
     * The diagnostic's line relative to the fence body: a hoisted {@code import} maps back to the line it was written on,
     * and an error on any other wrapper line is clamped into the body.
     */
    private static long line(Diagnostic<?> d, Unit unit) {
        var hoisted = unit.hoisted().get((int) d.getLineNumber());
        if (hoisted != null) {
            return hoisted;
        }
        var line = d.getLineNumber() - unit.offset();
        return Math.max(1, Math.min(line, Math.max(1, unit.bodyLines())));
    }

    private static String oneLine(String message) {
        return String.join("; ", message.lines().map(String::strip).filter(s -> !s.isEmpty()).toList());
    }

    private static void delete(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            for (var path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // A leftover temp directory is not worth failing the check over.
        }
    }
}
