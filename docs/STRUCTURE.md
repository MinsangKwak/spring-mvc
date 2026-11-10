# 구조와 설계 지도

폴더 구조와 핵심 DTO, 요청 처리 흐름, 변경 시 확인할 범위를 정리했습니다. 2026-11-09 기준 코드에 맞춰 설명합니다.

## 1. 폴더 구조

저장소 루트에는 프로젝트 소개와 빌드·배포 설정을 두었습니다. 문서는 `docs`, 화면 검증 도구는 `tools/ui`, 애플리케이션 코드는 `src`에서 관리합니다.

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
| `src/test/java/com/sparta/springmvc/` | JUnit·MockMvc 서버 테스트. 범위와 실행 기록은 [검증 근거](IMPROVEMENTS.md#검증-근거) 참조 |
| `tools/ui/check-ui.mjs` | 디자인 토큰·레이블·안전한 출력 검사. `DESIGN.md` frontmatter와 `lab.css` 토큰의 일치 확인 |
| `tools/ui/browser-smoke.mjs` | 실제 화면과 Swagger API 실행 검증 |
| `tools/ui/package.json` · `package-lock.json` · `premium-ui.json` | 검증 도구의 명령·의존성 버전·대상 경로 |
| `.github/workflows/ci.yml` | 빌드부터 512 MB Docker 실행까지 자동 검증 |
| `build.gradle` · `settings.gradle` · `gradlew` · `gradle/wrapper/` | 빌드 진입점 |
| `Dockerfile` · `render.yaml` | 컨테이너 빌드와 Render 배포 설정 |
| `.gitignore` · `.gitattributes` · `.dockerignore` | Git·Docker 기본 동작 |

Node.js는 화면 검증에만 사용하며, 서버는 Gradle로 실행합니다. 검증 캡처와 로그는 `build/`에 생성하며 버전 관리에서 제외합니다. 문서용으로 선별한 화면만 `docs/images/`에 보관합니다.

## 2. 핵심 데이터

DB 엔티티는 사용하지 않습니다. 아래 타입은 요청과 응답을 표현하는 DTO입니다.

| 타입 | 위치 | 필드 | 역할 |
|---|---|---|---|
| `Star` (request) | `request/Star.java` | `name: String` (`@NotBlank`, `@Size(max=40)`) · `age: Integer` (`@NotNull`, `@Min(0)`, `@Max(150)`) | 입력 DTO. 제약과 오류 문구의 원본 |
| `BindingResponse` | `request/BindingResponse.java` | `binding` · `name` · `age` | 바인딩 성공 응답. `binding`은 값을 읽은 애노테이션 이름 |
| `ApiError` | `error/ApiError.java` | `code` · `message` · `errors: List<FieldError>` | 예외 처리기가 반환하는 공통 오류 형식 |
| `ApiError.FieldError` | 〃 | `field` · `message` | 오류를 표시할 입력 필드와 안내 문구. `field`는 `name` 또는 `age` |
| `Star` (response) | `response/Star.java` | `name: String` · `age: int` | JSON 직렬화 비교용. 입력 제약이 있는 요청 DTO와 분리 |
| `MethodResponse` | `controller/HelloController.java` | `method` · `message` | HTTP 메서드 라우팅 확인용 |

요청 DTO의 `age`는 `Integer`로 선언해 누락(`null`)과 0을 구분하고, `@NotNull`로 누락을 검증합니다. 응답 비교용 DTO에는 `int`를 사용합니다. 두 `Star`는 DB 엔티티가 아닌 요청·응답 DTO입니다.

## 3. 요청 한 건이 응답이 되기까지

`POST /hello/request/form/json`을 기준으로 요청 처리 흐름을 설명하고, 다른 바인딩 방식과의 차이도 함께 정리했습니다. 1~6은 정상 응답의 처리 흐름입니다. 처리 중 예외가 발생하면 정상 흐름을 중단하고 7단계의 공통 오류 처리로 이동합니다.

| # | 단계 | 담당 | 실패 시 예외 | 응답 `code` |
|---|---|---|---|---|
| 1 | 라우팅: 경로·메서드 매칭 | `DispatcherServlet` → `@RequestMapping` | `NoResourceFoundException` / `HttpRequestMethodNotSupportedException` | 404 `NOT_FOUND` / 405 `METHOD_NOT_ALLOWED` |
| 2 | 미디어 타입: `consumes`·`produces` | 〃 | `HttpMediaTypeNotSupportedException` / `HttpMediaTypeNotAcceptableException` | 415 `UNSUPPORTED_MEDIA_TYPE` / 406 `NOT_ACCEPTABLE` |
| 3 | 객체 만들기(바인딩) | `@RequestBody`는 Jackson, `@ModelAttribute`는 `DataBinder`, `@RequestParam`·`@PathVariable`은 `ConversionService` | JSON: `HttpMessageNotReadableException` · 개별 인자: `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException` · `@ModelAttribute`: 오류가 `BindingResult`에 쌓여 4단계로 넘어감 | 400 `INVALID_REQUEST` |
| 4 | 검증 | 객체 인자는 `@Valid` → Bean Validation, 개별 인자는 클래스 `@Validated` → 메서드 검증 | `MethodArgumentNotValidException` / `ConstraintViolationException` | 400 `INVALID_INPUT` + `errors[]` |
| 5 | 핸들러 실행 | `RequestController.json()` | 분류되지 않은 예외 | 500 `INTERNAL_ERROR` |
| 6 | 직렬화 | `@ResponseBody` → Jackson | — | 200 `BindingResponse` |
| 7 | 예외 → `ApiError` | `ApiExceptionHandler` | — | 위 `code` |

JSON 문법·타입 오류는 `INVALID_REQUEST`와 빈 `errors[]`로 응답합니다. 개별 파라미터의 누락·타입 변환 오류도 `INVALID_REQUEST`이지만, 해당 필드 정보가 포함됩니다. 입력 제약 위반은 `INVALID_INPUT`과 필드별 오류로 응답합니다.

`@ModelAttribute`는 타입 변환 실패도 `BindingResult`에 담아 검증 오류와 함께 처리합니다. 따라서 같은 `age=abc`도 `@RequestParam`에서는 `INVALID_REQUEST`, `@ModelAttribute`에서는 `INVALID_INPUT`입니다. 이 차이는 `ErrorResponseContractTest.sameWrongAgeIsClassifiedByBindingStyle`에서 검증합니다.

화면(`lab.js`)에는 상태 코드와 Content-Type, 응답 본문을 표시합니다. 본문은 성공 여부와 관계없이 `textContent`로 출력합니다. 오류 응답의 `errors[]`에 `field`가 `name`·`age`인 항목이 있으면 해당 입력칸에 `aria-invalid`와 오류 문구를 적용합니다.

## 4. 변경 영향 범위

| 바꾸려는 것 | 함께 확인할 곳 |
|---|---|
| 나이 범위(0~150)·이름 길이(40) | `request/Star.java` 애노테이션·메시지 · `RequestController`의 `@Min`·`@Max`·`@Size`(개별 인자용, 두 곳이 같아야 함) · `ApiExceptionHandler.constraints()` 문구 · `@Tag` 설명 · `PRACTICE.md` 2절 · `MvcLabIntegrationTest` 경계값·`longName` · `browser-smoke.mjs` 오류 시나리오 |
| `ApiError` 필드 추가·이름 변경 | `lab.js`의 `message`·`errors`·`field` 접근 · `MvcLabIntegrationTest`·`ErrorResponseContractTest`의 jsonPath · `PRACTICE.md` 2절 예시 (OpenAPI 스키마는 자동 반영) |
| 오류 `code` 값 | `ApiExceptionHandler`의 두 switch · 3절 표 · `PRACTICE.md` 2절 · 테스트의 `$.code` |
| `BindingResponse` 필드 이름 | `lab.js` 표시 · 테스트 `assertBinding` · `PRACTICE.md` 1절 예시 |
| 실습 URL(`/hello/request/**` 등) | `lab.js` URL 조립 · `springdoc.paths-to-match` · `browser-smoke.mjs` · `ci.yml`의 curl · 문서 표 |
| Jackson 변환 설정 | `application.properties` · `invalidJsonFormat`·`jsonTypeMismatchFailsBeforeObjectExists` 테스트 · `PRACTICE.md` JSON 절 |
| 색상·간격 토큰 | `lab.css`의 `:root` · `DESIGN.md` frontmatter · `check-ui.mjs`가 둘의 일치를 검사 |
| Spring Boot 버전 | `build.gradle` · 부모 `ResponseEntityExceptionHandler`가 다루는 예외 목록이 바뀔 수 있으므로 `wrongContentTypeAndMethod`·`ErrorResponseContractTest` 결과를 먼저 확인 |

요청 DTO나 바인딩 규칙을 바꾸면 주로 `request/`를 확인해야 합니다. 공통 예외 처리(`error/`)나 Jackson 설정을 바꾸면 `controller/`·`response/`·`html/`의 응답도 함께 확인합니다.

[프로젝트 소개](../README.md) · [실습 가이드](PRACTICE.md) · [개선 기록](IMPROVEMENTS.md)
