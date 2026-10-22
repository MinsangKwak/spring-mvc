# 화면 검증 도구

서버 실행에 필요한 Node 앱이 아니라, 웹 실습 화면을 검사하기 위한 개발 도구입니다. Node.js 22 이상을 권장합니다.

저장소 루트에서 다음 명령을 실행합니다.

```bash
npm ci --prefix tools/ui
npm --prefix tools/ui run check:ui
npm --prefix tools/ui exec -- playwright install chromium
```

서버를 `./gradlew bootRun`으로 실행한 뒤 별도 터미널에서 브라우저 검증을 실행합니다.

```bash
npm --prefix tools/ui run test:browser
```

| 설정 | 기본값·용도 |
|---|---|
| `LAB_URL` | `http://127.0.0.1:8080` · 검증 대상 서버 |
| `LAB_SCREENSHOTS` | 저장소의 `build/browser-screenshots/` · 캡처 출력 위치 |
| `CHROMIUM_PATH` | 지정한 Chromium 실행 파일 사용 · 생략하면 Playwright 기본 브라우저 |

`check-ui.mjs`는 색상 토큰과 폼·상태 표시 규칙을 확인합니다. `browser-smoke.mjs`는 바인딩·메서드·응답 비교, 오류 복구, 모바일·키보드, Swagger 요청 실행을 확인합니다. 자동화 검증은 전체 접근성 적합성 심사를 대신하지 않습니다.

[폴더 구조](../../docs/STRUCTURE.md) · [프로젝트 소개](../../README.md)
