import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
const frontendRoot = new URL('../../frontend-thymeleaf/', import.meta.url);
const repositoryRoot = new URL('../../', import.meta.url);
const readFrontend = (path) => readFileSync(new URL(path, frontendRoot), 'utf8');
const readRepository = (path) => readFileSync(new URL(path, repositoryRoot), 'utf8');
const css = readFrontend('src/main/resources/static/css/lab.css');
const design = readRepository('project/docs/design/guide.md');
const html = readFrontend('src/main/resources/templates/index.html');
const js = readFrontend('src/main/resources/static/js/lab.js');
const swaggerHtml = readFrontend('src/main/resources/templates/java-swagger.html');
const swaggerJs = readFrontend('src/main/resources/static/js/java-swagger.js');
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
assert.ok(html.includes('href="/java-swagger"'));
assert.ok(swaggerHtml.includes('/swagger-ui/swagger-ui-bundle.js'));
assert.ok(swaggerHtml.includes('/js/java-swagger.js'));
assert.ok(swaggerJs.includes("url: '/v3/api-docs'"));
assert.ok(swaggerJs.includes('validatorUrl: null'));
assert.ok(swaggerJs.includes('queryConfigEnabled: false'));
assert.ok(swaggerJs.includes('persistAuthorization: false'));
assert.ok(!swaggerJs.includes('http://') && !swaggerJs.includes('https://'));
console.log('UI 정적 검사 통과: 토큰·레이블·상태 안내·안전한 응답 출력');
