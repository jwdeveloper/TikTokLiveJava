// Unit tests for the SEO gate. Each test writes a minimal built site into a
// temporary directory and runs verifySeo against it, so the gate's checks are
// exercised without a real Astro build.
//
//   pnpm run test            (node --test "scripts/**/*.test.mjs")

import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { test } from 'node:test';

import { BASE_URL, verifySeo } from './verify-seo.mjs';

/** Render one fixture page. `route` is the path under BASE_URL ('' for the homepage). */
function pageHtml({
  route,
  title = 'Guide',
  canonical = BASE_URL + route,
  eulerLinks = ['Euler Stream'],
  openGraph = false,
  sidebar = true,
} = {}) {
  const og = openGraph
    ? [
        '<meta property="og:title" content="Getting Started"/>',
        '<meta property="og:description" content="Install TikTokLiveJava."/>',
        '<meta property="og:image" content="https://ttlj.docs.isaackogan.com/og-image.png"/>',
        '<meta name="description" content="Install TikTokLiveJava."/>',
      ].join('\n')
    : '';
  const links = eulerLinks
    .map((text) => `<a href="https://www.eulerstream.com/">${text}</a>`)
    .join('\n');
  return `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8"/>
<title>${title}</title>
<link rel="canonical" href="${canonical}"/>
${og}
</head>
<body>
<nav>${sidebar ? '<ul class="sidebar-entries sl-sidebar"><li><a href="/">Home</a></li></ul>' : ''}</nav>
<main><h1>${title}</h1><p>Body text.</p></main>
<footer>${links}</footer>
</body>
</html>
`;
}

function defaultFiles() {
  return {
    'index.html': pageHtml({
      route: '',
      title: 'TikTok LIVE API for Java: TikTokLiveJava Documentation',
      openGraph: true,
      eulerLinks: ['Euler Stream', 'TikTok LIVE signing API', "Euler Stream's documentation"],
    }),
    'guide/index.html': pageHtml({ route: 'guide/' }),
    'guide/nested/index.html': pageHtml({ route: 'guide/nested/' }),
    '404.html': pageHtml({ route: '404/', title: 'Page not found' }),
    'sitemap-index.xml':
      '<?xml version="1.0" encoding="UTF-8"?><sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">' +
      `<sitemap><loc>${BASE_URL}sitemap-0.xml</loc></sitemap></sitemapindex>`,
    'sitemap-0.xml':
      '<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">' +
      `<url><loc>${BASE_URL}</loc></url><url><loc>${BASE_URL}guide/</loc></url>` +
      `<url><loc>${BASE_URL}guide/nested/</loc></url></urlset>`,
    'robots.txt': `User-agent: *\nAllow: /\n\nSitemap: ${BASE_URL}sitemap-index.xml\n`,
  };
}

/**
 * Write a minimal built site into `dir`. `overrides` maps a relative path to
 * its content; a `null` value omits that file from the fixture.
 */
function writeFixtureSite(dir, overrides = {}) {
  const files = { ...defaultFiles(), ...overrides };
  for (const [relative, content] of Object.entries(files)) {
    if (content === null) continue;
    const target = path.join(dir, relative);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    fs.writeFileSync(target, content, 'utf8');
  }
  return dir;
}

function fixtureDir(t) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'verify-seo-'));
  t.after(() => fs.rmSync(dir, { recursive: true, force: true }));
  return dir;
}

test('gatePassesOnFixtureSite', (t) => {
  const dir = writeFixtureSite(fixtureDir(t));

  const { passes, failures } = verifySeo(dir);

  assert.deepEqual(failures, []);
  assert.equal(passes.length, 17, `every check runs once on the fixture, got ${passes.length}`);
});

test('failsWhenNestedPageLacksEulerLink', (t) => {
  const dir = writeFixtureSite(fixtureDir(t), {
    'guide/nested/index.html': pageHtml({ route: 'guide/nested/', eulerLinks: [] }),
  });

  const { failures } = verifySeo(dir);

  assert.deepEqual(failures, ['eulerstream.com linked from every page']);
});

test('failsWhenRobotsHasNoSitemap', (t) => {
  const dir = writeFixtureSite(fixtureDir(t), {
    'robots.txt': 'User-agent: *\nAllow: /\n',
  });

  const { failures } = verifySeo(dir);

  assert.deepEqual(failures, ['robots.txt points at the sitemap index']);
});

test('failsWhenCanonicalPointsAtAnotherPage', (t) => {
  const dir = writeFixtureSite(fixtureDir(t), {
    'guide/nested/index.html': pageHtml({ route: 'guide/nested/', canonical: BASE_URL + 'guide/' }),
  });

  const { failures } = verifySeo(dir);

  assert.deepEqual(failures, ['every page has rel=canonical pointing at its own URL']);
});
