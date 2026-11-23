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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The {@code check} command: verifies that the authored pages cover the library (events, each with a section on a
 * grouped events page, settings, handlers, exceptions, extensions), reference no Discord link, agree with the
 * README's events list and resolve its hosted links, and that every Java example compiles.
 */
final class ContentCheck {

    static final List<String> SECTIONS = List.of("events", "settings", "builder", "exceptions", "extensions", "discord", "readme", "examples");
    static final List<String> EXTENSIONS = List.of("TikTokLiveRecorder", "TikTokLiveCollector");
    static final String EVENTS_PACKAGE = "io.github.jwdeveloper.tiktok.api.data.events";
    static final String EXCEPTIONS_PACKAGE = "io.github.jwdeveloper.tiktok.api.exceptions";
    static final String EVENTS_BUILDER = "io.github.jwdeveloper.tiktok.api.live.builder.EventsBuilder";
    static final String LIVE_CLIENT = "io.github.jwdeveloper.tiktok.api.live.LiveClient";
    static final List<String> SETTINGS_CLASSES = List.of("io.github.jwdeveloper.tiktok.api.data.settings.LiveClientSettings",
            "io.github.jwdeveloper.tiktok.api.data.settings.HttpClientSettings");
    static final String CATALOG_PAGE = "events/index.md";
    /** The directory of the grouped event pages, each event's {@code ## <EventClassName>} section lives on one of them. */
    static final String EVENTS_DIR = "events/";
    /** Category of an event class under the events package that carries no {@code @EventMeta}. */
    static final String NO_CATEGORY = "none";
    /** Generated directories under the content root; their pages are not authored documentation. */
    static final Set<String> GENERATED_DIRS = Set.of("api", "schema");
    /** The site's origin: the README's hosted links start with it and are resolved against the authored pages. */
    static final String SITE = "https://ttlj.docs.isaackogan.com/";

    private static final Pattern CATALOG_ROW = Pattern.compile("^\\s*\\|\\s*`(\\w+)`\\s*\\|\\s*(\\w+)\\s*\\|", Pattern.MULTILINE);
    private static final Pattern README_ITEM = Pattern.compile("^\\s*-\\s+\\[(on\\w+)]\\(#([\\w-]+)\\)", Pattern.MULTILINE);
    private static final Pattern EVENTS_HEADING = Pattern.compile("^##\\s+Events\\s*$", Pattern.MULTILINE);
    /** A level-1 or level-2 heading other than a repeated {@code ## Events}: where the README's events list ends. */
    private static final Pattern NEXT_SECTION = Pattern.compile("^#{1,2}\\s+(?!Events\\s*$).*$", Pattern.MULTILINE);
    private static final Pattern SITE_LINK = Pattern.compile(Pattern.quote(SITE) + "([\\w./-]*)(?:#([\\w-]+))?");
    private static final Pattern EVENT_HEADING = Pattern.compile("^##\\s+`?(\\w+)`?\\s*$", Pattern.MULTILINE);
    private static final Pattern HEADING = Pattern.compile("^#{1,6}\\s+(.+?)\\s*$", Pattern.MULTILINE);
    private static final Pattern DISCORD = Pattern.compile("(?i)discord\\.(com|gg)");
    private static final Pattern FENCE_OPEN = Pattern.compile("^\\s*(`{3,}|~{3,})\\s*(\\w*)(.*)$");
    private static final Pattern FENCE_CLOSE = Pattern.compile("^\\s*(`{3,}|~{3,})\\s*$");

    private ContentCheck() {}

    /** An authored page: its path relative to the content root with {@code /} separators, and its text. */
    record Page(String path, String text) {}

    // --- command ------------------------------------------------------------------------------

    /** Prints one line per section in order, then the summary; returns 1 when any section failed. */
    static int run(Path repoRoot, PrintStream out) {
        var units = JavaSources.load(repoRoot);
        var events = events(units);
        var settings = settingsFields(units);
        var handlers = handlers(units);
        var exceptions = exceptions(units);
        var pages = pages(repoRoot.resolve(Main.CONTENT_DIR));
        var examples = examples(pages);
        var exampleFailures = new ArrayList<>(unclosedFences(pages));
        var results = new LinkedHashMap<String, List<String>>();
        results.put("events", checkEvents(events, pages));
        results.put("settings", checkNames("settings field", settings, pages));
        results.put("builder", checkNames("handler", handlers, pages));
        results.put("exceptions", checkNames("exception", exceptions, pages));
        results.put("extensions", checkNames("extension", EXTENSIONS, pages));
        results.put("discord", checkDiscord(pages));
        var readme = read(repoRoot.resolve("README.md"));
        var readmeFailures = new ArrayList<>(checkReadme(readme, builderHandlers(units), eventClassNames(units)));
        readmeFailures.addAll(checkReadmeLinks(readme, pages));
        results.put("readme", readmeFailures);
        exampleFailures.addAll(ExampleCompiler.compile(examples, repoRoot, ExampleCompiler.packages(units)));
        results.put("examples", exampleFailures);
        var failed = false;
        for (var section : SECTIONS) {
            var failures = results.get(section);
            if (failures.isEmpty()) {
                out.println("PASS " + section);
            } else {
                failed = true;
                out.println("FAIL " + section + ": " + String.join("; ", failures.subList(0, Math.min(3, failures.size()))));
            }
        }
        out.println("Checked " + pages.size() + " pages, " + events.size() + " events, " + settings.size() + " settings fields, "
                + handlers.size() + " handlers, " + exceptions.size() + " exceptions, " + examples.size() + " Java examples.");
        return failed ? 1 : 0;
    }

    // --- extraction ---------------------------------------------------------------------------

    /** Concrete event class → {@code @EventMeta} category ({@code Control}, {@code Message}, {@code Debug}) or {@code none}. */
    static Map<String, String> events(Path repoRoot) {
        return events(JavaSources.load(repoRoot));
    }

    static Map<String, String> events(List<CompilationUnit> units) {
        var events = new TreeMap<String, String>();
        for (var type : eventTypes(units)) {
            if (type instanceof ClassOrInterfaceDeclaration c && !c.isInterface() && !c.isAbstract()) {
                events.put(c.getNameAsString(), c.getAnnotationByName("EventMeta").map(ContentCheck::category).orElse(NO_CATEGORY));
            }
        }
        return events;
    }

    /** Every top-level type under the events package, abstract base classes included: what a README anchor may name. */
    static Set<String> eventClassNames(Path repoRoot) {
        return eventClassNames(JavaSources.load(repoRoot));
    }

    static Set<String> eventClassNames(List<CompilationUnit> units) {
        var names = new TreeSet<String>();
        for (var type : eventTypes(units)) {
            names.add(type.getNameAsString());
        }
        return names;
    }

    /** Instance fields of {@code LiveClientSettings} and {@code HttpClientSettings}, in declaration order. */
    static List<String> settingsFields(Path repoRoot) {
        return settingsFields(JavaSources.load(repoRoot));
    }

    static List<String> settingsFields(List<CompilationUnit> units) {
        var fields = new ArrayList<String>();
        for (var name : SETTINGS_CLASSES) {
            for (var field : type(units, name).getFields()) {
                if (!field.isStatic()) {
                    for (var variable : field.getVariables()) {
                        fields.add(variable.getNameAsString());
                    }
                }
            }
        }
        return fields;
    }

    /** Distinct names of the public {@code on*} methods of {@code EventsBuilder}, in declaration order. */
    static List<String> builderHandlers(Path repoRoot) {
        return builderHandlers(JavaSources.load(repoRoot));
    }

    static List<String> builderHandlers(List<CompilationUnit> units) {
        return publicMethods(type(units, EVENTS_BUILDER), true);
    }

    /** {@link #builderHandlers} followed by the distinct public methods of {@code LiveClient}. */
    static List<String> handlers(Path repoRoot) {
        return handlers(JavaSources.load(repoRoot));
    }

    static List<String> handlers(List<CompilationUnit> units) {
        var handlers = new LinkedHashSet<>(builderHandlers(units));
        handlers.addAll(publicMethods(type(units, LIVE_CLIENT), false));
        return new ArrayList<>(handlers);
    }

    /** Names of the top-level types under the exceptions package, sorted. */
    static List<String> exceptions(Path repoRoot) {
        return exceptions(JavaSources.load(repoRoot));
    }

    static List<String> exceptions(List<CompilationUnit> units) {
        var names = new TreeSet<String>();
        for (var unit : units) {
            if (packageOf(unit).equals(EXCEPTIONS_PACKAGE)) {
                for (var type : unit.getTypes()) {
                    names.add(type.getNameAsString());
                }
            }
        }
        return new ArrayList<>(names);
    }

    private static List<TypeDeclaration<?>> eventTypes(List<CompilationUnit> units) {
        var types = new ArrayList<TypeDeclaration<?>>();
        for (var unit : units) {
            var pkg = packageOf(unit);
            if (pkg.equals(EVENTS_PACKAGE) || pkg.startsWith(EVENTS_PACKAGE + ".")) {
                types.addAll(unit.getTypes());
            }
        }
        return types;
    }

    /** {@code EventType.Message} → {@code Message}; an {@code @EventMeta} without the member is {@code none}. */
    private static String category(AnnotationExpr annotation) {
        Expression value = null;
        if (annotation.isNormalAnnotationExpr()) {
            for (var pair : annotation.asNormalAnnotationExpr().getPairs()) {
                if (pair.getNameAsString().equals("eventType")) {
                    value = pair.getValue();
                }
            }
        } else if (annotation.isSingleMemberAnnotationExpr()) {
            value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
        }
        if (value == null) {
            return NO_CATEGORY;
        }
        var text = value.toString();
        return text.substring(text.lastIndexOf('.') + 1);
    }

    private static List<String> publicMethods(TypeDeclaration<?> type, boolean onlyHandlers) {
        var isInterface = type instanceof ClassOrInterfaceDeclaration c && c.isInterface();
        var names = new LinkedHashSet<String>();
        for (MethodDeclaration method : type.getMethods()) {
            var isPublic = method.isPublic() || (isInterface && !method.isPrivate());
            var name = method.getNameAsString();
            if (isPublic && (!onlyHandlers || (name.startsWith("on") && name.length() > 2 && Character.isUpperCase(name.charAt(2))))) {
                names.add(name);
            }
        }
        return new ArrayList<>(names);
    }

    private static TypeDeclaration<?> type(List<CompilationUnit> units, String qualifiedName) {
        var dot = qualifiedName.lastIndexOf('.');
        var pkg = qualifiedName.substring(0, dot);
        var name = qualifiedName.substring(dot + 1);
        for (var unit : units) {
            if (packageOf(unit).equals(pkg)) {
                for (var type : unit.getTypes()) {
                    if (type.getNameAsString().equals(name)) {
                        return type;
                    }
                }
            }
        }
        throw new IllegalStateException("missing type " + qualifiedName);
    }

    private static String packageOf(CompilationUnit unit) {
        return unit.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse("");
    }

    // --- pages --------------------------------------------------------------------------------

    /** Every {@code .md}/{@code .mdx} page under the content root except the generated {@code api/} and {@code schema/} trees. */
    static List<Page> pages(Path contentDir) {
        if (!Files.isDirectory(contentDir)) {
            throw new IllegalStateException("missing content directory " + contentDir);
        }
        var pages = new ArrayList<Page>();
        try (Stream<Path> walk = Files.walk(contentDir)) {
            for (var path : walk.filter(Files::isRegularFile).sorted().toList()) {
                var relative = contentDir.relativize(path);
                var name = relative.getFileName().toString();
                if (!(name.endsWith(".md") || name.endsWith(".mdx")) || GENERATED_DIRS.contains(relative.getName(0).toString())) {
                    continue;
                }
                var parts = new ArrayList<String>();
                relative.forEach(p -> parts.add(p.toString()));
                pages.add(new Page(String.join("/", parts), Files.readString(path)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return pages;
    }

    /** Every {@code ```java} fence of every page, dedented, labelled "{@code <page>, example <n>}". */
    static List<Example> examples(List<Page> pages) {
        var examples = new ArrayList<Example>();
        for (var page : pages) {
            var n = 0;
            for (var body : fences(page.text(), "java")) {
                examples.add(new Example(page.path() + ", example " + ++n, body));
            }
        }
        return examples;
    }

    /** The dedented bodies of the closed fences with the given language (compared ignoring case), at any indentation. */
    static List<String> fences(String text, String language) {
        var bodies = new ArrayList<String>();
        for (var fence : scan(text)) {
            if (fence.closed() && fence.language().equalsIgnoreCase(language)) {
                bodies.add(dedent(fence.body()));
            }
        }
        return bodies;
    }

    /** One failure per fence that is never closed: Markdown renders the rest of the page as code, and no example compiles. */
    static List<String> unclosedFences(List<Page> pages) {
        var failures = new ArrayList<String>();
        for (var page : pages) {
            for (var fence : scan(page.text())) {
                if (!fence.closed()) {
                    failures.add(page.path() + ": the fence opened at line " + fence.line() + " is never closed");
                }
            }
        }
        return failures;
    }

    /** One fenced block: its language tag, body lines, the line number of its opening fence, and whether it was closed. */
    private record Fence(String language, List<String> body, int line, boolean closed) {}

    private static List<Fence> scan(String text) {
        var fences = new ArrayList<Fence>();
        String fence = null;
        String lang = null;
        var openLine = 0;
        var body = new ArrayList<String>();
        var lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            if (fence == null) {
                var open = FENCE_OPEN.matcher(line);
                if (open.matches()) {
                    fence = open.group(1);
                    lang = open.group(2);
                    openLine = i + 1;
                    body = new ArrayList<>();
                }
                continue;
            }
            var close = FENCE_CLOSE.matcher(line);
            if (close.matches() && close.group(1).charAt(0) == fence.charAt(0) && close.group(1).length() >= fence.length()) {
                fences.add(new Fence(lang, body, openLine, true));
                fence = null;
                continue;
            }
            body.add(line);
        }
        if (fence != null) {
            fences.add(new Fence(lang, body, openLine, false));
        }
        return fences;
    }

    private static String dedent(List<String> lines) {
        var indent = Integer.MAX_VALUE;
        for (var line : lines) {
            if (!line.isBlank()) {
                indent = Math.min(indent, line.length() - line.stripLeading().length());
            }
        }
        var out = new ArrayList<String>();
        for (var line : lines) {
            out.add(line.isBlank() ? "" : line.substring(indent));
        }
        return String.join("\n", out).stripTrailing();
    }

    // --- rules --------------------------------------------------------------------------------

    /**
     * Every event is named in backticks on some page, has a catalog row whose type equals its category, and has a
     * {@code ## <EventClassName>} section on a grouped page under {@code events/} other than the catalog: the
     * README's hosted anchors point at those headings.
     */
    static List<String> checkEvents(Map<String, String> events, List<Page> pages) {
        var failures = new ArrayList<String>();
        var catalog = pages.stream().filter(p -> p.path().equals(CATALOG_PAGE)).findFirst().map(Page::text).orElse(null);
        if (catalog == null) {
            failures.add("Event catalog page missing: " + CATALOG_PAGE);
        }
        var rows = new HashMap<String, String>();
        var m = CATALOG_ROW.matcher(catalog == null ? "" : catalog);
        while (m.find()) {
            if (rows.putIfAbsent(m.group(1), m.group(2)) != null) {
                failures.add("Event catalog row duplicated: " + m.group(1));
            }
        }
        var headings = new HashSet<String>();
        for (var page : pages) {
            if (page.path().startsWith(EVENTS_DIR) && !page.path().equals(CATALOG_PAGE)) {
                var h = EVENT_HEADING.matcher(page.text());
                while (h.find()) {
                    headings.add(h.group(1));
                }
            }
        }
        for (var entry : new TreeMap<>(events).entrySet()) {
            var name = entry.getKey();
            if (!mentioned(name, pages)) {
                failures.add("Missing event documentation: " + name);
            }
            if (!headings.contains(name)) {
                failures.add("Event section heading missing: " + name);
            }
            var type = rows.get(name);
            if (type == null) {
                failures.add("Event catalog row missing: " + name);
            } else if (!type.equals(entry.getValue())) {
                failures.add("Event catalog row is stale: " + name);
            }
        }
        return failures;
    }

    /** Every name is mentioned in backticks on some page; {@code kind} names the failure. */
    static List<String> checkNames(String kind, Collection<String> names, List<Page> pages) {
        var failures = new ArrayList<String>();
        for (var name : names) {
            if (!mentioned(name, pages)) {
                failures.add("Missing " + kind + " documentation: " + name);
            }
        }
        return failures;
    }

    /** No authored page links to Discord. */
    static List<String> checkDiscord(List<Page> pages) {
        var failures = new ArrayList<String>();
        for (var page : pages) {
            if (DISCORD.matcher(page.text()).find()) {
                failures.add(page.path() + ": published pages must not reference Discord");
            }
        }
        return failures;
    }

    /**
     * Every {@code - [onX](#onx-tiktokxevent)} item of the README's events list, whatever follows the link, names an
     * event class (the anchor's last segment, compared case-insensitively) and an {@code EventsBuilder} handler.
     */
    static List<String> checkReadme(String readme, Collection<String> builderHandlers, Collection<String> eventClasses) {
        var lower = new HashMap<String, String>();
        for (var name : eventClasses) {
            lower.put(name.toLowerCase(Locale.ROOT), name);
        }
        var failures = new LinkedHashSet<String>();
        var m = README_ITEM.matcher(eventsSection(readme));
        while (m.find()) {
            var handler = m.group(1);
            var anchor = m.group(2);
            var event = anchor.substring(anchor.lastIndexOf('-') + 1);
            if (!lower.containsKey(event.toLowerCase(Locale.ROOT))) {
                failures.add("README handler has no event class: " + handler);
            }
            if (!builderHandlers.contains(handler)) {
                failures.add("README handler is not on EventsBuilder: " + handler);
            }
        }
        return new ArrayList<>(failures);
    }

    /**
     * The README's events list: from its {@code ## Events} heading to the next level-1 or level-2 heading with another
     * title (a repeated {@code ## Events} does not end it), or the whole text when it has no such heading.
     */
    static String eventsSection(String readme) {
        var heading = EVENTS_HEADING.matcher(readme);
        if (!heading.find()) {
            return readme;
        }
        var next = NEXT_SECTION.matcher(readme);
        next.region(heading.end(), readme.length());
        return next.find() ? readme.substring(heading.start(), next.start()) : readme.substring(heading.start());
    }

    /**
     * Every hosted link in the README, {@code https://ttlj.docs.isaackogan.com/<path>#<fragment>}, names an authored
     * page ({@code <path>.md} or {@code <path>/index.md}; {@code /} is {@code index.md}) and, when it has a fragment,
     * a heading on that page whose slug equals it: lowercase, each run of non-alphanumerics one hyphen, as the README
     * anchors are written.
     */
    static List<String> checkReadmeLinks(String readme, List<Page> pages) {
        var byPath = new HashMap<String, Page>();
        for (var page : pages) {
            byPath.put(page.path(), page);
        }
        var failures = new LinkedHashSet<String>();
        var m = SITE_LINK.matcher(readme);
        while (m.find()) {
            var fragment = m.group(2);
            var url = SITE + m.group(1) + (fragment == null ? "" : "#" + fragment);
            var page = page(byPath, m.group(1).replaceAll("/+$", ""));
            if (page == null) {
                failures.add("README link has no page: " + url);
            } else if (fragment != null && !slugs(page.text()).contains(fragment)) {
                failures.add("README link has no heading: " + url);
            }
        }
        return new ArrayList<>(failures);
    }

    private static Page page(Map<String, Page> byPath, String path) {
        if (path.isEmpty()) {
            return byPath.get("index.md");
        }
        for (var candidate : List.of(path + ".md", path + ".mdx", path + "/index.md", path + "/index.mdx")) {
            var page = byPath.get(candidate);
            if (page != null) {
                return page;
            }
        }
        return null;
    }

    /** The slug of every heading on a page. */
    static Set<String> slugs(String text) {
        var slugs = new HashSet<String>();
        var m = HEADING.matcher(text);
        while (m.find()) {
            slugs.add(slug(m.group(1)));
        }
        return slugs;
    }

    /** Starlight's anchor for a heading: lowercase, each run of non-alphanumerics one hyphen, none at the ends. */
    static String slug(String heading) {
        return heading.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }

    private static boolean mentioned(String name, List<Page> pages) {
        var span = "`" + name + "`";
        for (var page : pages) {
            if (page.text().contains(span)) {
                return true;
            }
        }
        return false;
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
