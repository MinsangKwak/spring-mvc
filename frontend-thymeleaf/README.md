# Frontend · Thymeleaf

Spring MVC 요청을 작성하고 실제 응답을 비교하는 서버 렌더링 화면입니다. React 애플리케이션이 아니라 Thymeleaf, HTML, CSS와 JavaScript로 구성합니다.

## 구조

| 위치 | 역할 |
|---|---|
| `src/main/resources/templates/index.html` | 요청 설정·미리보기·응답 검사기 |
| `src/main/resources/templates/java-swagger.html` | `/java-swagger` 주소를 유지하는 API 문서 셸 |
| `src/main/resources/templates/hello*.html` | Thymeleaf 모델 렌더링 예제 |
| `src/main/resources/static/js/lab.js` | 요청 구성·전송·응답·오류 상태 처리 |
| `src/main/resources/static/js/java-swagger.js` | 같은 출처 OpenAPI 문서 초기화 |
| `src/main/resources/static/css/lab.css` | 반응형 레이아웃·상태·포커스 스타일 |
| `src/main/resources/static/hello.html` | 정적 HTML 응답 예제 |
| `tools/ui/check-ui.mjs` | 토큰·레이블·상태 안내·안전한 출력 정적 검사 |
| `tools/ui/browser-smoke.mjs` | 실제 서버를 대상으로 한 Playwright 흐름 검사 |

## 퍼블리싱 계약

- 명시적 레이블과 필드 오류를 연결하고 서버 오류에 `aria-invalid`를 적용합니다.
- 전송·성공·실패 상태를 라이브 영역으로 안내하며 오류 입력에 포커스를 이동합니다.
- 응답 본문은 `textContent`로 출력하고 사용자 입력을 HTML로 해석하지 않습니다.
- 키보드 포커스, 강제 색상 모드, reduced-motion과 760px 이하 레이아웃을 유지합니다.

저장소 루트에서 정적 검사를 실행합니다.

```bash
npm ci --prefix frontend-thymeleaf/tools/ui
npm --prefix frontend-thymeleaf/tools/ui run check:ui
```

브라우저 검사는 서버를 실행한 뒤 수행합니다.

```bash
npm --prefix frontend-thymeleaf/tools/ui run test:browser
```

자동 검사는 회귀 확인을 위한 것으로 전체 웹 접근성 준수나 인증을 의미하지 않습니다.

[프로젝트 소개](../README.md) · [UI 디자인 기준](../docs/DESIGN.md) · [보안 설계와 검증](../docs/SECURITY.md)
