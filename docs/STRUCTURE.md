# 구조와 설계 지도

코드를 읽기 전에 "무엇이 어디 있고, 요청이 어떤 단계를 거치며, 무엇을 바꾸면 어디까지 바뀌는가"를 한 장으로 보기 위한 문서입니다. 2026-11-09 기준 코드만 설명합니다.

## 1. 폴더 구조

루트에는 프로젝트 소개와 빌드·배포 진입점을 두고, 문서는 `docs`, 화면 검증 도구는 `tools/ui`, 애플리케이션은 `src`에서 관리합니다.

### 서버 코드 · `src/main/java/com/sparta/springmvc/`

| 패키지·파일 | 역할 |
|---|---|
| `SpringMvcApplication.java` | 애플리케이션 시작 |
| `config/` | OpenAPI 설명과 Swagger 설정 |
| `controller/` | GET·POST·PUT·DELETE 라우팅 예제 |
| `request/` | 경로·쿼리·폼·JSON 요청 바인딩과 DTO |
| `response/` | Controller·RestController의 문자열·객체 응답 비교 |
| `html/` | 정적 HTML·템플릿·모델·리다이렉트 예제 |
| `error/` | 공통 오류 응답과 예외 처리. 단계별 분류는 3절 |

### 화면과 설정 · `src/main/resources/`

| 경로 | 역할 |
|---|---|
| `templates/index.html` | 요청·응답 실습 홈 |
| `templates/hello.html` · `hello-visit.html` | Thymeleaf 렌더링 예제 |
| `static/js/lab.js` | 요청 미리보기·전송·응답 표시·오류 복구 |
| `static/css/lab.css` | 공통 스타일·반응형·접근성 상태 |
| `static/hello.html` | 정적 HTML 응답 예제 |
| `application.properties` | 로컬 기본 설정. Jackson 엄격 변환 옵션 포함 |
| `application-render.properties` | Render 포트와 프록시 설정 |

### 검증 도구와 루트 파일

| 위치 | 역할 |
|---|---|
| `src/test/java/com/sparta/springmvc/` | JUnit·MockMvc 서버 테스트 44개 (2026-11-09 기준, 매개변수 조합 포함) |
| `tools/ui/check-ui.mjs` | 디자인 토큰·레이블·안전한 출력 검사. `DESIGN.md` frontmatter와 `lab.css` 토큰의 일치 확인 |
| `tools/ui/browser-smoke.mjs` | 실제 화면과 Swagger API 실행 검증 |
| `tools/ui/package.json` · `package-lock.json` · `premium-ui.json` | 검증 도구의 명령·의존성 버전·대상 경로 |
| `.github/workflows/ci.yml` | 빌드부터 512 MB Docker 실행까지 자동 검증 |
| `build.gradle` · `settings.gradle` · `gradlew` · `gradle/wrapper/` | 빌드 진입점 |
| `Dockerfile` · `render.yaml` | 컨테이너 빌드와 Render 배포 설정 |
| `.gitignore` · `.gitattributes` · `.dockerignore` | Git·Docker 기본 동작 |

Node.js는 검증할 때만 필요하고 서버는 Gradle로 실행합니다. 검증 캡처와 로그는 `build/`에 생성하며 버전 관리에서 제외합니다. 문서용으로 선별한 화면만 `docs/images/`에 보관합니다.

## 2. 핵심 데이터

DB 엔티티는 없습니다. 모든 데이터는 요청·응답을 위한 DTO입니다.

| 타입 | 위치 | 필드 | 역할 |
|---|---|---|---|
| `Star` (request) | `request/Star.java` | `name: String` (`@NotBlank`, `@Size(max=40)`) · `age: Integer` (`@NotNull`, `@Min(0)`, `@Max(150)`) | 입력 DTO. 제약과 오류 문구의 원본 |
| `BindingResponse` | `request/BindingResponse.java` | `binding` · `name` · `age` | 바인딩 성공 응답. `binding`은 값을 읽은 애노테이션 이름 |
| `ApiError` | `error/ApiError.java` | `code` · `message` · `errors: List<FieldError>` | 모든 오류 응답의 단일 형식 |
| `ApiError.FieldError` | 〃 | `field` · `message` | 화면이 입력칸을 찾는 열쇠. `field`는 `name` 또는 `age` |
| `Star` (response) | `response/Star.java` | `name: String` · `age: int` | 직렬화 비교용. 제약 없음. 요청 DTO와 일부러 분리 |
| `MethodResponse` | `controller/HelloController.java` | `method` · `message` | HTTP 메서드 라우팅 확인용 |

`age`가 요청 DTO에서는 `Integer`, 응답 DTO에서는 `int`인 것은 의도입니다. 원시형이면 누락과 0을 구분할 수 없어 `@NotNull`로 누락을 검출하지 못합니다. 두 `Star` 모두 도메인 모델이나 DB 엔티티가 아닙니다.

## 3. 요청 한 건이 응답이 되기까지

`POST /hello/request/form/json` 기준이며, 번호는 Spring MVC가 실제로 밟는 순서입니다. 앞 단계에서 실패하면 그 단계의 예외가 발생하고 뒤 단계는 실행되지 않습니다.

| # | 단계 | 담당 | 실패 시 예외 | 응답 `code` |
|---|---|---|---|---|
| 1 | 라우팅: 경로·메서드 매칭 | `DispatcherServlet` → `@RequestMapping` | `NoResourceFoundException` / `HttpRequestMethodNotSupportedException` | 404 `NOT_FOUND` / 405 `METHOD_NOT_ALLOWED` |
| 2 | 미디어 타입: `consumes`·`produces` | 〃 | `HttpMediaTypeNotSupportedException` / `HttpMediaTypeNotAcceptableException` | 415 `UNSUPPORTED_MEDIA_TYPE` / 406 `NOT_ACCEPTABLE` |
| 3 | 객체 만들기(바인딩) | `@RequestBody`는 Jackson, `@ModelAttribute`는 `DataBinder`, `@RequestParam`·`@PathVariable`은 `ConversionService` | JSON: `HttpMessageNotReadableException` · 개별 인자: `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException` · `@ModelAttribute`: 오류가 `BindingResult`에 쌓여 4단계로 넘어감 | 400 `INVALID_REQUEST` |
| 4 | 검증 | 객체 인자는 `@Valid` → Bean Validation, 개별 인자는 클래스 `@Validated` → 메서드 검증 | `MethodArgumentNotValidException` / `ConstraintViolationException` | 400 `INVALID_INPUT` + `errors[]` |
| 5 | 핸들러 실행 | `RequestController.json()` | 분류되지 않은 예외 | 500 `INTERNAL_ERROR` |
| 6 | 직렬화 | `@ResponseBody` → Jackson | — | 200 `BindingResponse` |
| 7 | 예외 → `ApiError` | `ApiExceptionHandler` (1~5단계의 모든 예외가 모임) | — | 위 `code` |

핵심 규칙: **3단계 실패는 객체가 없으므로 필드 정보가 없을 수 있고(`INVALID_REQUEST`), 4단계 실패는 객체가 있으므로 필드별 오류가 있다(`INVALID_INPUT`).** 단, `@ModelAttribute`는 타입 변환 실패도 `BindingResult`에 담아 4단계 예외로 던지므로 같은 `age=abc`라도 `@RequestParam`은 `INVALID_REQUEST`, `@ModelAttribute`는 `INVALID_INPUT`입니다. 이 차이는 `ErrorResponseContractTest.sameWrongAgeIsClassifiedByBindingStyle`로 고정했습니다.

화면(`lab.js`)은 응답을 두 가지로만 해석합니다. `response.ok`이면 본문을 `textContent`로 표시하고, 아니면 `errors[]`를 돌며 `field`가 `name`·`age`인 항목을 해당 입력칸의 `aria-invalid`와 오류 문구에 연결합니다. 서버와 화면의 계약은 `ApiError`의 세 필드와 `field` 값 집합뿐입니다.

## 4. 변경 영향 범위

| 바꾸려는 것 | 함께 바뀌어야 하는 곳 |
|---|---|
| 나이 범위(0~150)·이름 길이(40) | `request/Star.java` 애노테이션·메시지 · `RequestController`의 `@Min`·`@Max`·`@Size`(개별 인자용, 두 곳이 같아야 함) · `ApiExceptionHandler.constraints()` 문구 · `@Tag` 설명 · `PRACTICE.md` 2절 · `MvcLabIntegrationTest` 경계값·`longName` · `browser-smoke.mjs` 오류 시나리오 |
| `ApiError` 필드 추가·이름 변경 | `lab.js`의 `errors`·`field` 접근 · `MvcLabIntegrationTest`·`ErrorResponseContractTest`의 jsonPath · `PRACTICE.md` 2절 예시 (OpenAPI 스키마는 자동 반영) |
| 오류 `code` 값 | `ApiExceptionHandler`의 두 switch · 3절 표 · `PRACTICE.md` 2절 · 테스트의 `$.code` |
| `BindingResponse` 필드 이름 | `lab.js` 표시 · 테스트 `assertBinding` · `PRACTICE.md` 1절 예시 |
| 실습 URL(`/hello/request/**` 등) | `lab.js` URL 조립 · `springdoc.paths-to-match` · `browser-smoke.mjs` · `ci.yml`의 curl · 문서 표 |
| Jackson 변환 설정 | `application.properties` · `invalidJsonFormat`·`jsonTypeMismatchFailsBeforeObjectExists` 테스트 · `PRACTICE.md` JSON 절 |
| 색상·간격 토큰 | `lab.css`의 `:root` · `DESIGN.md` frontmatter · `check-ui.mjs`가 둘의 일치를 검사 |
| Spring Boot 버전 | `build.gradle` · 부모 `ResponseEntityExceptionHandler`가 다루는 예외 목록이 바뀔 수 있으므로 `wrongContentTypeAndMethod`·`ErrorResponseContractTest` 결과를 먼저 확인 |

영향 범위 밖: `controller/`·`response/`·`html/`는 `request/`·`error/`와 코드로 연결되어 있지 않아 요청 바인딩 변경의 영향을 받지 않습니다.

[프로젝트 소개](../README.md) · [실습 가이드](PRACTICE.md) · [개선 기록](IMPROVEMENTS.md)
