# Spring MVC Request Lab

**Thymeleaf 화면에서 보낸 요청이 Spring MVC의 라우팅·바인딩·검증·예외 처리를 거쳐 어떤 HTTP 응답으로 돌아오는지 비교하는 풀스택 학습 프로젝트입니다.**

[웹 실습](https://spring-mvc-k14y.onrender.com/) · [Java Swagger](https://spring-mvc-k14y.onrender.com/java-swagger) · [상세 포트폴리오](https://www.notion.so/401cbbdd109b816ea6caeef336df0802)

무료 배포 환경은 비활동 시 정지할 수 있어 첫 접속이 늦을 수 있습니다. 입력값은 DB에 저장하지 않습니다.

## 기술과 담당 영역

| 영역 | 위치 | 기술 | 구현 범위 |
|---|---|---|---|
| Backend · Java | `backend-java/` | Java 17, Spring Boot 3.5.16, Spring MVC, Bean Validation, Jackson, springdoc-openapi | 바인딩 6종, 요청·응답 DTO, 검증, 공통 오류 계약 |
| Frontend · Thymeleaf | `frontend-thymeleaf/` | Thymeleaf, HTML, CSS, JavaScript | 요청 작성·미리보기, 응답 검사기, 반응형 화면, 키보드·상태 안내·필드 오류 처리 |
| Tests · Tools · Docs | `docs/` | JUnit, MockMvc, Playwright, Node.js | 서버·화면 계약 테스트, 정적 검사, 기술·보안·배포 문서 |
| Build · Delivery | 저장소 루트 | Gradle Wrapper, GitHub Actions, Docker, Render | 빌드·서버·브라우저·512 MB 컨테이너 검증과 배포 설정 |

프런트엔드는 React가 아니라 Spring이 제공하는 Thymeleaf 템플릿과 정적 자산입니다. Node.js는 UI 자동 검증에만 사용하고, 애플리케이션 실행은 Gradle이 담당합니다.

## 현재 구현

| 주제 | 확인할 수 있는 것 |
|---|---|
| 요청 바인딩 | `@PathVariable`, `@RequestParam`, `@ModelAttribute`, `@RequestBody`의 입력 위치와 변환 차이 |
| 검증과 오류 | 누락·범위·타입·JSON 문법·Content-Type 오류를 공통 `ApiError` 형식으로 분류 |
| 응답 형식 | `@Controller`·`@RestController`, 문자열·객체의 반환 타입과 Content-Type 차이 |
| HTML 응답 | 정적 HTML, Thymeleaf 모델, 인스턴스 메모리 카운터, 리다이렉트 |
| 실행 화면 | 실제 요청 URL·헤더·본문 미리보기와 상태 코드·응답 헤더·본문 비교 |
| API 문서 | `/java-swagger`의 Swagger UI에서 JSON·폼 요청을 실제 서버로 전송 |

이 프로젝트는 MVC 요청 처리 학습에 집중합니다. DB, 인증, 영속 CRUD, 운영 트래픽 성능 측정은 구현 범위에 포함하지 않습니다.

## 저장소 구조

```text
spring-mvc/
├── backend-java/
│   ├── build.gradle
│   └── src/main/
│       ├── java/com/sparta/springmvc/
│       └── resources/application*.properties
├── frontend-thymeleaf/
│   └── src/main/resources/
│       ├── templates/
│       └── static/
├── docs/
│   ├── tests/
│   │   ├── backend-java/java/com/sparta/springmvc/
│   │   └── frontend-thymeleaf/
│   ├── tools/
│   ├── package.json
│   └── README.md
├── .github/workflows/ci.yml
├── Dockerfile
├── render.yaml
├── settings.gradle
└── gradlew
```

`settings.gradle`은 루트 프로젝트의 빌드 파일을 `backend-java/build.gradle`로 지정합니다. 빌드 파일은 백엔드 영역에 두되 `./gradlew`, `build/`, `build/libs/` 같은 루트 실행·산출물 경로는 유지합니다. Gradle의 `sourceSets`가 백엔드 설정, 프런트엔드 템플릿·정적 자산과 `docs/tests/backend-java`의 테스트를 연결합니다. 실행 JAR에는 제품 코드와 화면 자산만 포함됩니다.

## 요청 처리 흐름

1. `DispatcherServlet`이 경로·HTTP 메서드·미디어 타입을 판별합니다.
2. Spring MVC가 경로, 쿼리, 폼 또는 JSON을 컨트롤러 인자와 DTO로 변환합니다.
3. Bean Validation이 이름·나이 제약을 검사합니다.
4. 컨트롤러가 `BindingResponse` 또는 비교용 응답을 반환합니다.
5. 실패하면 `ApiExceptionHandler`가 상태 코드와 공통 `ApiError`로 변환합니다.
6. 브라우저는 응답을 `textContent`로 출력하고 필드 오류를 입력 요소에 연결합니다.

바인딩 단계별 예외와 `code`는 [구조와 설계 지도](docs/STRUCTURE.md#3-요청-한-건이-응답이-되기까지)에 정리했습니다.

## 퍼블리싱과 접근성

- 입력 요소와 명시적 `label`, 오류 문구를 연결하고 서버 오류 시 `aria-invalid`를 적용합니다.
- 전송 상태와 결과 안내에는 라이브 영역을 사용하며, 오류 필드에 포커스를 이동해 다음 행동을 알립니다.
- 링크·버튼·기본 `select`를 용도에 맞게 사용하고 키보드 조작과 `:focus-visible` 상태를 유지합니다.
- 760px 이하에서 요청·응답을 세로로 재배치하고 긴 URL·JSON이 화면 밖으로 넘치지 않게 처리합니다.
- 강제 색상 모드와 `prefers-reduced-motion`을 고려하며, 상태는 색상만으로 구분하지 않습니다.
- UI 정적 검사는 디자인 토큰, 레이블, 라이브 영역, 포커스 스타일, 안전한 텍스트 출력을 확인합니다. Playwright는 키보드·오류·모바일·API 흐름을 실제 브라우저에서 검사합니다.

자동 검사는 구현 계약의 회귀를 찾기 위한 것이며 전체 웹 접근성 준수나 인증을 의미하지 않습니다.

## 프런트엔드·백엔드 방어

| 위협 | Backend · Java | Frontend · Thymeleaf | 검증 |
|---|---|---|---|
| 비정상 입력·파서 남용 | Bean Validation, 엄격한 Jackson 변환, JSON 본문·폼 16 KB와 문자열·숫자·중첩 제한 | 입력 안내와 서버 필드 오류 연결 | 경계값·타입·초과 JSON 계약 테스트 |
| XSS·콘텐츠 삽입 | 사용자 입력을 JSON으로 직렬화하고 응답 타입을 고정 | 응답·오류를 `textContent`로 출력, Thymeleaf `th:text`, 앱 CSP | hostile HTML 입력의 DOM 미생성 브라우저 검사 |
| 오류 정보 노출 | 고정된 `ApiError`, 500 내부 원인은 서버 로그에만 기록, 기본 오류의 메시지·스택 비공개 | 내부 예외 대신 고정 안내 표시 | 오류 응답 형식 테스트 |
| 교차 출처·클릭재킹·캐시 | 다른 출처의 브라우저 응답 읽기를 허용하지 않고 `DENY`·`nosniff`·`no-store`·HTTPS HSTS 적용 | `connect-src 'self'`, `frame-ancestors 'none'`, 외부 스크립트 미사용 | 보안 헤더·hostile Origin 계약 테스트 |
| 전달 헤더 변조 | OpenAPI 서버 URL을 상대 경로 `/`로 고정 | 같은 출처 `/v3/api-docs`만 로드 | 위조 `Forwarded`·`X-Forwarded-Host` 비반영 테스트 |

Swagger UI는 `/java-swagger` 주소를 유지하는 얇은 같은 출처 화면으로 제공합니다. 기능상 필요한 inline style만 이 경로의 CSP에서 허용하고, 스크립트는 같은 출처만 허용합니다. 기존 `/swagger-ui.html`은 호환 리다이렉트입니다. 상세한 범위와 남은 운영 경계는 [보안 설계와 검증](docs/SECURITY.md)에 정리했습니다.

## 실행 방법

필요 환경은 Java 17 JDK입니다. 저장소 루트에서 실행합니다.

```bash
./gradlew bootRun
```

Windows에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat bootRun
```

기본 주소는 `http://localhost:8080/`입니다. 포트를 바꾸려면 다음처럼 실행합니다.

```bash
./gradlew bootRun --args='--server.port=8081'
```

## 검증 방법

### 서버 빌드와 테스트

```bash
./gradlew clean build
```

### UI 정적 검사

```bash
npm ci --prefix docs --ignore-scripts
npm --prefix docs run check:ui
```

### 브라우저 흐름 검사

서버를 먼저 실행한 뒤 Chromium을 설치하고 검사합니다.

```bash
npm --prefix docs exec -- playwright install chromium
npm --prefix docs run test:browser
```

다른 서버 주소를 사용할 때는 `LAB_URL` 환경변수로 지정합니다. GitHub Actions는 빌드·서버 테스트·UI 정적 검사·브라우저 흐름·Docker 실행을 한 번에 검증하고 `build/` 아래의 결과를 아티팩트로 보관합니다.

## 코드 지도

| 목적 | 시작 파일 |
|---|---|
| 애플리케이션 시작 | `backend-java/src/main/java/com/sparta/springmvc/SpringMvcApplication.java` |
| 바인딩 API | `backend-java/src/main/java/com/sparta/springmvc/request/RequestController.java` |
| 공통 오류 계약 | `backend-java/src/main/java/com/sparta/springmvc/error/ApiExceptionHandler.java` |
| Thymeleaf 실습 화면 | `frontend-thymeleaf/src/main/resources/templates/index.html` |
| 요청 전송·응답 표시 | `frontend-thymeleaf/src/main/resources/static/js/lab.js` |
| 반응형·상태 스타일 | `frontend-thymeleaf/src/main/resources/static/css/lab.css` |
| 서버 계약 테스트 | `docs/tests/backend-java/java/com/sparta/springmvc/` |
| UI 정적 검사 | `docs/tools/check-ui.mjs` |
| 브라우저 흐름 검사 | `docs/tests/frontend-thymeleaf/browser-smoke.mjs` |

## 문서

- [문서·검증 안내](docs/README.md)
- [구조와 설계 지도](docs/STRUCTURE.md)
- [실습 가이드](docs/PRACTICE.md)
- [현재 구현과 검증](docs/IMPROVEMENTS.md)
- [UI 디자인 기준](docs/DESIGN.md)
- [보안 설계와 검증](docs/SECURITY.md)
- [Render 배포](docs/DEPLOYMENT.md)
