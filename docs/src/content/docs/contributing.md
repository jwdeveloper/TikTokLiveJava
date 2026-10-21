---
title: Contributing to the docs
description: Build, check, and maintain the documentation site alongside the library source, with the authoring rules the build enforces.
---

## Run locally

The documentation is a Starlight project under `docs/`, with its own `package.json` and lockfile. The reference generator and the content check are a small Maven project under `docs/tools/` that reads the library's sources, and the content check compiles every Java example against the built library, so the site is built from a checkout of the repository with the library built.

Prerequisites:

- JDK 17 or newer and Maven. The library targets Java 16; the documentation tool compiles with `--release 17`, and the content check needs a JDK rather than a JRE for its compiler. CI uses Temurin 17.
- Node.js 24 and pnpm 10.34.2. `npx -y pnpm@10.34.2` runs the pinned pnpm without a global install.

From the repository root:

```sh
npx -y pnpm@10.34.2 --dir docs install --frozen-lockfile
npx -y pnpm@10.34.2 --dir docs build
```

| Command | What it does |
| --- | --- |
| `pnpm --dir docs lib:build` | Builds the `API`, `Client`, `extension-recorder`, and `extension-collector` modules and writes each module's classpath to `target/classpath.txt`: `mvn -q -pl API,Client,extension-recorder,extension-collector -am -DskipTests install dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`. About a minute; the example compiler needs it. |
| `pnpm --dir docs tools:build` | Packages the documentation tool as `docs/tools/target/docs-tools.jar`. |
| `pnpm --dir docs check` | Runs the tool's `check` command: the content check described below. Needs both builds above. |
| `pnpm --dir docs generate` | Runs the tool's `generate` command: writes the `api/` and `schema/` reference pages. |
| `pnpm --dir docs build` | `lib:build`, `tools:build`, `check`, `generate`, then `astro build` with link validation. This is what CI runs. |
| `pnpm --dir docs dev` | Starts the Astro dev server on `127.0.0.1`. Run `generate` once first so the reference sections exist. |
| `pnpm --dir docs preview` | Serves the last build from `docs/dist`. |
| `pnpm --dir docs test` | The SEO gate's own tests, with `node --test`. |
| `pnpm --dir docs run seo` | The SEO gate over `docs/dist`, see [hosting](/hosting/#seo-gate). |
| `mvn -q -f docs/tools/pom.xml test` | The documentation tool's JUnit tests: the generator, the proto parser, the content check, and the example compiler. |

With `npx` in place of a global pnpm: `npx -y pnpm@10.34.2 --dir docs check`.

## Where to edit

- Authored pages are Markdown under `docs/src/content/docs/`: the start-here pages, `configuration/`, `guides/`, `events/`, `advanced/`, and the top-level pages.
- `api/` and `schema/` inside that directory are generated on every build from the `API` and `Client` sources and the `.proto` files, and are ignored by Git. Never edit or commit them. To change a reference page, edit the Javadoc or the signature at the source declaration; to change a schema page, edit the `.proto` file.
- The event catalog on `events/index.md` and the grouped event pages are authored. A catalog row's type cell is compared with the event class's `@EventMeta` category, and the README's events list is compared with the `EventsBuilder` handlers, so a new event needs its class, its handler, its catalog row, and its section.
- The sidebar, the site title and description, the header links, and the footer are in `docs/astro.config.mjs`.
- The tool is in `docs/tools/src/main/java/docs/`, with tests in `docs/tools/src/test/java/docs/`: `ApiPages` and `SchemaPages` generate, `ContentCheck` and `ExampleCompiler` check.

## Authoring rules

The build enforces these; a page that breaks one fails `pnpm --dir docs build`.

- Every ```` ```java ```` fence compiles against the built library. A fence that declares a top-level `class`, `interface`, `record`, or `enum` is compiled as its own file with the imports it carries, so a listener class or a `main` example is written complete. Any other fence is a fragment: its statements are placed in a method body, `void run() throws Exception`, with a wildcard import of every package of the `API` and `Client` sources and of `java.util`, `java.time`, `java.util.function`, `java.util.logging`, and `java.io`, and `import` lines inside the fragment are hoisted above the class. The generated protobuf packages are not wildcard-imported, so a fragment that names `WebcastChatMessage` or another message class carries its own `import` line. Checked exceptions need no handling in a fragment; a lambda parameter must not reuse the name of a local variable. A snippet that should not compile goes in prose or in another fence language.
- Shell commands use ```` ```sh ```` fences; Maven coordinates use ```` ```xml ````, Gradle ```` ```groovy ````. Only Java fences are compiled.
- No Discord links in page text. The header button is the one Discord link on the site, and the report channel named on pages is GitHub issues.
- Links to [Euler Stream](https://www.eulerstream.com/) are plain Markdown links, dofollow, with no `rel` attribute. Every page carries at least one; the SEO gate described on the [hosting page](/hosting/) checks it.
- Internal links are site-relative with a trailing slash, `/configuration/signing/`, and point at pages that exist; the build validates them. Event anchors are the lowercase class name on the grouped page, `/events/gifts-and-goals/#tiktokgiftevent`.
- Frontmatter has `title` and `description`; pages in an autogenerated sidebar group also set `sidebar.order`.
- An event, settings field, handler, exception, or extension name counts as documented only as its own code span: `` `onComment` `` counts, `` `client.onComment(...)` `` does not.

## Where facts come from

Describe the library as it is in the checkout, not as the README or an older page says it was. In order:

1. The library source: `API/src/main/java` for the interfaces, events, settings, and exceptions, `Client/src/main/java` for the client, the HTTP and WebSocket layers, and the mappers, and `API/src/main/proto` for the schema. A signature, a default, an exception, or a published event is what the code does.
2. The README, for the event descriptions and the wording of the landing page.
3. `examples/src/main/java`, which shows the intended use of each feature; where an example disagrees with the code, the code wins and the guide says so.

When a behaviour cannot be read from the code, run it against a live room and say what was observed rather than what the schema allows; see [protocol](/advanced/protocol/#schema-definitions-and-verified-behaviour). When the code has a bug, describe the behaviour as it is and work around it in the example, as the [troubleshooting page](/troubleshooting/#known-limitations-in-this-release) does for this release.

## The content check

`check` runs first in every build. It parses the `API` and `Client` sources with JavaParser, reads the authored pages, skips `api/` and `schema/`, and prints `PASS` or `FAIL` for eight sections in this order, then a `Checked ... pages` summary line:

| Section | Passes when |
| --- | --- |
| `events` | Every concrete event class under `api/data/events` is named in backticks on some page, has a catalog row on `events/index.md` whose type cell equals its `@EventMeta` category (`Control`, `Message`, `Debug`, or `none`), and has a `## <EventClassName>` section on a grouped page under `events/` other than the catalog, which is what the README's hosted anchors point at. |
| `settings` | Every field of `LiveClientSettings` and `HttpClientSettings` is named in backticks on some page. |
| `builder` | Every public `on*` handler of `EventsBuilder` and every public method of `LiveClient` is named in backticks on some page. |
| `exceptions` | Every class under `api/exceptions` is named in backticks on some page. |
| `extensions` | `TikTokLiveRecorder` and `TikTokLiveCollector` are named in backticks on some page. |
| `discord` | No page contains a Discord link domain. |
| `readme` | Every `- [onX](#anchor)` item of the README's events list names an event class, the anchor's last segment, and an `EventsBuilder` handler; and every `https://ttlj.docs.isaackogan.com/...` link in the README resolves to an authored page and, with a fragment, to a heading on it whose slug (lowercase, non-alphanumerics as hyphens) equals the fragment. |
| `examples` | Every Java fence compiles as described above, and no fence on any page is left unclosed; a deliberately wrong canary must fail, so an empty classpath cannot pass the section. |

A failing section prints its first three failures with the offending name. The check tells you what is missing; it cannot tell whether a sentence is true, so verify behaviour in the source before describing it.

## Deployment

`.github/workflows/docs.yml` runs the tests, the full build, and the SEO gate on every pull request that touches the docs or the library, uploads the built site as a review artifact, and on pushes to `master` redeploys the existing `tiktoklivejava-docs` Worker at `https://ttlj.docs.isaackogan.com/` where the repository holds a token scoped to that Worker; a daily scheduled run, where the deploy secrets exist, builds and deploys the upstream repository's `master` once it carries `docs/`, and until then its `upstream` probe job skips the build. See [hosting](/hosting/) for the Worker configuration, the one-time domain attachment, the secrets, and how to deploy from a checkout.

`DOCS_SITE` sets the public origin and defaults to the custom domain; `DOCS_BASE` sets the deployment subpath and defaults to `/`. Neither needs to be set for a normal build.
