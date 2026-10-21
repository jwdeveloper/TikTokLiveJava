---
title: Hosting
description: Host the static documentation on Cloudflare Workers static assets under a custom domain, deploy it from CI or a checkout, track the upstream repository on a schedule, and gate deploys on SEO checks.
---

The site is deployed to Cloudflare Workers as static assets and served at `https://ttlj.docs.isaackogan.com/`. Hosting lives in a maintainer's Cloudflare account while the documentation source stays in the public repository: the fork `isaackogan/TikTokLiveJava` holds the deployment secrets and runs the deploys, and a scheduled run there builds and deploys the upstream `jwdeveloper/TikTokLiveJava` `master` every day once upstream carries `docs/` (until then the run checks and skips), so the site tracks upstream without the upstream repository holding any credentials. The same workflow in the upstream repository validates every pull request, skips its scheduled run while it has no deploy secrets, and would deploy too if its maintainer added them. There is no Worker script: `docs/wrangler.jsonc` only declares the asset directory and the HTML handling; the custom domain is attached to the Worker once by a maintainer, not by the config, so CI deploys with a token scoped to that Worker alone. The site needs no Cloudflare Astro adapter and no TikTok or signing credentials; its only external link requirement is the dofollow links to [Euler Stream](https://www.eulerstream.com/) the SEO gate checks.

## Configuration

```jsonc
{
  "$schema": "node_modules/wrangler/config-schema.json",
  "name": "tiktoklivejava-docs",
  "compatibility_date": "2026-10-20",
  "assets": {
    "directory": "./dist",
    "html_handling": "auto-trailing-slash",
    "not_found_handling": "404-page"
  },
  // The custom domain ttlj.docs.isaackogan.com is attached to the Worker once by a maintainer
  // and survives deploys, so no routes are declared here and the CI token needs no zone permission.
  "workers_dev": false
}
```

- `html_handling: auto-trailing-slash` matches Astro's `trailingSlash: 'always'`. A request for `/events` redirects to `/events/`, which serves `events/index.html`.
- `not_found_handling: 404-page` serves Starlight's generated `404.html` with a 404 status.
- `workers_dev: false` keeps the site off the `workers.dev` hostname so the custom domain is the only public origin.
- There is no `routes` block. A maintainer created the Worker `tiktoklivejava-docs` from their own wrangler login and attached the custom domain `ttlj.docs.isaackogan.com` to it once, in the Cloudflare dashboard under the Worker's **Settings → Domains & Routes** or with a one-off `wrangler deploy` from a logged-in session using a `routes` entry in a local copy of the config; Cloudflare created the DNS record and the certificate then, and the attachment survives every later deploy. Because the config declares no routes, a deploy prints `No targets deployed`, which is expected, and the CI token needs no zone permission: a token with Worker-only rights uploads the site but gets a 403 reconciling a `routes` custom domain, which is why the entry is left out. The `isaackogan.com` zone is in the same Cloudflare account as the Worker.

`DOCS_SITE` overrides the canonical origin and defaults to the custom domain. `DOCS_BASE` sets the deployment subpath and defaults to `/`. Neither needs to be set for a production build.

See Cloudflare's [static assets](https://developers.cloudflare.com/workers/static-assets/) and [custom domains](https://developers.cloudflare.com/workers/configuration/routing/custom-domains/) documentation for the underlying behaviour.

## Build inputs

The build needs both toolchains, because the reference pages are generated from the library sources and the examples are compiled against the built library:

- JDK 17 or newer and Maven; `pnpm --dir docs build` runs the Maven build of the four library modules itself.
- Node.js 24 and pnpm 10.34.2 with `pnpm --dir docs install --frozen-lockfile`.

The [contributing page](/contributing/) has the full setup and the commands.

## Deploy from a checkout

Wrangler is pinned in `docs/package.json`, so the documentation install provides it. Run `pnpm --dir docs exec wrangler login` once on a new machine; the login is an OAuth session in the maintainer's account.

```sh
pnpm --dir docs install --frozen-lockfile
pnpm --dir docs build
pnpm --dir docs run deploy:cloudflare
```

The Worker and its custom domain already exist, so a deploy only uploads the files that changed to the existing Worker. Wrangler reports the upload and then `No targets deployed`, because the config declares no routes; the custom domain stays attached and keeps serving the new build. `pnpm --dir docs exec wrangler deploy --dry-run --outdir /tmp/wrangler-dry` validates the config and the asset directory without uploading.

## Continuous deployment

`.github/workflows/docs.yml`, named `Documentation`, runs on pull requests and on pushes to `master` that touch `docs/`, `API/`, `Client/`, `extension-recorder/`, `extension-collector/`, `README.md`, or the workflow itself; it can be started by hand with `workflow_dispatch`; and it runs on a schedule, `17 6 * * *`, 06:17 UTC every day. Its permissions are `contents: read`, and one run at a time per ref: the concurrency group is `docs-${{ github.ref }}`, with `cancel-in-progress` only for pull requests, so a new push to a pull request replaces the running check, while `master` pushes and scheduled runs, which share the `refs/heads/master` group, queue behind each other and no deploy is cancelled mid-upload.

The workflow has two jobs. The first, `upstream`, has a single step, `Check whether upstream carries the documentation`, and a job variable `DEPLOY_ENABLED` derived from whether the `CLOUDFLARE_API_TOKEN` secret is set. On every event other than the schedule the step sets the job output `has_docs` to `true`. On a scheduled run it first looks at `DEPLOY_ENABLED`: where the secret is absent it prints the notice `The Cloudflare deploy secrets are absent; skipping the scheduled build, which exists only to deploy.` and sets `has_docs` to `false`, so the upstream repository's own daily run does not build a site it cannot deploy. Otherwise it requests `https://api.github.com/repos/jwdeveloper/TikTokLiveJava/contents/docs/package.json` with the run's own `GITHUB_TOKEN` and reads the HTTP status: `200` sets `has_docs` to `true`; `404` prints the notice `jwdeveloper/TikTokLiveJava has no docs/package.json yet; skipping the scheduled build.` and sets it to `false`; any other status, or a request that fails, prints an `::error::` annotation and fails the job, so a rate limit or an outage is a red run rather than a silently skipped deploy. The `build` job has `needs: upstream` and runs only when `has_docs` is `true`, so until the documentation is merged upstream a scheduled run on the fork is green with `upstream` succeeded, `build` skipped, the notice in the run's annotations, and nothing deployed.

The `build` job has one of two checkout steps: `Check out upstream master` on a scheduled run, which checks out `jwdeveloper/TikTokLiveJava` at `master`, and `Check out the repository` on every other event, which checks out the repository and ref that triggered it. It then sets up Temurin JDK 17 with the Maven cache; `Build the library` runs `mvn -q -pl API,Client,extension-recorder,extension-collector -am -DskipTests package`; it sets up Node.js 24; `Install pnpm` runs `npm install --global pnpm@10.34.2`; `Install documentation dependencies` runs `pnpm --dir docs install --frozen-lockfile`; `Test the documentation scripts` runs `pnpm --dir docs test`; `Validate and build documentation` runs `pnpm --dir docs build`; `Verify SEO invariants` runs the SEO gate `pnpm --dir docs run seo`; and `Upload site for review` uploads `docs/dist` as the `documentation` artifact. The last step, `Deploy to Cloudflare Workers`, runs `cloudflare/wrangler-action@v4` (`workingDirectory: docs`, `packageManager: pnpm`, `command: deploy`) when the event is not a pull request, the ref is `refs/heads/master` or the event is the schedule, and `DEPLOY_ENABLED` is `true`; the `build` job derives that variable the same way as `upstream`. The deploy redeploys the existing Worker and needs both of these secrets:

| Secret | Value |
| --- | --- |
| `CLOUDFLARE_API_TOKEN` | An account-owned token scoped to Editor on the `tiktoklivejava-docs` Worker only. It can upload to that Worker and nothing else: no Worker creation, no zone or route permission, which the config does not need. |
| `CLOUDFLARE_ACCOUNT_ID` | The hosting account's id, shown by `wrangler whoami`. |

Without `CLOUDFLARE_API_TOKEN` the deploy step is skipped and the workflow still validates the build on pull requests, pushes, and manual runs, so the upstream repository, forks, and pull requests are unaffected; only the scheduled run is skipped there, as described above. The gate checks only the token; `CLOUDFLARE_ACCOUNT_ID` is passed to wrangler and is required for the deploy to succeed, so set both. The maintainer sets them with `gh secret set CLOUDFLARE_API_TOKEN -R isaackogan/TikTokLiveJava` and `gh secret set CLOUDFLARE_ACCOUNT_ID -R isaackogan/TikTokLiveJava`. Because the scheduled run checks out the upstream repository, the workflow file must be on the fork's default branch for the schedule to exist, and a change to the documentation reaches the site either by being merged upstream and picked up by the next scheduled run, or by being pushed to the fork's `master`. Both deploy to the same Worker, so the two sources are not independent: a change pushed to the fork's `master` but not merged upstream is overwritten by the next scheduled run at 06:17 UTC, which deploys upstream `master`, and stays overwritten until it is merged upstream.

## SEO gate

`pnpm --dir docs run seo` runs `docs/scripts/verify-seo.mjs` over `docs/dist` after every build in CI and prints a `PASS` line per check; any failure fails the workflow. The checks are the invariants the sibling documentation sites carry: the homepage `<title>` contains `TikTok LIVE API`; every page, `404.html` included, has a `rel="canonical"` link to its own URL on `https://ttlj.docs.isaackogan.com/`; the homepage has `og:title`, `og:description`, `og:image`, and a meta description; `sitemap-index.xml` and `sitemap-0.xml` exist, the latter on the custom domain with more than one URL; `robots.txt` names the sitemap; no link to Euler Stream is nofollowed, every page has at least one, and the homepage has at least three distinct anchor texts; and every page carries the theme's sidebar marker, so a theme change that drops the navigation is caught. The gate's tests run with `pnpm --dir docs test`.

The Open Graph and Twitter card tags come from `head` in `docs/astro.config.mjs`; `robots.txt` is in `docs/public/`; the sitemap is generated from `site`.

## Verify a deployment

```sh
curl -I https://ttlj.docs.isaackogan.com/events/
curl -I https://ttlj.docs.isaackogan.com/events
curl -I https://ttlj.docs.isaackogan.com/missing/
```

Expect a 200, a 307 redirect to the trailing-slash form, and a 404 in that order. Then open the site, check an event page and the generated API and schema pages, and search for a gift event. The canonical link in the page head must use the custom domain. A scheduled run shows up as `gh run list --workflow=docs.yml -R isaackogan/TikTokLiveJava --event schedule --limit 1`, and `gh run view <run-id> -R isaackogan/TikTokLiveJava` lists its jobs: while upstream has no `docs/`, `upstream` succeeded and `build` skipped, with the notice `jwdeveloper/TikTokLiveJava has no docs/package.json yet; skipping the scheduled build.` in the annotations (in a repository without the deploy secrets the notice reads `The Cloudflare deploy secrets are absent; skipping the scheduled build, which exists only to deploy.` instead); once it does, both jobs succeed and the `Deploy to Cloudflare Workers` step ran. A scheduled run whose `upstream` job failed could not get an answer from the GitHub API; its error annotation carries the HTTP status, and the next day's run tries again.

## Changing the hostname

Attach the new hostname to the Worker the same way the current one was attached: under the Worker's **Settings → Domains & Routes** in the Cloudflare dashboard, or with a one-off `wrangler deploy` from a logged-in maintainer session using a `routes` entry in a local copy of the config; Cloudflare provisions the DNS record and the certificate then, and the repository config stays without a `routes` block. Then change the default `site` in `docs/astro.config.mjs`, update `BASE_URL` in `docs/scripts/verify-seo.mjs` and the `Sitemap` line in `docs/public/robots.txt`, update the hosted links in the repository README, `SITE` in `docs/tools/src/main/java/docs/ContentCheck.java` (the content check resolves the README's hosted links against it), and the hostname named in the `docs/wrangler.jsonc` comment, and deploy. Remove the old hostname in the same dashboard section.
