# Documentation

The site uses Astro Starlight, with reference pages generated from the library sources and the `.proto` files by the Maven tool under `tools/`, and internal links validated by Starlight Links Validator. Its Node dependencies are isolated from the library build.

Use JDK 17 or newer, Maven, Node.js 24, and pnpm 10.34.2 (`npx -y pnpm@10.34.2` runs the pinned version without a global install). From the repository root:

```sh
npx -y pnpm@10.34.2 --dir docs install --frozen-lockfile
pnpm --dir docs lib:build
pnpm --dir docs tools:build
pnpm --dir docs generate
pnpm --dir docs dev
```

`lib:build` builds the `API`, `Client`, `extension-recorder`, and `extension-collector` modules and writes their classpaths (`mvn -q -pl API,Client,extension-recorder,extension-collector -am -DskipTests install dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`); the content check compiles every Java example against them. `tools:build` packages the tool as `tools/target/docs-tools.jar`.

Validate and build:

```sh
pnpm --dir docs check
pnpm --dir docs build
pnpm --dir docs preview
pnpm --dir docs test
mvn -q -f docs/tools/pom.xml test
```

Authored pages live in `src/content/docs/`. The `api/` and `schema/` directories there are generated on every build from the `API` and `Client` sources and from `API/src/main/proto`; they are ignored by Git. Do not edit the generated Markdown; change the Javadoc at the source declaration or the `.proto` file instead.

`check` runs the tool's content check, which compares the pages with the code in eight sections (`events`, `settings`, `builder`, `exceptions`, `extensions`, `discord`, `readme`, `examples`) and compiles every Java fence: a fence with a top-level type declaration as its own file, any other fence as a fragment wrapped in a method with wildcard imports of the library packages. `generate` writes the reference pages. `build` runs `lib:build`, `tools:build`, `check`, `generate`, then `astro build`, which validates links and builds the search index.

Facts come from the library source first, then the README, then `examples/`. Java fences must compile, shell commands use `sh` fences, pages carry no Discord links, links to Euler Stream are plain Markdown links, and the generated directories are never committed. See the site's contributing page for the full rules.

## Hosting

The site is served from Cloudflare Workers static assets at <https://ttlj.docs.isaackogan.com/>. `wrangler.jsonc` in this directory declares the asset directory and the HTML handling for the `tiktoklivejava-docs` Worker; there is no Worker script and no `routes` block, because a maintainer attached the custom domain to the Worker once and it survives deploys. The GitHub workflow `.github/workflows/docs.yml` validates every pull request, runs the SEO gate (`pnpm --dir docs run seo`, which runs `scripts/verify-seo.mjs` over `dist`), and on pushes to `master` redeploys the existing Worker when the repository has the `CLOUDFLARE_API_TOKEN` secret, a token scoped to Editor on that Worker only; the deploy also needs `CLOUDFLARE_ACCOUNT_ID`. A daily scheduled run checks out the upstream repository's `master` and deploys it, so the hosted site tracks upstream from the fork that holds the secrets, and a change pushed only to the fork's `master` is overwritten by it until merged upstream; until upstream carries `docs/`, the workflow's `upstream` job detects that (a `404` from the GitHub API; any other failure fails the job) and the build is skipped, as it is for a scheduled run in a repository without the deploy secrets.

To deploy from a checkout after the installs above:

```sh
pnpm --dir docs build
pnpm --dir docs run deploy:cloudflare
```

Wrangler uploads to the existing Worker and prints `No targets deployed`, which is expected: the config declares no routes and the custom domain stays attached. See [Hosting](src/content/docs/hosting.md) for the configuration, the one-time domain attachment, the secrets, and the verification steps. `DOCS_SITE` defaults to the custom domain and `DOCS_BASE` to `/`; neither needs to be set.
