(() => {
    'use strict';
    const $ = (id) => document.getElementById(id);
    const form = $('lab-form');
    const topicButtons = [...document.querySelectorAll('[data-topic]')];
    const lessons = {
        query: 'URL의 ? 뒤에 전달한 값을 개별 메서드 인자로 읽습니다.',
        path: 'URL 경로에 포함된 이름과 나이를 읽습니다. 각 값은 URL 인코딩해 전달합니다.',
        form: 'application/x-www-form-urlencoded 본문을 개별 인자로 읽습니다.',
        model: '폼 본문을 Star 객체의 속성에 바인딩하고 검증합니다.',
        queryModel: '쿼리 파라미터를 Star 객체의 속성에 바인딩하고 검증합니다.',
        json: 'application/json 본문을 Jackson으로 Star 객체에 변환하고 검증합니다.'
    };
    const labels = { request: '요청 바인딩', method: 'HTTP 메서드', response: '응답 형식' };
    let topic = 'request';
    let pending = false;
    function clearErrors() {
        for (const key of ['name', 'age']) {
            $(key).removeAttribute('aria-invalid');
            $(key + '-error').textContent = '';
        }
    }
    function spec() {
        if (topic === 'method') return { method: $('method').value, url: '/api/' + $('method').value.toLowerCase(), accept: 'application/json' };
        if (topic === 'response') return { method: 'GET', url: $('response-type').value, accept: $('response-type').value.endsWith('/string') ? 'text/plain' : 'application/json' };
        const name = $('name').value;
        const rawAge = $('age').value.trim();
        const data = { name, age: rawAge === '' ? null : Number.isFinite(Number(rawAge)) ? Number(rawAge) : rawAge };
        const params = new URLSearchParams({ name, age: rawAge });
        const choice = $('binding').value;
        const base = '/hello/request';
        if (choice === 'path') return { method: 'GET', url: base + '/star/' + encodeURIComponent(name) + '/age/' + encodeURIComponent(rawAge), accept: 'application/json' };
        if (choice === 'query' || choice === 'queryModel') return { method: 'GET', url: base + (choice === 'query' ? '/form/param?' : '/form/param/model?') + params, accept: 'application/json' };
        if (choice === 'json') return { method: 'POST', url: base + '/form/json', type: 'application/json', body: JSON.stringify(data), accept: 'application/json' };
        return { method: 'POST', url: base + (choice === 'model' ? '/form/model' : '/form/param'), type: 'application/x-www-form-urlencoded', body: params.toString(), accept: 'application/json' };
    }
    function resetResponse() {
        $('response-status').className = 'status-badge';
        $('response-status').textContent = '대기 중';
        $('response-time').textContent = '— ms';
        $('response-content-type').textContent = '—';
        $('response-message').textContent = '요청을 보내면 실제 서버 응답이 여기에 표시됩니다.';
        $('response-body').textContent = '아직 전송한 요청이 없습니다.';
    }
    function render() {
        const request = spec();
        $('binding-field').hidden = topic !== 'request';
        $('method-field').hidden = topic !== 'method';
        $('response-field').hidden = topic !== 'response';
        $('data-fields').hidden = topic !== 'request';
        $('sample-actions').hidden = topic !== 'request';
        $('topic-heading').textContent = labels[topic];
        $('lesson').textContent = topic === 'request' ? lessons[$('binding').value] : topic === 'method' ? '메서드에 따라 다른 핸들러가 응답합니다. 데이터 저장과 실제 CRUD는 수행하지 않습니다.' : '본문이 JSON처럼 보여도 문자열은 text/plain, 객체는 application/json으로 응답하도록 명시했습니다.';
        $('request-method').textContent = request.method;
        $('request-url').textContent = request.url;
        $('request-content-type').textContent = request.type || '본문 없음';
        $('request-accept').textContent = request.accept;
        $('request-body').textContent = request.type === 'application/json' ? JSON.stringify(JSON.parse(request.body), null, 2) : request.body || '이 요청에는 본문이 없습니다.';
    }
    topicButtons.forEach((button) => button.addEventListener('click', () => {
        if (pending) return;
        topic = button.dataset.topic;
        for (const item of topicButtons) {
            const selected = item === button;
            item.classList.toggle('selected', selected);
            item.setAttribute('aria-pressed', String(selected));
        }
        clearErrors();
        resetResponse();
        render();
    }));
    form.addEventListener('input', () => { clearErrors(); render(); });
    form.addEventListener('change', () => { clearErrors(); resetResponse(); render(); });
    for (const [id, valid] of [['valid-sample', true], ['invalid-sample', false]]) {
        $(id).addEventListener('click', () => {
            $('name').value = valid ? '민상' : ' ';
            $('age').value = valid ? '33' : '151';
            clearErrors();
            render();
            $('name').focus();
        });
    }
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (pending || event.isComposing) return;
        const request = spec();
        clearErrors();
        pending = true;
        const controls = [...form.querySelectorAll('input, select, button'), ...topicButtons];
        controls.forEach((control) => { control.disabled = true; });
        $('send').setAttribute('aria-busy', 'true');
        $('response-pane')?.setAttribute('aria-busy', 'true');
        $('response-status').className = 'status-badge';
        $('response-status').textContent = '전송 중';
        $('response-message').textContent = '서버 응답을 기다리고 있습니다.';
        $('response-time').textContent = '— ms';
        $('response-content-type').textContent = '—';
        $('response-body').textContent = '요청을 처리하고 있습니다.';
        const abort = new AbortController();
        const timeout = setTimeout(() => abort.abort(), 10000);
        const start = performance.now();
        try {
            const headers = { Accept: request.accept };
            if (request.type) headers['Content-Type'] = request.type;
            const response = await fetch(request.url, { method: request.method, headers, body: request.body, signal: abort.signal });
            const text = await response.text();
            const type = response.headers.get('content-type') || '헤더 없음';
            let data;
            if (type.includes('application/json')) {
                try { data = JSON.parse(text); } catch { data = undefined; }
            }
            $('response-status').textContent = String(response.status) + (response.ok ? ' OK' : ' ERROR');
            $('response-status').className = 'status-badge ' + (response.ok ? 'success' : 'error');
            $('response-content-type').textContent = type;
            $('response-body').textContent = data === undefined ? text : JSON.stringify(data, null, 2);
            $('response-message').textContent = response.ok ? '응답을 받았습니다. 상태 코드와 Content-Type, 본문을 비교해 보세요.' : data?.message || '요청에 실패했습니다. 입력값과 요청 형식을 확인한 뒤 다시 보내 주세요.';
            if (!response.ok && Array.isArray(data?.errors)) {
                for (const error of data.errors) {
                    if (['name', 'age'].includes(error.field)) {
                        $(error.field).setAttribute('aria-invalid', 'true');
                        $(error.field + '-error').textContent = error.message;
                    }
                }
            }
        } catch (error) {
            $('response-status').className = 'status-badge error';
            $('response-status').textContent = '연결 실패';
            $('response-message').textContent = error.name === 'AbortError' ? '10초 안에 응답을 받지 못했습니다. 서버 상태를 확인한 뒤 다시 보내 주세요.' : '서버에 연결하지 못했습니다. 서버 실행과 네트워크를 확인한 뒤 다시 보내 주세요.';
            $('response-body').textContent = '응답 본문을 받지 못했습니다.';
        } finally {
            clearTimeout(timeout);
            $('response-time').textContent = Math.round(performance.now() - start) + ' ms';
            controls.forEach((control) => { control.disabled = false; });
            $('send').removeAttribute('aria-busy');
            $('response-pane').removeAttribute('aria-busy');
            pending = false;
            form.querySelector('[aria-invalid="true"]')?.focus();
        }
    });
    [...form.querySelectorAll('button'), ...topicButtons].forEach((control) => { control.disabled = false; });
    render();
})();
