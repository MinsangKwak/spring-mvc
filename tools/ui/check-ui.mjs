import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
const root = new URL('../../', import.meta.url);
const readProject = (path) => readFileSync(new URL(path, root), 'utf8');
const css = readProject('src/main/resources/static/css/lab.css');
const design = readProject('docs/DESIGN.md');
const html = readProject('src/main/resources/templates/index.html');
const js = readProject('src/main/resources/static/js/lab.js');
const colorBlock = design.split('colors:')[1].split('typography:')[0];
for (const [, name, value] of colorBlock.matchAll(/  ([a-z-]+): "(#[a-f0-9]+)"/g)) {
    assert.ok(css.includes('--' + name + ': ' + value + ';'), '토큰 불일치: ' + name);
}
assert.ok(html.includes('novalidate'));
for (const input of ['name', 'age']) {
    assert.ok(html.includes('for="' + input + '"'));
    assert.ok(html.includes('id="' + input + '-error"'));
}
assert.ok(html.includes('aria-live="polite"'));
assert.ok(css.includes('scrollbar-color:'));
assert.ok(css.includes(':focus-visible'));
assert.ok(css.includes('prefers-reduced-motion'));
assert.ok(!js.includes('.innerHTML'));
assert.ok(!/\b(alert|confirm|prompt)\s*\(/.test(js));
console.log('UI 정적 검사 통과: 토큰·레이블·상태 안내·안전한 응답 출력');
