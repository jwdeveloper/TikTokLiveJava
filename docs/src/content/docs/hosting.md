---
title: Hosting
description: Host the static documentation on Cloudflare Workers static assets under a custom domain, deploy it from CI or a checkout, track the upstream repository on a schedule, and gate deploys on SEO checks.
---

The site is deployed to Cloudflare Workers as static assets and served at `https://ttlj.docs.isaackogan.com/`. Hosting lives in a maintainer's Cloudflare account while the documentation source stays in the public repository: the fork `isaackogan/TikTokLiveJava` holds the deployment secrets and runs the deploys, and a scheduled run there builds the upstream `jwdeveloper/TikTokLiveJava` `master` every day, so the site tracks upstream without the upstream repository holding any credentials. The same workflow in the upstream repository validates every pull request and would deploy too if its maintainer added the secrets. There is no Worker script: `docs/wrangler.jsonc` only declares the asset directory, HTML handling, and the custom domain. The site needs no Cloudflare Astro adapter and no TikTok or signing credentials; its only external link requirement is the dofollow links to [Euler Stream](https://www.eulerstream.com/) the SEO gate checks.

## Configuration

```jsonc
{
  "name": "tiktoklivejava-docs",
  "compatibility_date": "2026-10-20",
  "assets": {
    "directory": "./dist",
    "html_handling": "auto-trailing-slash",
    "not_found_handling": "404-page"
  },
  "workers_dev": false,
  "routes": [
    { "pattern": "ttlj.docs.isaackogan.com", "custom_domain": true }
  ]
}
```

- `html_handling: auto-trailing-slash` matches Astro's `trailingSlash: 'always'`. A request for `/events` redirects to `/events/`, which serves `events/index.html`.
- `not_found_handling: 404-page` serves Starlight's generated `404.html` with a 404 status.
- `workers_dev: false` keeps the site off the `workers.dev` hostname so the custom domain is the only public origin.
- The `routes` entry with `custom_domain: true` makes Cloudflare create the DNS record and the certificate for the hostname on deploy. The `isaackogan.com` zone must be in the same Cloudflare account as the Worker, and the hostname must not already have a DNS record.

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

The first deploy creates the Worker and attaches the custom domain; wrangler reports `Deployed tiktoklivejava-docs triggers` and `ttlj.docs.isaackogan.com (custom domain)`, and the certificate takes a few minutes to issue. Later deploys upload only the files that changed.

## Continuous deployment

`.github/workflows/docs.yml`, named `Documentation`, runs on pull requests and on pushes to `master` that touch `docs/`, `API/`, `Client/`, `extension-recorder/`, `extension-collector/`, `README.md`, or the workflow itself; it can be started by hand with `workflow_dispatch`; and it runs on a schedule, `17 6 * * *`, 06:17 UTC every day. Its permissions are `contents: read`, and one run at a time per ref: the concurrency group is `docs-${{ github.ref }}`.

The `build` job has one of two checkout steps: on a scheduled run it checks out `jwdeveloper/TikTokLiveJava` at `master`, and on every other event the repository and ref that triggered it. It then sets up Temurin JDK 17 with the Maven cache, runs `mvn -q -pl API,Client,extension-recorder,extension-collector -am -DskipTests package`, sets up Node.js 24, installs pnpm with `npm install --global pnpm@10.34.2`, runs `pnpm --dir docs install --frozen-lockfile`, `pnpm --dir docs test`, `pnpm --dir docs build`, and the SEO gate `pnpm --dir docs run seo`, and uploads `docs/dist` as the `documentation` artifact for review. The last step deploys with `cloudflare/wrangler-action@v4` (`workingDirectory: docs`, `packageManager: pnpm`, `command: deploy`) when the event is not a pull request, the ref is `refs/heads/master` or the event is the schedule, and `DEPLOY_ENABLED` is `true`; that job variable is derived from whether the `CLOUDFLARE_API_TOKEN` secret is set. The deploy needs both of these secrets:

| Secret | Value |
| --- | --- |
| `CLOUDFLARE_API_TOKEN` | A token with Workers Scripts Edit, Account Settings Read, Workers Routes Edit, and Zone Read on `isaackogan.com`. |
| `CLOUDFLARE_ACCOUNT_ID` | The hosting account's id, shown by `wrangler whoami`. |

Without `CLOUDFLARE_API_TOKEN` the deploy step is skipped and the workflow still validates the build, so the upstream repository, forks, and pull requests are unaffected. The gate checks only the token; `CLOUDFLARE_ACCOUNT_ID` is passed to wrangler and is required for the deploy to succeed, so set both. The maintainer sets them with `gh secret set CLOUDFLARE_API_TOKEN -R isaackogan/TikTokLiveJava` and `gh secret set CLOUDFLARE_ACCOUNT_ID -R isaackogan/TikTokLiveJava`. Because the scheduled run checks out the upstream repository, the workflow file must be on the fork's default branch for the schedule to exist, and a change to the documentation reaches the site either by being merged upstream and picked up by the next scheduled run, or by being pushed to the fork's `master`.

## SEO gate

`pnpm --dir docs run seo` runs `docs/scripts/verify-seo.mjs` over `docs/dist` after every build in CI and prints a `PASS` line per check; any failure fails the workflow. The checks are the invariants the sibling documentation sites carry: the homepage `<title>` contains `TikTok LIVE API`; every page, `404.html` included, has a `rel="canonical"` link to its own URL on `https://ttlj.docs.isaackogan.com/`; the homepage has `og:title`, `og:description`, `og:image`, and a meta description; `sitemap-index.xml` and `sitemap-0.xml` exist, the latter on the custom domain with more than one URL; `robots.txt` names the sitemap; no link to Euler Stream is nofollowed, every page has at least one, and the homepage has at least three distinct anchor texts; and every page carries the theme's sidebar marker, so a theme change that drops the navigation is caught. The gate's tests run with `pnpm --dir docs test`.

The Open Graph and Twitter card tags come from `head` in `docs/astro.config.mjs`; `robots.txt` is in `docs/public/`; the sitemap is generated from `site`.

## Verify a deployment

```sh
curl -I https://ttlj.docs.isaackogan.com/events/
curl -I https://ttlj.docs.isaackogan.com/events
curl -I https://ttlj.docs.isaackogan.com/missing/
```

Expect a 200, a 307 redirect to the trailing-slash form, and a 404 in that order. Then open the site, check an event page and the generated API and schema pages, and search for a gift event. The canonical link in the page head must use the custom domain. A scheduled run shows up as `gh run list --workflow=docs.yml -R isaackogan/TikTokLiveJava --event schedule --limit 1`.

## Changing the hostname

Edit `pattern` in `docs/wrangler.jsonc`, change the default `site` in `docs/astro.config.mjs`, update `BASE_URL` in `docs/scripts/verify-seo.mjs` and the `Sitemap` line in `docs/public/robots.txt`, update the hosted links in the repository README, and deploy. Cloudflare provisions the new hostname on the next deploy. Remove the old one under the Worker's **Settings → Domains & Routes** in the Cloudflare dashboard.
