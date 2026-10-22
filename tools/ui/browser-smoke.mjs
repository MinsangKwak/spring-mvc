import assert from 'node:assert/strict';
import { mkdir } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const browser = await chromium.launch({
    headless: true,
    executablePath: process.env.CHROMIUM_PATH || undefined,
    args: ['--no-sandbox']
});
const base = process.env.LAB_URL || 'http://127.0.0.1:8080';
const screenshots = process.env.LAB_SCREENSHOTS || fileURLToPath(new URL('../../build/browser-screenshots/', import.meta.url));
await mkdir(screenshots, { recursive: true });
const page = await browser.newPage({ viewport: { width: 1440, height: 1100 }, locale: 'ko-KR' });
const errors = [];
page.on('pageerror', (error) => errors.push(error.message));
async function send(expected = 200) {
    const response = page.waitForResponse((r) => r.url().includes('/hello/request/') || r.url().includes('/api/') || r.url().includes('/response/'));
    await page.getByRole('button', { name: '요청 보내기' }).click();
    assert.equal((await response).status(), expected);
    await page.waitForFunction(() => document.getElementById('send').getAttribute('aria-busy') !== 'true');
}
try {
    await page.goto(base);
    await page.getByLabel('요청 방식').selectOption('json');
    await send();
    assert.match(await page.locator('#response-body').innerText(), /민상/);
    await page.screenshot({ path: screenshots + '/lab-desktop.png', fullPage: true });
    for (const binding of ['query', 'path', 'form', 'model', 'queryModel', 'json']) {
        await page.getByLabel('요청 방식').selectOption(binding);
        await send();
        assert.match(await page.locator('#response-body').innerText(), /"age": 33/);
    }
    await page.getByRole('button', { name: '오류값', exact: true }).click();
    await send(400);
    assert.equal(await page.locator('#name').getAttribute('aria-invalid'), 'true');
    assert.equal(await page.locator('#age').getAttribute('aria-invalid'), 'true');
    assert.equal(await page.locator('#name').evaluate((e) => e === document.activeElement), true);
    await page.screenshot({ path: screenshots + '/lab-validation.png', fullPage: true });
    await page.getByRole('button', { name: '정상값', exact: true }).click();
    await page.getByLabel('이름', { exact: true }).fill('<img src=x onerror=alert(1)>');
    await send();
    assert.equal(await page.locator('#response-body img').count(), 0);
    assert.match(await page.locator('#response-body').innerText(), /<img/);
    await page.getByRole('button', { name: 'HTTP 메서드' }).click();
    for (const method of ['GET', 'POST', 'PUT', 'DELETE']) {
        await page.getByLabel('HTTP 메서드', { exact: true }).selectOption(method);
        await send();
        assert.match(await page.locator('#response-body').innerText(), new RegExp(method));
    }
    await page.getByRole('button', { name: '응답 형식' }).click();
    for (const path of ['/response/json/string', '/response/json/class', '/response/rest/json/string', '/response/rest/json/class']) {
        await page.getByLabel('응답 방식').selectOption(path);
        await send();
        assert.match(await page.locator('#response-content-type').innerText(), path.endsWith('/string') ? /text\/plain/ : /application\/json/);
    }
    await page.getByRole('button', { name: '요청 바인딩' }).click();
    await page.getByRole('button', { name: '정상값', exact: true }).click();
    await page.route('**/hello/request/form/json', async (route) => {
        await new Promise((resolve) => setTimeout(resolve, 500));
        await route.continue();
    });
    await page.getByRole('button', { name: '요청 보내기' }).click();
    assert.equal(await page.locator('#send').isDisabled(), true);
    assert.match(await page.locator('#response-status').innerText(), /전송 중/);
    await page.waitForFunction(() => !document.getElementById('send').disabled);
    await page.unroute('**/hello/request/form/json');
    await page.route('**/hello/request/form/json', (route) => route.abort('internetdisconnected'));
    await page.getByRole('button', { name: '요청 보내기' }).click();
    await page.waitForFunction(() => document.getElementById('response-status').textContent === '연결 실패');
    assert.equal(await page.locator('#send').isEnabled(), true);
    await page.unroute('**/hello/request/form/json');
    await page.clock.install();
    await page.route('**/hello/request/form/json', () => {});
    await page.getByRole('button', { name: '요청 보내기' }).click();
    await page.clock.fastForward(10001);
    await page.waitForFunction(() => document.getElementById('response-status').textContent === '연결 실패');
    assert.match(await page.locator('#response-message').innerText(), /10초/);
    await page.unroute('**/hello/request/form/json');
    await page.clock.resume();
    await send();
    await page.setViewportSize({ width: 375, height: 812 });
    await page.emulateMedia({ reducedMotion: 'reduce' });
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true);
    await page.screenshot({ path: screenshots + '/lab-mobile.png', fullPage: true });
    await page.locator('#binding').focus();
    await page.keyboard.press('ArrowUp');
    await page.keyboard.press('Tab');
    assert.equal(await page.locator('#name').evaluate((e) => e === document.activeElement), true);
    for (const path of ['/hello.html', '/html/templates', '/html/dynamic', '/html/redirect']) {
        await page.goto(base + path);
        await page.getByRole('link', { name: '실습 홈으로' }).click();
        assert.equal(new URL(page.url()).pathname, '/');
    }
    await page.setViewportSize({ width: 1440, height: 1100 });
    await page.goto(base + '/swagger-ui.html');
    await page.waitForSelector('.opblock');
    assert.equal(await page.locator('.opblock').count(), 15);
    const operation = page.locator('.opblock').filter({ has: page.locator('[data-path="/hello/request/form/json"]') });
    await operation.locator('.opblock-summary').click();
    await operation.getByRole('button', { name: 'Try it out' }).click();
    await operation.locator('textarea').fill(JSON.stringify({ name: '민상', age: 33 }, null, 2));
    const swaggerResponse = page.waitForResponse((r) => new URL(r.url()).pathname === '/hello/request/form/json' && r.request().method() === 'POST');
    await operation.getByRole('button', { name: 'Execute', exact: true }).click();
    assert.equal((await swaggerResponse).status(), 200);
    await operation.locator('.live-responses-table').waitFor();
    await page.screenshot({ path: screenshots + '/swagger-ui.png', fullPage: true });
    const formOperation = page.locator('.opblock').filter({ has: page.locator('[data-path="/hello/request/form/model"]') });
    await formOperation.locator('.opblock-summary').click();
    await formOperation.getByRole('button', { name: 'Try it out' }).click();
    const formInputs = formOperation.locator('input');
    assert.equal(await formInputs.count(), 2);
    await formInputs.nth(0).fill('민상');
    await formInputs.nth(1).fill('33');
    const formResponse = page.waitForResponse((r) => new URL(r.url()).pathname === '/hello/request/form/model' && r.request().method() === 'POST');
    await formOperation.getByRole('button', { name: 'Execute', exact: true }).click();
    assert.equal((await formResponse).status(), 200);
    assert.deepEqual(errors, []);
    console.log('브라우저 검증 통과: 바인딩 6종, 메서드 4종, 응답 4종, 검증 오류, 안전한 출력, 로딩·중복 방지·실패 복구, 모바일·키보드, HTML 이동, Swagger 실제 실행');
} finally {
    await browser.close();
}
