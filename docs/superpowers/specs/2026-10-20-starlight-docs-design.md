# Starlight Documentation Site for TikTokLiveJava

**Date:** 2026-10-20
**Status:** Implemented (approved 2026-10-20; rulings made during implementation are noted inline)
**Upstream:** `jwdeveloper/TikTokLiveJava` (default branch `master`); the maintainer of this effort has no push rights there.
**Working fork:** `isaackogan/TikTokLiveJava` (a real GitHub fork, in sync with upstream at `81f54d7`).
**Reference implementations:** Node.js site https://ttlc.docs.isaackogan.com/ (repo `zerodytrash/TikTok-Live-Connector`, `docs/`), Python site https://ttl.docs.isaackogan.com/ (repo `isaackogan/TikTokLive`, `docs/`, design at `docs/superpowers/specs/2026-10-19-starlight-docs-design.md`).

## 1. Intent

Give the Java library the same documentation site its Node.js and Python siblings have: an Astro Starlight site with the lucode theme, a generated API reference, content-coverage checks that fail the build when the docs drift from the code, Euler Stream SEO links, hosted on Cloudflare Workers static assets under a trademark-free hostname, deployed by GitHub Actions. The work is developed on the maintainer's fork, where the first CI run creates the Cloudflare Worker, and then offered upstream as a pull request.

The maintainer's words: "do the same for the Java app, except … run the initial action on my personal remote and init the CF worker from there and then PR on JWDeveloper's branch."

## 2. Current state (verified 2026-10-20)

- Maven multi-module build: `API`, `Client`, `examples`, `tools-readme`, `extension-recorder`, `extension-collector`; Java 16 target; published through JitPack (`jitpack.yml`); version `1.13.1-Release`. Local toolchain: JDK 21, Maven 3.9.12. CI: one workflow, `maven-publish.yml` (manual dispatch, JDK 16 Corretto).
- No documentation site, no GitHub Pages (`has_pages: false`), a GitHub wiki exists. README (758 lines) is the only documentation: introduction, getting started, an events list, per-event examples, extensions, listeners, contributing. It links the Euler Stream quickstart and the Discord invite `discord.gg/e2XwPNTBBr`.
- The `tools-readme` module once generated the README (`ReadmeGenerator`, `EventsInfoGenerator`, templates under `tools-readme/src/main/resources/`), but the checked-in README differs from the generator's `output.md` by 378 diff lines: the README is hand-maintained now (the maintainer's own PR #158 revised it directly). `tools-readme` is left untouched.
- Protobuf definitions ship in the repository: `API/src/main/proto/webcast.proto`, `data.proto`, `enums.proto`; the Java message classes are generated at build time. Listener handler methods are marked with `@TikTokEventObserver`; `@EventMeta` annotates event classes.
- Public surface: entry point `TikTokLive.newClient(...)` returning a `LiveClientBuilder` with fluent `on<Event>` handlers; `LiveClientSettings` (16 fields such as `offline`, `fetchGifts`, `retryOnConnectionFailure`, `retryConnectionTimeout`, `pingInterval`, `useEulerstreamWebsocket`, `sessionId`, `apiKey`, `hostName`, `httpSettings`), `HttpClientSettings`; 78 event classes under `io.github.jwdeveloper.tiktok.api.data.events` in sub-packages `common`, `control`, `envelop`, `gift`, `http`, `link`, `poll`, `room`, `social`, `websocket`; listener base `TikTokEventListenerBase` with annotated handler methods; data models under `api.data.models` and `api.data.dto`; exceptions under `api.exceptions`; extensions (`extension-recorder`, `extension-collector`).
- Javadoc coverage is thin: 32 of 154 API source files carry a `/** */` comment.
- `ttlj.docs.isaackogan.com` has no DNS record; zone `isaackogan.com` is on Cloudflare in the maintainer's account, which already hosts the two sibling Workers.

## 3. Goals

1. A Starlight site in `docs/` mirroring the siblings' structure, theme, header (GitHub and Discord buttons), footer, favicon, Getting Started root page, and Euler Stream link profile.
2. A generated **API reference** from the Java sources of `API` and `Client` (signatures, fields, Javadoc where present, including `api.data.models` and `api.data.dto`) and a **Protobuf schema** reference generated from the three `.proto` files (every message and enum with its fields), regenerated on every build and never committed.
3. A **content check** that fails the build when any event class, `LiveClientSettings` or `HttpClientSettings` field, `LiveClientBuilder` handler method, exception class, or extension is undocumented, and that compiles every Java example in the docs against the library.
4. **Hosting** on Cloudflare Workers static assets at **https://ttlj.docs.isaackogan.com/** (my proposal; see open question 1), Worker `tiktoklivejava-docs`, deployed by `docs.yml` on pushes to `master`, validated on pull requests, gated on the `CLOUDFLARE_API_TOKEN` secret so a repository without the secret still validates.
5. **Bootstrapped from the fork:** the first deployment is performed by the workflow running in `isaackogan/TikTokLiveJava` (secrets set there), which creates the Worker and attaches the custom domain; no deploy is run from a laptop.
6. **README** links point at the hosted site, made through the `tools-readme` template if the README is still generated from it.
7. A pull request from the fork's branch to `jwdeveloper/TikTokLiveJava:master` carrying everything, with a description the upstream maintainer can act on.

## 4. Non-goals

- Changing library behaviour or adding Javadoc beyond what a generated page needs to be readable.
- Replacing or restructuring `tools-readme`; the README keeps its structure and only gains links and corrections.
- Publishing anything to JitPack or Maven Central; the docs tooling never enters the root reactor's published modules.
- Documenting the wiki or migrating it.
- Internationalisation, blog, versioned docs.

## 5. Anti-goals

- No "tiktok" in any public hostname. The Worker name is internal.
- No docs dependencies in the published artifacts: nothing enters `API/pom.xml` or `Client/pom.xml`; docs tooling lives under `docs/` with its own build.
- No generated content in Git.
- No Discord link in page text; the header button is the only Discord link.

## 6. Constraints

- Same pinned web stack as the siblings: `astro 7.4.1`, `@astrojs/starlight 0.42.6`, `lucode-starlight 1.0.0`, `starlight-links-validator 0.27.0`, `wrangler 4.151.2`, `pnpm@10.34.2`, Node 24 in CI.
- Java tooling in CI: JDK 17 (Temurin) to build the library modules with the project's Java 16 target and to run the docs tools; Maven with dependency caching.
- The upstream `maven-publish.yml` is untouched. `jitpack.yml` is untouched.
- Work on branch `docs/starlight-site` in the local checkout, pushed to the fork `origin`. Nothing is force-pushed. The upstream remote is added as `upstream` for syncing only.

## 7. Design

### 7.1 Site structure (mirrors the siblings)

- **Start here**: Getting Started (root `/`), Install (JitPack repository and coordinates for Maven and Gradle, Java 16 or newer, the two JitPack group names that both resolve), Connection lifecycle (`connect`, `connectAsync`, reconnect settings, `TikTokConnectedEvent`/`TikTokDisconnectedEvent`/`TikTokReconnectingEvent`/`TikTokErrorEvent`). There is no Migration page: the README carries no migration notes and GitHub releases are the only changelog (builder-check question 4, my call).
- **Configuration**: Client settings (`LiveClientSettings`), HTTP settings (`HttpClientSettings`), Signing (Euler Stream, `apiKey`, `useEulerstreamWebsocket`), Authentication (`sessionId`, `ttTargetIdc`), Proxies and host (`hostName`, `httpSettings`), Logging (`printToConsole`, `logLevel`).
- **Guides**: Gifts and combos (`TikTokGiftEvent`, `TikTokGiftComboEvent`), Listeners (`TikTokEventListenerBase`, annotated handlers), Custom events and mappings (`CustomEventExample`, `CustomMappingExample`), Recorder and Collector extensions, Offline testing (`offline`, `Events_And_Gifts_Testing_Example`), Debugging (`TikTokWebsocketResponseEvent`, `TikTokWebsocketUnhandledMessageEvent`, `TikTokHttpResponseEvent`).
- **Events**: catalog with one row per event class, columns ``| `TikTokXEvent` | type | description |`` where `type` is the `@EventMeta` category (`Control`, `Message`, or `Debug`) read from the class, and grouped pages **by theme using the siblings' page names** (builder-check question 1, my call; 32 of the 78 events sit directly in `api.data.events` and `link/` alone holds 23, so sub-packages cannot be the grouping): `control` (connecting, connected, disconnected, reconnecting, error, pre-connection), `chat-and-audience` (comment, emote, join, like, follow, share, question, barrage, caption), `gifts-and-goals` (gift, gift combo, goal update, envelope, chest), `battles-and-cohosting` (every `TikTokLink*` event and `TikTokLinkMic*`), `subscriptions-and-fans` (subscribe, sub notify), `room-and-display` (room info, room pin, in-room banner, header, poll events), `rankings` (rank text, rank update), `moderation-and-access` (IM delete, unauthorized member, detect, unhandled member/social/control), `games-and-commerce` (shop), `stream-control` (live ended, paused, unpaused), `debugging` (websocket message/response/unhandled, http response, empty, unhandled). Each event gets one `## TikTokXEvent` heading with description, key fields, and a short Java example where behaviour is non-obvious.
- **Advanced**: HTTP client and mappers, WebSocket layer, Protocol notes (what the library maps from TikTok's messages).
- **Troubleshooting**, **Contributing to the docs**, **Hosting**.
- **API reference** (generated), **Protobuf schema** (generated).

Header: nav links Docs, Events, API; social links GitHub (`https://github.com/jwdeveloper/TikTokLiveJava`) and Discord (open question 2). Footer: the unofficial disclaimer with an Euler Stream link and a GitHub link. Favicon: the same `favicon.png` as the siblings.

### 7.2 Generated references (my call)

A standalone Maven project `docs/tools/` (its own `pom.xml`, not a module of the root reactor) containing one Java program with two commands:

- `generate`: parses `API/src/main/java` and `Client/src/main/java` with **JavaParser** (no compilation needed) and writes Starlight Markdown: `src/content/docs/api/` with one page per public class, interface, enum, and record (signature, Javadoc summary when present, fields, constructors, methods with parameter and return types, extends/implements, source link to GitHub `blob/master/<path>#L<line>`), grouped by package directory so the sidebar groups by package. It also parses `API/src/main/proto/*.proto` (a small hand-written parser is enough for proto3 messages, enums, and nested types) and writes `src/content/docs/schema/` with one page per message and enum, titled `TikTok Webcast schema (<library version>)`. Pages are files (`<slug>.md`), package index pages are `<package>/index.md`, output is deterministic (sorted), and both directories are wiped before writing.
- `check`: the content check in 7.3.

Why JavaParser rather than a Javadoc doclet: it needs no compile of the library, tolerates the thin Javadoc, and gives full control of the Markdown layout, matching what `starlight-typedoc` and griffe produced for the siblings. The builder may switch to a doclet if JavaParser cannot resolve something the pages need, keeping the output contract.

### 7.3 Content check

`docs/tools` `check` runs before the Astro build and exits non-zero on any failing section, printing `PASS <section>` / `FAIL <section>: …` per section: `events` (every class under `api/data/events` appears in backticks on an authored page and has a catalog row whose `type` cell equals its `@EventMeta` category), `settings` (every field of `LiveClientSettings` and `HttpClientSettings`), `builder` (every public `on*` handler method declared on `EventsBuilder<T>`, the super-interface of `LiveClientBuilder`, and every public method of `LiveClient`), `exceptions` (every class under `api/exceptions`), `extensions` (both extension modules named), `discord` (no `discord.com`/`discord.gg` in authored pages), `readme` (every handler the README's events list names has a matching event class and a catalog row; the README carries no descriptions to compare), `examples` (every ```` ```java ```` fence in authored pages compiles with `javax.tools.JavaCompiler` against the `API`, `Client`, `extension-recorder`, and `extension-collector` classes: a fence containing a top-level `class`, `interface`, `record`, or `enum` declaration is compiled as-is; any other fence is a **fragment** and is wrapped in a synthetic class with wildcard imports of every `io.github.jwdeveloper.tiktok` package and its statements placed in a method body, so README-style snippets stay short (builder-check question 3, my call); a known-bad canary must fail so the gate cannot pass vacuously). `pnpm --dir docs build` runs `check`, then `generate`, then `astro build`.

### 7.4 Hosting and deployment

- `docs/wrangler.jsonc`: Worker `tiktoklivejava-docs`, `assets.directory ./dist`, `auto-trailing-slash`, `404-page`, `workers_dev false`, and no `routes` block: the custom domain `ttlj.docs.isaackogan.com` is attached to the Worker once from a maintainer's wrangler login and persists across deploys, so the CI token needs no zone permission (ruling J12, 2026-10-20).
- `.github/workflows/docs.yml`: on `pull_request` and `push` to `master` (paths `docs/**`, `API/**`, `Client/**`, `extension-*/**`, `README.md`, `tools-readme/**`, `.github/workflows/docs.yml`) and `workflow_dispatch`; JDK 17 with Maven cache; `mvn -q -pl API,Client -am -DskipTests install` so the docs tools can compile examples against the library; Node 24 and pnpm 10.34.2; `pnpm --dir docs install --frozen-lockfile`; `pnpm --dir docs build`; SEO gate; upload the artifact; deploy with `cloudflare/wrangler-action@v4` when `github.ref == 'refs/heads/master'`, the event is not a pull request, and `DEPLOY_ENABLED` (derived from the `CLOUDFLARE_API_TOKEN` secret) is true.
- **Bootstrap from the fork (superseded on 2026-10-20 by decision 4 below: the Worker and its domain were created from the maintainer's wrangler login before the bootstrap, so the fork's token is Worker-scoped from the start):** the branch is first merged into the fork's `master` (fork-local; upstream unaffected) with the secrets set on the fork, so the fork's first `master` run creates the Worker and attaches the domain. That first run needs a token that can create Workers and routes: Account → Workers Scripts → Edit, Account → Account Settings → Read, Zone → Workers Routes → Edit and Zone → Zone → Read on `isaackogan.com` (the same four-permission recipe as the Node site's first token). After the Worker exists the token can be replaced by one scoped to Editor on `tiktoklivejava-docs`, since later deploys only reconcile an existing domain.
- **After the upstream merge** (open question 3): upstream squash-merges, so the fork's `master`, which already carries the docs commits, can never fast-forward to upstream `master` (builder-check question 2). Therefore no sync-by-push exists. If the fork keeps deploying, `docs.yml` gains a `schedule` (daily) and `workflow_dispatch` path that checks out `jwdeveloper/TikTokLiveJava` at `master` with `actions/checkout` (`repository:` and `ref:` inputs; the repo is public), builds, and deploys from that checkout without pushing anything to the fork; the push-triggered path keeps deploying the fork's own `master`. If instead the upstream maintainer adds the secrets, the same workflow deploys from upstream directly and the fork's scheduled path can be disabled. No force pushes anywhere.

### 7.5 SEO

Same profile as the siblings: one sitewide dofollow Euler Stream footer link plus contextual links with distinct anchors on the Getting Started and Signing pages; `sitemap-index.xml`, `robots.txt` with the sitemap line, canonical on every page, Open Graph tags; a `docs/scripts/verify_seo.py`-equivalent is written as a Node script (`docs/scripts/verify-seo.mjs`) so the Java repo needs no Python; it asserts the same invariants as the Python gate and runs in CI after the build. No Search Console file exists for this project; the maintainer adds the property by DNS verification.

### 7.6 README

The README is hand-maintained (section 2), so it is edited directly and `tools-readme` is not touched. Changes: a documentation line and table-of-contents entry pointing at the site, every event name in the events list linked to its hosted anchor, per-event example headings linking to the hosted event pages in addition to the GitHub source link they carry today, no relative links. Structure and copy otherwise unchanged.

### 7.7 Repository changes

`docs/` (Starlight project: `astro.config.mjs`, `package.json`, `pnpm-lock.yaml`, `pnpm-workspace.yaml`, `tsconfig.json`, `wrangler.jsonc`, `public/`, `scripts/verify-seo.mjs`, `src/content/docs/**`, `README.md`), `docs/tools/` (Maven project with the generator and check plus unit tests), `.github/workflows/docs.yml`, `.gitignore` additions (`docs/node_modules/`, `docs/.astro/`, `docs/dist/`, `docs/src/content/docs/api/`, `docs/src/content/docs/schema/`, `docs/tools/target/`), README (direct edits; its per-event source links currently 404 because they lack the `api/` path segment and are corrected in passing), a `Documentation` section in `collaboration.md` (the repo's contributing file). The build of `extension-recorder` and `extension-collector` is included in CI so the Recorder and Collector guides' examples compile.

## 10. Builder-check resolutions (2026-10-20)

1. Event grouping: by theme with the siblings' page names, catalog `type` column from `@EventMeta` (7.1, 7.3).
2. Fork sync after a squash merge: scheduled checkout of upstream `master` inside `docs.yml`, no pushes (7.4).
3. Java example fences: fragments are wrapped by the checker; full compilation units compile as-is (7.3).
4. Migration page dropped in favour of an Install page (7.1).
Accepted builder defaults: extensions on the example classpath; `readme` check reduced to handler existence; handlers read from `EventsBuilder<T>`; ignore `api/` and `schema/`; README source links fixed.

## 8. Left to the builder

- JavaParser page layout details; how generics, Lombok-generated accessors (`@Getter`/`@Setter` on settings classes), and records render.
- Prose for every page, sourced from the README, `examples/`, and the sources.
- Grouping of 78 events into the ten sub-package pages and the catalog descriptions (the README describes a subset; the rest are authored from the event classes).
- The exact `check` implementation and its test fixtures.
- Whether `docs/tools` is invoked through `mvn exec:java` or a shaded jar.

## 9. Decisions (the maintainer delegated these on 2026-10-20: "decide yourself based on the most reasonable")

1. **Hostname:** `ttlj.docs.isaackogan.com`.
2. **Discord button:** the README's `https://discord.gg/e2XwPNTBBr`.
3. **Deploys after the upstream merge:** the fork keeps deploying. The docs commits are merged into the fork's `master` (a normal merge, no force push) so `docs.yml` lives on the fork's default branch; its scheduled path checks out upstream `master` and deploys from it. The upstream copy of the workflow is gated on the secret and only validates there until jwdeveloper chooses to add secrets. Until upstream carries `docs/`, a scheduled run would check out a tree with nothing to build, so the workflow's first job probes upstream for `docs/package.json` and the build job is skipped on a schedule when it is absent (ruling J10, 2026-10-20).
4. **First-deploy token (revised 2026-10-20):** the maintainer had the Worker `tiktoklivejava-docs` created from their own wrangler login with a placeholder page and the custom domain already attached, so the fork's CI only deploys to an existing Worker. The fork's `CLOUDFLARE_API_TOKEN` is therefore an account-owned token scoped to Editor on `tiktoklivejava-docs` (the same recipe as the sibling sites); no zone or account-wide permission is needed. Verified 2026-10-20: a token with Worker-only rights uploads fine but cannot reconcile a `routes` custom domain (403 on the zone lookup), which is why the repository config omits `routes` (ruling J12).
5. **PR target:** `jwdeveloper/TikTokLiveJava:master` from `isaackogan:docs/starlight-site`.
