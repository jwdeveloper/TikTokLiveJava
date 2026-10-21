# Starlight Documentation Site Implementation Plan (TikTokLiveJava)

**Goal:** Give the Java library a Starlight documentation site with generated API and protobuf schema references, content and SEO gates, hosted on Cloudflare Workers at `https://ttlj.docs.isaackogan.com/`, bootstrapped by the fork's own GitHub Actions run and offered upstream as a pull request.

**Architecture:** `docs/` is a self-contained pnpm project (Astro + Starlight + lucode) whose `build` script first builds and runs a standalone Maven tool in `docs/tools/` (`check`, then `generate`) and then `astro build`. The tool parses Java sources with JavaParser and the three `.proto` files with a small proto3 parser, writes Starlight Markdown, and compiles every Java example fence against the built library jars. `docs.yml` validates on pull requests, deploys on pushes to `master` when the Cloudflare secret exists, and on a daily schedule deploys from a checkout of upstream `master`. The Python sibling (the `docs/` directory of the TikTokLive repository) is the reference implementation for everything web-side; copy its structure and prose register.

**Tech Stack:** Java 17 in CI (JDK 21 locally; library target 16), Maven, JavaParser 3.26.x, JUnit 5, `javax.tools.JavaCompiler`; Node 24, pnpm 10.34.2, astro 7.4.1, @astrojs/starlight 0.42.6, lucode-starlight 1.0.0, starlight-links-validator 0.27.0, wrangler 4.151.2; Cloudflare Workers static assets; GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-10-20-starlight-docs-design.md`

## Global Constraints

- Public hostname is exactly `ttlj.docs.isaackogan.com`; Worker name `tiktoklivejava-docs` (internal only); no other public name contains "tiktok".
- Exact pins, no ranges: `astro 7.4.1`, `@astrojs/starlight 0.42.6`, `lucode-starlight 1.0.0`, `starlight-links-validator 0.27.0`, `wrangler 4.151.2`, `@astrojs/markdown-remark 7.3.3`, `unist-util-visit 5.0.0`, `pnpm@10.34.2`, Node `>=22.22.0`; `javaparser-core 3.26.4`, `junit-jupiter 5.11.4`.
- Docs tooling lives only under `docs/`; nothing is added to the root `pom.xml` modules, `API/pom.xml`, `Client/pom.xml`, `jitpack.yml`, or `maven-publish.yml`.
- Generated content (`docs/src/content/docs/api/`, `docs/src/content/docs/schema/`, `docs/dist/`, `docs/node_modules/`, `docs/.astro/`, `docs/tools/target/`) is never committed.
- No `discord.com` / `discord.gg` link in any authored Markdown page; the only Discord link is the header social button `https://discord.gg/e2XwPNTBBr`.
- Every `eulerstream.com` link is dofollow; every built page carries at least one (footer); the homepage carries at least three with distinct anchor texts.
- Java examples use ```` ```java ```` fences; a fence with a top-level `class`/`interface`/`record`/`enum` compiles as-is, any other fence is a fragment the checker wraps; shell commands use ```` ```sh ````; build files use ```` ```xml ```` or ```` ```groovy ````.
- Internal links are site-relative with trailing slashes. Prose register matches the sibling sites: short declarative sentences, no marketing.
- Library facts come from the sources at `1.13.1-Release`: entry `TikTokLive.newClient(String hostName)` → `LiveClientBuilder` (handlers on `EventsBuilder<T>`, 73 `on*` methods); `LiveClientSettings` (16 fields), `HttpClientSettings`; 78 event classes under `API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/`, each annotated `@EventMeta(eventType = EventType.Control|Message|Debug)`; 11 classes under `api/exceptions/`; listener classes extend `TikTokEventListenerBase` with `@TikTokEventObserver` methods; extensions `TikTokLiveRecorder` (`extension-recorder`) and `TikTokLiveCollector`/`DataCollector` (`extension-collector`); proto Java package `io.github.jwdeveloper.tiktok.api.messages.webcast` from `API/src/main/proto/{webcast,data,enums}.proto` (proto3, `package TikTok`).
- JitPack coordinates: group `com.github.jwdeveloper.TikTok-Live-Java`, artifact `Client`, version `1.13.1-Release`; Java 16 or newer.
- Work on branch `docs/starlight-site` in the local checkout of the fork (origin = the fork `isaackogan/TikTokLiveJava`; add `upstream = https://github.com/jwdeveloper/TikTokLiveJava.git`). Never push to upstream; never force-push.
- Local commands: `npx -y pnpm@10.34.2` for pnpm; `mvn` 3.9.12 and JDK 21 are on PATH; build the library once with `mvn -q -pl API,Client,extension-recorder,extension-collector -am -DskipTests package` so `*/target/classes` exist for the example compiler.

## Review Focus

1. A new event class added upstream with no docs entry: the build must fail naming the class. Pinned in Task 3 (`checkEventsReportsMissingEvent`).
2. A fragment example calling a method that does not exist (for example `event.getComent()`): the build must fail with the page and example number. Pinned in Task 3 (`fragmentWithUnknownMethodFails`).
3. A nested proto message (for example a message declared inside another) must get a schema page and the links from event pages must resolve. Pinned in Task 2 (`nestedMessageGetsPage`) and Task 6 (links validator run).
4. A README handler name with no matching event class (a typo in the README list): the `readme` section must fail. Pinned in Task 3 (`readmeHandlerWithoutClassFails`).
5. The scheduled deploy must build upstream's sources, not the fork's, while using the fork's workflow: pinned in Task 11 by asserting the schedule job's checkout uses `repository: jwdeveloper/TikTokLiveJava` and `ref: master` and the push job's checkout does not.

---

### Task 1: Scaffold the Starlight project

**Files:**
- Create: `docs/package.json`, `docs/pnpm-workspace.yaml`, `docs/tsconfig.json`, `docs/astro.config.mjs`, `docs/src/content.config.ts`, `docs/src/content/docs/index.md`, `docs/src/content/docs/404.md`, placeholder pages `docs/src/content/docs/getting-started/install.md`, `getting-started/connection.md`, `events/index.md`, `troubleshooting.md`, `contributing.md`, `hosting.md`, `docs/public/favicon.png`, `docs/public/robots.txt`
- Modify: `.gitignore`

**Interfaces:**
- Produces: `pnpm --dir docs build` = `pnpm run tools:build && pnpm run check && pnpm run generate && astro build`, with `tools:build` = `mvn -q -f tools/pom.xml -DskipTests package`, `check` = `java -jar tools/target/docs-tools.jar check ..`, `generate` = `java -jar tools/target/docs-tools.jar generate ..` (the argument is the repository root relative to `docs/`). Until Tasks 2–3 exist, verify with `pnpm --dir docs exec astro build`. Sidebar fixed here:

```js
sidebar: [
  { label: 'Start here', items: [
    { label: 'Getting Started', slug: '' },
    { label: 'Install', slug: 'getting-started/install' },
    { label: 'Connection lifecycle', slug: 'getting-started/connection' },
  ] },
  { label: 'Configuration', items: [{ autogenerate: { directory: 'configuration' } }] },
  { label: 'Guides', items: [{ autogenerate: { directory: 'guides' } }] },
  { label: 'Events', items: [{ autogenerate: { directory: 'events' } }], collapsed: true },
  { label: 'Advanced', items: [{ autogenerate: { directory: 'advanced' } }], collapsed: true },
  { label: 'Troubleshooting', slug: 'troubleshooting' },
  { label: 'Contributing to the docs', slug: 'contributing' },
  { label: 'Hosting', slug: 'hosting' },
  { label: 'API reference', items: [{ autogenerate: { directory: 'api' } }], collapsed: true },
  { label: 'Protobuf schema', items: [{ autogenerate: { directory: 'schema' } }], collapsed: true },
],
```

- [ ] **Step 1: Copy the Python sibling's `docs/` web files and adapt**

From the Python TikTokLive repository's `docs/` copy `astro.config.mjs`, `tsconfig.json`, `pnpm-workspace.yaml`, `src/content.config.ts`, `src/content/docs/404.md`, `public/favicon.png`. In `astro.config.mjs` set `site` default `https://ttlj.docs.isaackogan.com`, `title: 'TikTokLiveJava'`, `description: 'Unofficial TikTok LIVE API for Java. Guides, events, and generated API reference.'`, social GitHub `https://github.com/jwdeveloper/TikTokLiveJava` and Discord `https://discord.gg/e2XwPNTBBr`, `editLink.baseUrl` `https://github.com/jwdeveloper/TikTokLiveJava/edit/master/docs/`, lucode `navLinks` Docs/Events/API, `footerText` `'Unofficial project, not affiliated with TikTok or ByteDance Ltd. Signing by [Euler Stream](https://www.eulerstream.com/). Source on [GitHub](https://github.com/jwdeveloper/TikTokLiveJava).'`, `head` entries `og:image` `https://raw.githubusercontent.com/jwdeveloper/TikTokLiveJava/master/tools-readme/src/main/resources/logo.svg` with `og:image:alt` `TikTokLiveJava, TikTok LIVE API for Java` and `twitter:card` `summary_large_image`, and the sidebar above. `package.json`: name `tiktoklivejava-docs`, scripts `dev`, `tools:build`, `check`, `generate`, `build`, `preview`, `deploy:cloudflare` (`wrangler deploy`), devDependencies pinned per Global Constraints. `public/robots.txt`: `User-agent: *`, `Allow: /`, blank line, `Sitemap: https://ttlj.docs.isaackogan.com/sitemap-index.xml`.

- [ ] **Step 2: Placeholder pages and `.gitignore`**

`index.md` title `Getting Started` with one paragraph; the other placeholders are frontmatter plus the sentence "This page is written in a later task of the documentation plan." Append to `.gitignore`: `docs/node_modules/`, `docs/.astro/`, `docs/dist/`, `docs/src/content/docs/api/`, `docs/src/content/docs/schema/`, `docs/tools/target/`. Create gitignored placeholder `api/index.md` and `schema/index.md` (title only) so the autogenerated groups exist. Add the `upstream` git remote.

- [ ] **Step 3: Install and build**

Run: `npx -y pnpm@10.34.2 --dir docs install && npx -y pnpm@10.34.2 --dir docs exec astro build 2>&1 | tail -3`
Expected: `All internal links are valid.`; `docs/dist/index.html` has `<title>Getting Started | TikTokLiveJava</title>`; `grep -c 'discord.gg/e2XwPNTBBr' docs/dist/index.html` is 1; `docs/dist/robots.txt` present.

- [ ] **Step 4: Commit**

```bash
git add -A docs .gitignore
git commit -m "docs: scaffold the Starlight documentation site"
```

### Task 2: Docs tool, `generate` command

**Files:**
- Create: `docs/tools/pom.xml`, `docs/tools/src/main/java/docs/Main.java`, `docs/tools/src/main/java/docs/JavaSources.java`, `docs/tools/src/main/java/docs/ApiPages.java`, `docs/tools/src/main/java/docs/ProtoParser.java`, `docs/tools/src/main/java/docs/SchemaPages.java`, `docs/tools/src/main/java/docs/Markdown.java`
- Test: `docs/tools/src/test/java/docs/ApiPagesTest.java`, `docs/tools/src/test/java/docs/ProtoParserTest.java`, `docs/tools/src/test/java/docs/SchemaPagesTest.java`

**Interfaces:**
- `docs/tools/pom.xml`: standalone project (`groupId docs`, `artifactId docs-tools`, Java release 17), dependencies `com.github.javaparser:javaparser-core:3.26.4`, `org.junit.jupiter:junit-jupiter:5.11.4` (test), `maven-shade-plugin` producing `target/docs-tools.jar` with `Main-Class: docs.Main`.
- `docs.Main.main(String[] args)`: `args[0]` is `generate` or `check`, `args[1]` is the repository root; exit code 0/1.
- `JavaSources.load(Path repoRoot) -> List<CompilationUnit>` parsing every `.java` under `API/src/main/java` and `Client/src/main/java` (JavaParser, language level Java 17, Lombok annotations kept as annotations).
- `ApiPages.slug(String fqcn) -> String`: `io.github.jwdeveloper.tiktok.api.live.LiveClient` → `io/github/jwdeveloper/tiktok/api/live/liveclient`; package index pages at `<package path>/index.md`, type pages at `<package path>/<lowercase simple name>.md`; nested types get `<outer>-<inner>` names.
- `ApiPages.render(List<CompilationUnit> units, String version, String sourceBaseUrl, Path outDir) -> List<Path>`: one page per public top-level or nested type: frontmatter `title` (simple name), `description` (first Javadoc sentence or `"<kind> <fqcn>"`), `sidebar.label`; sections Signature (modifiers, kind, name, type parameters, extends, implements), Javadoc body, Fields (name, type, Javadoc, Lombok `@Getter`/`@Setter`/`@Builder` noted), Constructors, Methods (signature, parameter and return types, Javadoc `@param`/`@return`), Source (`<sourceBaseUrl>/<module>/src/main/java/<path>#L<line>`). Deterministic ordering (packages and members sorted by name). Index page title `TikTokLiveJava <version>`, version read from the root `pom.xml` `<version>`.
- `ProtoParser.parse(Path protoFile) -> ProtoFile` with records `ProtoFile(String pkg, String javaPackage, List<ProtoMessage> messages, List<ProtoEnum> enums)`, `ProtoMessage(String name, String parent, List<ProtoField> fields, List<ProtoMessage> nested, List<ProtoEnum> nestedEnums)`, `ProtoField(String label, String type, String name, int number)`, `ProtoEnum(String name, List<ProtoEnumValue> values)`. Handles proto3 `message`, `enum`, `repeated`, `optional`, `map<k,v>`, `oneof`, nested types, comments; ignores `option` lines and `reserved`.
- `SchemaPages.render(List<ProtoFile> files, String version, Path outDir) -> List<Path>`: one page per message and enum (nested ones named `<Outer>.<Inner>` with slug `<outer>-<inner>`), slug under `schema/<file stem>/<slug>.md` (`webcast/webcastchatmessage.md`), fields table (name, type with link when it is another message/enum, number, label), index page titled `TikTok Webcast schema (<version>)`.
- Consumed by Task 6 (event pages link `/schema/<file stem>/<message slug>/`) and Task 10 (nothing).

- [ ] **Step 1: Write failing tests**

```java
class ApiPagesTest {
  @Test void slugLowercasesAndNestsPackages() {
    assertEquals("io/github/jwdeveloper/tiktok/api/live/liveclient", ApiPages.slug("io.github.jwdeveloper.tiktok.api.live.LiveClient"));
  }
  @Test void liveClientSettingsPageListsFields(@TempDir Path out) {
    var units = JavaSources.load(REPO);
    var paths = ApiPages.render(units, "1.13.1-Release", "https://github.com/jwdeveloper/TikTokLiveJava/blob/master", out);
    var page = out.resolve("io/github/jwdeveloper/tiktok/api/data/settings/liveclientsettings.md");
    assertTrue(Files.exists(page));
    var md = Files.readString(page);
    assertTrue(md.startsWith("---\ntitle: LiveClientSettings\n"));
    for (var f : List.of("offline", "fetchGifts", "retryOnConnectionFailure", "pingInterval", "apiKey")) assertTrue(md.contains("`" + f + "`"), f);
    assertTrue(paths.size() > 150);
  }
  @Test void typeWithoutJavadocStillRenders(@TempDir Path out) { /* EventType enum has no Javadoc: page exists, description is "enum io.github...EventType" */ }
  @Test void outputIsDeterministic(@TempDir Path a, @TempDir Path b) { /* render twice, compare file lists and contents */ }
}
class ProtoParserTest {
  @Test void parsesMessagesEnumsAndNested() {
    var f = ProtoParser.parse(REPO.resolve("API/src/main/proto/webcast.proto"));
    assertEquals("io.github.jwdeveloper.tiktok.api.messages.webcast", f.javaPackage());
    assertTrue(f.messages().stream().anyMatch(m -> m.name().equals("WebcastChatMessage")));
    assertTrue(f.messages().stream().anyMatch(m -> !m.nested().isEmpty()));
  }
  @Test void parsesRepeatedMapAndOneof() { /* fixture string with repeated, map<string,int32>, oneof; assert labels/types */ }
}
class SchemaPagesTest {
  @Test void nestedMessageGetsPage(@TempDir Path out) {
    var files = List.of(ProtoParser.parse(REPO.resolve("API/src/main/proto/webcast.proto")), ProtoParser.parse(REPO.resolve("API/src/main/proto/data.proto")), ProtoParser.parse(REPO.resolve("API/src/main/proto/enums.proto")));
    var paths = SchemaPages.render(files, "1.13.1-Release", out);
    assertTrue(Files.exists(out.resolve("webcast/webcastchatmessage.md")));
    assertTrue(paths.stream().anyMatch(p -> p.getFileName().toString().contains("-")), "a nested type page named outer-inner");
    assertTrue(paths.size() > 300);
  }
}
```

`REPO` is `Path.of(System.getProperty("repo.root"))`, set in the surefire configuration to `${project.basedir}/../..`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd docs/tools && mvn -q test 2>&1 | tail -5`
Expected: compilation errors for the missing classes.

- [ ] **Step 3: Implement the classes per Interfaces** (JavaParser `StaticJavaParser` with `ParserConfiguration.LanguageLevel.JAVA_17`; the proto parser is a line-oriented recursive-descent parser, no grammar library).

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd docs/tools && mvn -q test 2>&1 | tail -5`
Expected: all tests pass, no warnings.

- [ ] **Step 5: Wire and build the site**

Run: `npx -y pnpm@10.34.2 --dir docs run tools:build && npx -y pnpm@10.34.2 --dir docs run generate && npx -y pnpm@10.34.2 --dir docs exec astro build 2>&1 | tail -4`
Expected: `All internal links are valid.`, page count above 500; `docs/dist/api/io/github/jwdeveloper/tiktok/api/live/liveclient/index.html` renders the signature block; the sidebar groups API pages by package directory.

- [ ] **Step 6: Commit**

```bash
git add docs/tools docs/package.json
git commit -m "docs: generate API and protobuf schema reference pages"
```

### Task 3: Docs tool, `check` command

**Files:**
- Create: `docs/tools/src/main/java/docs/ContentCheck.java`, `docs/tools/src/main/java/docs/ExampleCompiler.java`
- Test: `docs/tools/src/test/java/docs/ContentCheckTest.java`, `docs/tools/src/test/java/docs/ExampleCompilerTest.java`

**Interfaces:**
- `ContentCheck.run(Path repoRoot, PrintStream out) -> int` printing exactly these sections in order: `events`, `settings`, `builder`, `exceptions`, `extensions`, `discord`, `readme`, `examples`, each `PASS <section>` or `FAIL <section>: <first three failures joined by "; ">`, then `Checked N pages, E events, S settings fields, H handlers, X exceptions, J Java examples.`; returns 1 if any section failed.
- Extraction (JavaParser over the sources): `events(repoRoot) -> Map<String, String>` event simple name → `@EventMeta` category (`Control`/`Message`/`Debug`) for every class under `api/data/events`; `settingsFields(repoRoot) -> List<String>` from `LiveClientSettings` and `HttpClientSettings`; `handlers(repoRoot) -> List<String>` every public method named `on*` declared on `EventsBuilder` plus every public method of `LiveClient`; `exceptions(repoRoot) -> List<String>` class names under `api/exceptions`; extensions = `TikTokLiveRecorder`, `TikTokLiveCollector`.
- Page rules: authored pages are every `.md`/`.mdx` under `docs/src/content/docs` except `api/` and `schema/`; `events`: every event name appears in backticks somewhere and the catalog `events/index.md` has a row ``| `TikTokXEvent` | <Control|Message|Debug> | description |`` whose type equals the annotation; `settings`, `builder`, `exceptions`, `extensions`: each name in backticks somewhere; `discord`: no `discord.com`/`discord.gg`; `readme`: every `- [onX](#...)` item in the repository `README.md` events list names a handler that exists on `EventsBuilder` and whose event class (the heading's `TikTokXEvent`) is in `events`; `examples`: every ```` ```java ```` fence (any indentation, dedented) compiles.
- `ExampleCompiler.compile(List<Example> examples, Path repoRoot) -> List<String>` where `Example(String label, String code)`; a fence containing a top-level type declaration (regex `^(public\s+)?(final\s+)?(class|interface|record|enum)\s+\w+` on a dedented line) compiles as-is in its own file named after the first declared type; any other fence is wrapped as `import io.github.jwdeveloper.tiktok.*; import io.github.jwdeveloper.tiktok.api.*; …` (one wildcard import per package found under `API` and `Client` sources, plus `java.util.*`, `java.time.*`, `java.util.function.*`, `java.util.logging.*`) around `public class Example<N> { void run() throws Exception { <body> } }`; compiled with `ToolProvider.getSystemJavaCompiler()`, `--release 17`, classpath = `API/target/classes`, `Client/target/classes`, `extension-recorder/target/classes`, `extension-collector/target/classes` plus every jar under `~/.m2` that those modules depend on, resolved by running `mvn -q -pl API,Client,extension-recorder,extension-collector dependency:build-classpath -Dmdep.outputFile=docs/tools/target/classpath.txt` from `tools:build` (the `check` command reads that file; failing when it is missing). A canary fragment `TikTokLive.newClient("x").onComment((c, e) -> e.getComent());` is always compiled and must fail, else `examples` reports `example compiler canary not flagged`.

- [ ] **Step 1: Write failing tests**

```java
class ContentCheckTest {
  @Test void eventsMapIncludesCategories() { var e = ContentCheck.events(REPO); assertEquals("Message", e.get("TikTokCommentEvent")); assertEquals("Control", e.get("TikTokConnectedEvent")); assertTrue(e.size() >= 78); }
  @Test void settingsHandlersExceptions() { assertTrue(ContentCheck.settingsFields(REPO).contains("retryConnectionTimeout")); assertTrue(ContentCheck.handlers(REPO).contains("onComment")); assertEquals(11, ContentCheck.exceptions(REPO).size()); }
  @Test void checkEventsReportsMissingEvent(@TempDir Path site) { /* one page mentioning `TikTokCommentEvent` only; events map {TikTokCommentEvent→Message, TikTokNewShinyEvent→Message} → failure "Missing event documentation: TikTokNewShinyEvent" */ }
  @Test void catalogRowTypeMustMatchAnnotation() { /* row "| `TikTokCommentEvent` | Control | x |" vs Message → "Event catalog row is stale: TikTokCommentEvent" */ }
  @Test void readmeHandlerWithoutClassFails() { /* README text "- [onComent](#oncoment-tiktokcomentevent)" → "README handler has no event class: onComent" */ }
  @Test void discordLinkFails() { /* "https://discord.gg/abc" → "t.md: published pages must not reference Discord" */ }
}
class ExampleCompilerTest {
  @Test void fragmentCompiles() { assertTrue(ExampleCompiler.compile(List.of(new Example("p.md, example 1", "TikTokLive.newClient(\"user\").onComment((client, event) -> System.out.println(event.getText())).buildAndConnect();")), REPO).isEmpty()); }
  @Test void fragmentWithUnknownMethodFails() { var f = ExampleCompiler.compile(List.of(new Example("p.md, example 1", "TikTokLive.newClient(\"user\").onComment((client, event) -> event.getComent());")), REPO); assertEquals(1, f.size()); assertTrue(f.get(0).startsWith("p.md, example 1")); }
  @Test void fullUnitCompilesAsIs() { /* a fence declaring `public class Demo extends TikTokEventListenerBase` with a @TikTokEventObserver method compiles */ }
  @Test void canaryGuardsVacuousPass() { /* with an empty classpath file, compile returns the canary failure message */ }
}
```

- [ ] **Step 2: Run tests to verify they fail** (`cd docs/tools && mvn -q test`): compilation errors for the missing classes.

- [ ] **Step 3: Implement `ContentCheck` and `ExampleCompiler` per Interfaces; wire `check` in `Main`.**

- [ ] **Step 4: Run tests to verify they pass** (`cd docs/tools && mvn -q test`): all pass.

- [ ] **Step 5: Run the check against the scaffold**

Run: `npx -y pnpm@10.34.2 --dir docs run tools:build && npx -y pnpm@10.34.2 --dir docs run check; echo exit=$?`
Expected: exit 1 with `FAIL events`, `FAIL settings`, `FAIL builder`, `FAIL exceptions`, `FAIL extensions`, `FAIL readme`, and `PASS discord`, `PASS examples` (zero fences, canary flagged).

- [ ] **Step 6: Commit**

```bash
git add docs/tools
git commit -m "docs: add content coverage and example compilation checks"
```

### Task 4: Start here pages

**Files:** Modify `docs/src/content/docs/index.md`; create/replace `getting-started/install.md`, `getting-started/connection.md`.

**Interfaces:** Consumes README `Introduction`, `Getting started` (Maven and Gradle snippets, the three-line connect example), `examples/ConnectionExample.java`, `LiveClient`/`LiveClientBuilder`/`EventsBuilder` sources, `TikTokConnectedEvent`/`TikTokDisconnectedEvent`/`TikTokReconnectingEvent`/`TikTokErrorEvent`/`TikTokPreConnectionEvent` classes, `LiveClientSettings` reconnect fields. Produces root headings `#prerequisites`, `#install`, `#listen-to-a-livestream`, `#next-steps`; the root page carries at least three distinct Euler Stream anchors (`Euler Stream`, `TikTok LIVE signing API`, `TikTok LIVE WebSocket API`) and a Signing section naming `apiKey` and `useEulerstreamWebsocket`.

- [ ] **Step 1: Write the three pages** (Install: JitPack repository block for Maven and Gradle, both coordinates, Java 16+; Connection: `build()` vs `buildAndConnect()` vs `buildAndConnectAsync()`, `connect()`/`connectAsync()`/`disconnect()` on `LiveClient`, retry settings, the four lifecycle events, `getRoomInfo()`).
- [ ] **Step 2: Verify**: `npx -y pnpm@10.34.2 --dir docs run check | grep -E '^(PASS|FAIL) (examples|discord)'` → both PASS; `npx -y pnpm@10.34.2 --dir docs exec astro build 2>&1 | grep -E 'links are valid|error'` → valid (add one-sentence placeholders for `/configuration/client-settings/`, `/configuration/signing/`, `/events/`, `/guides/gifts/` only if linked and missing; list them in the report).
- [ ] **Step 3: Commit** `docs: write Getting Started, Install, and connection lifecycle pages`.

### Task 5: Configuration pages

**Files:** Create `configuration/client-settings.md`, `http-settings.md`, `signing.md`, `authentication.md`, `proxies-and-host.md`, `logging.md` (frontmatter `sidebar.order` 1–6 in that order).

**Interfaces:** Consumes `ContentCheck.settingsFields` (every `LiveClientSettings` and `HttpClientSettings` field must appear in backticks; get the list with `java -jar docs/tools/target/docs-tools.jar check ..` output or a one-off JShell call), `examples/CustomizationExample.java`, `ProxyExample.java`, `Client/.../websocket/euler/*` for the Euler Stream WebSocket path. Produces: `client-settings.md` has one table row per `LiveClientSettings` field (name, type, default, effect); `http-settings.md` one per `HttpClientSettings` field; `signing.md` carries two distinct Euler Stream anchors.

- [ ] **Step 1: Write the six pages.**
- [ ] **Step 2: Verify**: `check` → `PASS settings`, `PASS examples`, `PASS discord`; build links valid.
- [ ] **Step 3: Commit** `docs: write configuration pages`.

### Task 6: Event catalog and grouped event pages

**Files:** Replace `events/index.md`; create `events/control.md`, `chat-and-audience.md`, `gifts-and-goals.md`, `battles-and-cohosting.md`, `subscriptions-and-fans.md`, `room-and-display.md`, `rankings.md`, `moderation-and-access.md`, `games-and-commerce.md`, `stream-control.md`, `debugging.md` (orders 0–11).

**Interfaces:** Consumes `ContentCheck.events` (78 names with categories), the spec's grouping list (7.1), README per-event sections for descriptions, each event class's fields (Lombok getters) and, where an event wraps a proto message, the message class name for the schema link `/schema/<file stem>/<message slug>/` (find the file by `find docs/src/content/docs/schema -name '<lowercase>.md'`). Produces exactly one `## TikTokXEvent` heading per event (slug `#tiktokxevent`), catalog rows per the Task 3 contract, Java examples for `TikTokCommentEvent`, `TikTokGiftEvent`, `TikTokGiftComboEvent`, `TikTokLikeEvent`, `TikTokConnectedEvent`, `TikTokDisconnectedEvent` (fragments using `TikTokLive.newClient(...)`).

- [ ] **Step 1: Write `events/index.md`** (intro with the `onX` fluent handler and the `@TikTokEventObserver` listener forms, counts, `## Find an event` with one sub-table per page).
- [ ] **Step 2: Write the eleven grouped pages.**
- [ ] **Step 3: Verify**: `check` → `PASS events`, `PASS readme`, previous sections still PASS; `generate` then `astro build` → links valid (schema links resolve).
- [ ] **Step 4: Commit** `docs: write the event catalog and grouped event pages`.

### Task 7: Guides

**Files:** Create `guides/gifts.md`, `listeners.md`, `custom-events.md`, `recorder.md`, `collector.md`, `offline-testing.md`, `debugging.md` (orders 1–7).

**Interfaces:** Consumes `examples/*.java` (one per guide), `extension-recorder` and `extension-collector` sources (`TikTokLiveRecorder`, `TikTokLiveCollector`, `DataCollector`), `TikTokEventListenerBase`, `@TikTokEventObserver`, `LiveClientSettings.offline`, `Events_And_Gifts_Testing_Example.java`. Produces `PASS extensions` (both extension class names in backticks) and at least one compiling Java example per guide (full listener classes compile as units).

- [ ] **Step 1: Write the seven guides.** - [ ] **Step 2: Verify** `check` → `PASS extensions`, `PASS examples`; build links valid. - [ ] **Step 3: Commit** `docs: write guides`.

### Task 8: Advanced, troubleshooting, contributing, hosting; full green build

**Files:** Create `advanced/http-client.md`, `advanced/websocket.md`, `advanced/protocol.md`; replace `troubleshooting.md`, `contributing.md`, `hosting.md`; create `docs/README.md`; add a `Documentation` section to `collaboration.md`.

**Interfaces:** Consumes `ContentCheck.exceptions` (all 11 exception classes in backticks on troubleshooting or advanced pages), `Client/.../http/*`, `Client/.../websocket/*`, `api/mappers/*`, the Python sibling's `troubleshooting.md`, `contributing.md`, `hosting.md`, `docs/README.md` as templates, and the plan's Tasks 9 and 11 for the hosting facts (hostname, Worker name, workflow shape, secrets, scheduled upstream checkout, SEO gate). Produces the first fully green build.

- [ ] **Step 1: Write the pages** (contributing covers: JDK 17+, Maven, Node 24, pnpm 10.34.2, `pnpm --dir docs tools:build | check | generate | build | dev`, fence rules including fragment wrapping, no Discord, Euler links, generated dirs never committed, the eight check sections, fact sources).
- [ ] **Step 2: Full build**: `npx -y pnpm@10.34.2 --dir docs build 2>&1 | grep -E '^(PASS|FAIL)|Checked|links are valid|page\(s\) built'` → all eight `PASS`, summary line, links valid, pages above 500.
- [ ] **Step 3: Commit** `docs: write advanced, troubleshooting, contributing, and hosting pages`.

### Task 9: SEO gate (Node)

**Files:** Create `docs/scripts/verify-seo.mjs`, `docs/scripts/verify-seo.test.mjs`; add `package.json` scripts `seo` (`node scripts/verify-seo.mjs dist`) and `test` (`node --test scripts/`).

**Interfaces:** `verifySeo(buildDir) -> { passes: string[], failures: string[] }` and a CLI exiting 1 on failures; `BASE_URL = 'https://ttlj.docs.isaackogan.com/'`; checks identical to the Python sibling's `scripts/seo/verify_seo.py`: homepage `<title>` contains `TikTok LIVE API`; every page (`**/index.html` plus `404.html`) has `rel="canonical"` with its own URL (`BASE_URL + dir + '/'`, `404.html` → `404/`); homepage `og:title`, `og:description`, `og:image`, meta description; `sitemap-index.xml` and `sitemap-0.xml` exist, the latter uses `BASE_URL` with more than one `<loc>`; `robots.txt` names the sitemap; no eulerstream link has `rel="nofollow"`; every page has at least one eulerstream link; homepage has at least three distinct eulerstream anchor texts (whitespace-collapsed, lowercased); every page contains `class="sidebar-entries`.

- [ ] **Step 1: Write failing tests** (`node:test`): `gatePassesOnFixtureSite`, `failsWhenNestedPageLacksEulerLink`, `failsWhenRobotsHasNoSitemap`, `failsWhenCanonicalPointsAtAnotherPage`, with a `writeFixtureSite(dir, overrides)` helper.
- [ ] **Step 2: Run to verify they fail** (`npx -y pnpm@10.34.2 --dir docs test`). - [ ] **Step 3: Implement.** - [ ] **Step 4: Tests pass; then `npx -y pnpm@10.34.2 --dir docs run seo` on the real build prints only PASS lines.** If the homepage title check fails, add the `head` title override on `index.md` as the Python sibling does (`TikTok LIVE API for Java: TikTokLiveJava Documentation`).
- [ ] **Step 5: Commit** `docs: add the SEO gate for the built site`.

### Task 10: README

**Files:** Modify `README.md`.

**Interfaces:** Consumes event heading slugs from Task 6 (`/events/<page>/#tiktokxevent`) and `ContentCheck` `readme` section. Produces: a documentation line after the introduction and a `Documentation` entry at the top of the events/contents area pointing at `https://ttlj.docs.isaackogan.com/`; every `- [onX](#onx-tiktokxevent)` item in the events list keeps its in-page anchor and gains a trailing ` · [docs](https://ttlj.docs.isaackogan.com/events/<page>/#tiktokxevent)` link; each `## onX [TikTokXEvent](<github source url>)` heading's source URL gains the missing `api/` path segment (they 404 today) and a following line `Documentation: https://ttlj.docs.isaackogan.com/events/<page>/#tiktokxevent`; no relative links remain. Structure and copy otherwise unchanged.

- [ ] **Step 1: Edit.** - [ ] **Step 2: Verify** with a one-off audit (every `ttlj.docs.isaackogan.com` link maps to `docs/dist/<path>/index.html` containing `id="<anchor>"`; every GitHub source link resolves via `gh api repos/jwdeveloper/TikTokLiveJava/contents/<path>` returning 200; `check` → `PASS readme`). - [ ] **Step 3: Commit** `docs: point the README at the hosted documentation`.

### Task 11: Worker config and workflow

**Files:** Create `docs/wrangler.jsonc`, `.github/workflows/docs.yml`.

**Interfaces:**
- `wrangler.jsonc`: `name tiktoklivejava-docs`, `compatibility_date 2026-10-20`, assets `./dist`, `auto-trailing-slash`, `404-page`, `workers_dev false`, and no `routes` block (the custom domain `ttlj.docs.isaackogan.com` is already attached to the Worker and survives deploys; wrangler prints `No targets deployed`, which is expected; ruling J12). `hosting.md` states the one-time domain attachment and that the CI token is scoped to the Worker only.
- `docs.yml`: name `Documentation`; triggers `pull_request` and `push` to `master` with paths `docs/**`, `API/**`, `Client/**`, `extension-recorder/**`, `extension-collector/**`, `README.md`, `.github/workflows/docs.yml`; `workflow_dispatch`; `schedule: - cron: '17 6 * * *'`; `permissions: contents: read`; concurrency `docs-${{ github.ref }}`. Job `build` (env `DEPLOY_ENABLED: ${{ secrets.CLOUDFLARE_API_TOKEN != '' }}`): checkout, where `with.repository` is `jwdeveloper/TikTokLiveJava` and `ref: master` **only when** `github.event_name == 'schedule'` (use two checkout steps with opposite `if`s); `actions/setup-java@v4` (`temurin`, `17`, `cache: maven`); `mvn -q -pl API,Client,extension-recorder,extension-collector -am -DskipTests package`; `actions/setup-node@v4` (24); `npm install --global pnpm@10.34.2`; `pnpm --dir docs install --frozen-lockfile`; `pnpm --dir docs test`; `pnpm --dir docs build`; `pnpm --dir docs run seo`; `actions/upload-artifact@v4` (`documentation`, `docs/dist`); deploy `cloudflare/wrangler-action@v4` (`apiToken`, `accountId`, `workingDirectory: docs`, `packageManager: pnpm`, `command: deploy`) with `if: github.event_name != 'pull_request' && (github.ref == 'refs/heads/master' || github.event_name == 'schedule') && env.DEPLOY_ENABLED == 'true'`. A first job `upstream` (ubuntu-latest, output `has_docs`) whose single step sets `has_docs=true` unless `github.event_name == 'schedule'` and `gh api repos/jwdeveloper/TikTokLiveJava/contents/docs/package.json --silent` fails (env `GH_TOKEN: ${{ github.token }}`; print a `::notice::` when skipping); job `build` has `needs: upstream` and `if: needs.upstream.outputs.has_docs == 'true'` (ruling J10).
- Validation: `npx -y js-yaml .github/workflows/docs.yml > /dev/null` parses; `grep -c 'repository: jwdeveloper/TikTokLiveJava' .github/workflows/docs.yml` prints 1 and that line sits in the checkout step whose `if` is `github.event_name == 'schedule'` (the other checkout step's `if` is `github.event_name != 'schedule'` and has no `repository` input); `cd docs && npx -y pnpm@10.34.2 exec wrangler deploy --dry-run --outdir /tmp/wrangler-dry` succeeds. `grep -c "needs.upstream.outputs.has_docs == 'true'" .github/workflows/docs.yml` prints 1.

- [ ] **Step 1: Write both files.** - [ ] **Step 2: Validate as above; reconcile `hosting.md`, `contributing.md`, `docs/README.md` with the real files.** - [ ] **Step 3: Commit** `docs: deploy the site to Cloudflare Workers from GitHub Actions`.

### Task 12: Bootstrap on the fork (controller, with the maintainer)

- [ ] **Step 1: Secrets on the fork.** The Worker `tiktoklivejava-docs` already exists with the custom domain attached (created from the maintainer's wrangler login on 2026-10-20 with a placeholder page), so the maintainer creates an account-owned token scoped to Editor on that Worker. `gh secret set CLOUDFLARE_API_TOKEN -R isaackogan/TikTokLiveJava` (value via stdin) and `gh secret set CLOUDFLARE_ACCOUNT_ID -R isaackogan/TikTokLiveJava --body <account id>`. Verify with a local deploy of the current build using that token before relying on CI.
- [ ] **Step 2: Push the branch; open a fork-internal PR `docs/starlight-site` → `isaackogan:master` to get a CI run; when green, merge it with a merge commit (no squash, no force).**
- [ ] **Step 3: Watch the fork's `master` run**: `build` succeeds, `Deploy to Cloudflare Workers` ran; `curl -s -o /dev/null -w '%{http_code}' https://ttlj.docs.isaackogan.com/events/` becomes 200 within a few minutes; `/events` → 307; `/missing/` → 404.
- [ ] **Step 4: Scheduled path.** A schedule cannot be triggered by hand; the Task 11 YAML assertion is the pre-merge verification, and the first scheduled run (06:17 UTC the next day) is checked afterwards with `gh run list --workflow=docs.yml -R isaackogan/TikTokLiveJava --event schedule --limit 1`.

### Task 13: Pull request upstream (controller, with the maintainer)

- [ ] **Step 1:** `gh pr create -R jwdeveloper/TikTokLiveJava --base master --head isaackogan:docs/starlight-site --title "docs: Starlight documentation site at ttlj.docs.isaackogan.com" --body-file <summary>` describing the site, the gates, the hosting (maintained on the fork; upstream deploys only if the two secrets are added), and the README changes.
- [ ] **Step 2:** Confirm upstream's `pull_request` run validates (no deploy). Merge is the upstream maintainer's.
