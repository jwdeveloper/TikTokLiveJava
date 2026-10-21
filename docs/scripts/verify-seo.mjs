#!/usr/bin/env node
// Assert the SEO invariants of the built documentation.
//
// Run against a built Starlight site from the docs directory:
//
//   node scripts/verify-seo.mjs dist        (pnpm run seo)
//
// Prints one PASS/FAIL line per check and exits 1 if any check fails, so an
// SEO regression fails the CI build instead of silently degrading rankings.
// The gate only reads the build output; nothing here ships with the site.

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

export const BASE_URL = 'https://ttlj.docs.isaackogan.com/';
const EULER_HOST = 'eulerstream.com';
const SITEMAP_INDEX = 'sitemap-index.xml';
const SITEMAP_PAGES = 'sitemap-0.xml';

// The lucode theme renders the sidebar as <ul class="sidebar-entries ...">.
// A theme or Starlight upgrade that restructures the sidebar would silently
// delete site navigation while every other check still passed, so every page
// must carry this marker.
const NAV_MARKER = 'class="sidebar-entries';

// One sitewide footer link is the floor; the homepage must also vary its
// anchor text so the link profile does not read as a single repeated anchor.
const MIN_HOMEPAGE_ANCHORS = 3;

const ANCHOR_TAG = /<a\b[^>]*>/gi;
const CANONICAL_TAG = /<link\b[^>]*\brel="canonical"[^>]*>/gi;
const HREF = /\bhref="([^"]*)"/i;
const EULER_ANCHOR = new RegExp(
  `<a\\b[^>]*href="[^"]*${EULER_HOST.replace(/\./g, '\\.')}[^"]*"[^>]*>([\\s\\S]*?)</a>`,
  'gi',
);

function read(file) {
  return fs.readFileSync(file, 'utf8');
}

function isFile(file) {
  return fs.existsSync(file) && fs.statSync(file).isFile();
}

/** Astro emits one index.html per route plus a root 404.html; nothing else is a page. */
function findPages(root) {
  const pages = fs
    .readdirSync(root, { recursive: true, withFileTypes: true })
    .filter((entry) => entry.isFile() && entry.name === 'index.html')
    .map((entry) => path.relative(root, path.join(entry.parentPath, entry.name)).split(path.sep).join('/'))
    .sort();
  if (isFile(path.join(root, '404.html'))) pages.push('404.html');
  return new Map(pages.map((name) => [name, read(path.join(root, name))]));
}

/** The URL a built page must declare as canonical: `BASE_URL + dir + '/'`, `404.html` → `404/`. */
function expectedUrl(name) {
  const route = name === 'index.html'
    ? ''
    : name.endsWith('/index.html') ? name.slice(0, -'index.html'.length) : `${name.replace(/\.html$/, '')}/`;
  return BASE_URL + route;
}

function eulerAnchorTexts(html) {
  const texts = new Set();
  for (const [, inner] of html.matchAll(EULER_ANCHOR)) {
    const text = inner.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim().toLowerCase();
    if (text) texts.add(text);
  }
  return texts;
}

const sample = (list, n) => JSON.stringify(list.slice(0, n));

/**
 * Run every check against a built site.
 * @param {string} buildDir directory holding the Astro build output
 * @param {{ log?: (line: string) => void }} [options] `log` receives each PASS/FAIL line as it is decided
 * @returns {{ passes: string[], failures: string[] }} check names, in the order they ran
 */
export function verifySeo(buildDir, { log = () => {} } = {}) {
  const passes = [];
  const failures = [];
  const check = (name, ok, detail = '') => {
    (ok ? passes : failures).push(name);
    log(`${ok ? 'PASS' : 'FAIL'}  ${name}${!ok && detail ? ` :: ${detail}` : ''}`);
  };

  const root = path.resolve(buildDir);
  if (!fs.existsSync(root) || !fs.statSync(root).isDirectory()) {
    check(`build directory exists: ${buildDir}`, false, 'run the build first');
    return { passes, failures };
  }

  const indexExists = isFile(path.join(root, 'index.html'));
  check('index.html exists', indexExists);
  if (!indexExists) return { passes, failures };

  const pages = findPages(root);
  const indexHtml = pages.get('index.html');
  const pageNames = [...pages.keys()];

  // -- Title ---------------------------------------------------------------
  const titleText = (indexHtml.match(/<title>([\s\S]*?)<\/title>/)?.[1] ?? '').trim();
  check("homepage title contains 'TikTok LIVE API'", titleText.toLowerCase().includes('tiktok live api'), titleText);

  // -- Canonical -----------------------------------------------------------
  const wrongCanonical = pageNames.filter((name) => {
    const hrefs = [...pages.get(name).matchAll(CANONICAL_TAG)].map((m) => m[0].match(HREF)?.[1]);
    return !hrefs.includes(expectedUrl(name));
  });
  check('every page has rel=canonical pointing at its own URL', wrongCanonical.length === 0,
    `${wrongCanonical.length} wrong or missing, e.g. ${sample(wrongCanonical, 3)}`);

  // -- Open Graph + meta description ---------------------------------------
  check('homepage has og:title', indexHtml.includes('property="og:title"'));
  check('homepage has og:description', indexHtml.includes('property="og:description"'));
  check('homepage has og:image', indexHtml.includes('property="og:image"'));
  check('homepage has meta description', indexHtml.includes('name="description"'));

  // -- Sitemap -------------------------------------------------------------
  const sitemapIndex = path.join(root, SITEMAP_INDEX);
  check(`${SITEMAP_INDEX} exists`, isFile(sitemapIndex));
  if (isFile(sitemapIndex)) {
    check(`${SITEMAP_INDEX} references ${SITEMAP_PAGES}`,
      read(sitemapIndex).includes(`<loc>${BASE_URL}${SITEMAP_PAGES}</loc>`));
  }

  const sitemapPages = path.join(root, SITEMAP_PAGES);
  check(`${SITEMAP_PAGES} exists`, isFile(sitemapPages));
  if (isFile(sitemapPages)) {
    const xml = read(sitemapPages);
    const locs = (xml.match(/<loc>/g) ?? []).length;
    check(`${SITEMAP_PAGES} uses the canonical base URL`, xml.includes(BASE_URL));
    check(`${SITEMAP_PAGES} lists more than one URL`, locs > 1, `${locs} <loc> entries`);
  }

  // -- robots.txt ----------------------------------------------------------
  const robots = path.join(root, 'robots.txt');
  const sitemapLine = `Sitemap: ${BASE_URL}${SITEMAP_INDEX}`;
  check('robots.txt points at the sitemap index',
    isFile(robots) && read(robots).includes(sitemapLine),
    isFile(robots) ? `no '${sitemapLine}' line` : 'robots.txt missing');

  // -- Dofollow link profile -----------------------------------------------
  const nofollowed = [];
  const withoutEuler = [];
  for (const [name, html] of pages) {
    const eulerAnchors = (html.match(ANCHOR_TAG) ?? []).filter((a) => a.includes(EULER_HOST));
    if (eulerAnchors.length === 0) withoutEuler.push(name);
    for (const a of eulerAnchors) {
      if (a.toLowerCase().includes('nofollow')) nofollowed.push(`${name}: ${a.slice(0, 90)}`);
    }
  }
  check('no eulerstream.com link is nofollowed', nofollowed.length === 0,
    `${nofollowed.length} nofollowed, e.g. ${sample(nofollowed, 2)}`);
  check('eulerstream.com linked from every page', withoutEuler.length === 0,
    `${withoutEuler.length}/${pages.size} pages missing, e.g. ${sample(withoutEuler, 3)}`);

  // -- Anchor diversity ----------------------------------------------------
  const anchors = eulerAnchorTexts(indexHtml);
  check(`homepage uses >= ${MIN_HOMEPAGE_ANCHORS} distinct eulerstream anchor texts`,
    anchors.size >= MIN_HOMEPAGE_ANCHORS, `${anchors.size} distinct: ${sample([...anchors].sort(), 5)}`);

  // -- Navigation integrity ------------------------------------------------
  const missingNav = pageNames.filter((name) => !pages.get(name).includes(NAV_MARKER));
  check('every page retains sidebar navigation', missingNav.length === 0,
    `${missingNav.length} missing '${NAV_MARKER}', e.g. ${sample(missingNav, 3)}`);

  return { passes, failures };
}

function main(buildDir = 'dist') {
  const { passes, failures } = verifySeo(buildDir, { log: console.log });
  console.log(`\n${passes.length} passed, ${failures.length} failed`);
  return failures.length > 0 ? 1 : 0;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  process.exitCode = main(process.argv[2]);
}
