package docs;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Loads the library's Java sources with JavaParser; nothing is compiled, so Lombok annotations stay annotations. */
final class JavaSources {

    /** The modules whose {@code src/main/java} trees are documented. */
    static final List<String> MODULES = List.of("API", "Client");

    private JavaSources() {}

    /** Every {@code .java} file under the documented modules, parsed at language level Java 17, in path order. */
    static List<CompilationUnit> load(Path repoRoot) {
        StaticJavaParser.setConfiguration(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17));
        var units = new ArrayList<CompilationUnit>();
        for (var module : MODULES) {
            var root = repoRoot.resolve(module).resolve("src/main/java");
            if (!Files.isDirectory(root)) {
                throw new IllegalStateException("missing source tree " + root);
            }
            List<Path> files;
            try (Stream<Path> walk = Files.walk(root)) {
                files = walk.filter(p -> p.toString().endsWith(".java")).sorted().toList();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            for (var file : files) {
                try {
                    units.add(StaticJavaParser.parse(file));
                } catch (ParseProblemException e) {
                    throw new IllegalStateException("cannot parse " + file + ": " + e.getProblems(), e);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return units;
    }

    /** {@code API/src/main/java/io/.../Type.java}: the unit's path from its module directory, or null when unknown. */
    static String sourcePath(CompilationUnit unit) {
        var path = unit.getStorage().map(CompilationUnit.Storage::getPath).orElse(null);
        if (path == null) {
            return null;
        }
        var names = new ArrayList<String>();
        for (var part : path) {
            names.add(part.toString());
        }
        for (int i = 1; i + 2 < names.size(); i++) {
            if (names.get(i).equals("src") && names.get(i + 1).equals("main") && names.get(i + 2).equals("java")) {
                return String.join("/", names.subList(i - 1, names.size()));
            }
        }
        return null;
    }
}
