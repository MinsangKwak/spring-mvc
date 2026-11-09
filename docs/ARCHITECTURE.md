# 설계 지도

이 문서는 코드를 읽기 전에 "무엇이 어디서 어떻게 바뀌는가"를 한 장으로 파악하기 위한 지도입니다. 2026-11-09 기준 코드만 설명하며, 코드에 없는 기능은 적지 않습니다.

## 1. 왜 이 기술인가

이 프로젝트의 작성자는 6년간 웹 퍼블리싱·프론트엔드를 해 왔습니다. 폼을 만들고 `fetch`로 JSON을 보내는 일은 익숙하지만, 그 데이터가 서버에서 **어느 단계에서 읽히고, 어느 단계에서 거부되며, 왜 같은 잘못된 값인데 오류 응답 모양이 다른지**는 서버 코드를 직접 짜 보지 않으면 알 수 없습니다.

Spring MVC를 고른 이유는 세 가지입니다.

| 이유 | 설명 |
|---|---|
| 요청 처리 단계가 분리되어 있다 | 라우팅(`@RequestMapping`) → 인자 바인딩(`@PathVariable`·`@RequestParam`·`@ModelAttribute`·`@RequestBody`) → 검증(`@Valid`·`@Validated`) → 예외 변환(`@RestControllerAdvice`)이 각각 다른 확장 지점이라, 어느 단계에서 실패했는지 예외 타입으로 구분할 수 있습니다. |
| 프론트엔드에서 보는 현상의 원인을 서버 쪽에서 확인할 수 있다 | "숫자를 보냈는데 400"이 Jackson 변환 실패인지, Bean Validation 위반인지, Content-Type 불일치인지를 코드로 재현합니다. |
| 실무 환경과 같다 | 작성자가 퍼블리싱을 맡아 온 금융 웹 서비스의 서버가 Java·Spring 계열이라, 개발팀과 같은 용어로 문제 원인을 이야기하기 위해서입니다. |

목표는 "Spring을 쓸 줄 안다"가 아니라 **"요청 하나가 어떤 단계를 거쳐 응답이 되는지 설명하고, 어긋났을 때 어느 단계를 봐야 하는지 아는 것"**입니다. 그래서 기능 수를 늘리는 대신 같은 데이터를 네 가지 방식으로 보내고 결과를 비교하는 구조로 범위를 고정했습니다.

## 2. 핵심 개념 지도

### 2-1. 핵심 데이터

이 프로젝트에는 DB 엔티티가 없습니다. 모든 데이터는 요청·응답을 위한 DTO입니다.

| 타입 | 위치 | 필드 | 역할 |
|---|---|---|---|
| `Star` (request) | `request/Star.java` | `name: String` (`@NotBlank`, `@Size(max=40)`) · `age: Integer` (`@NotNull`, `@Min(0)`, `@Max(150)`) | 입력 DTO. 제약과 오류 문구의 원본 |
| `BindingResponse` | `request/BindingResponse.java` | `binding: String` · `name: String` · `age: Integer` | 바인딩 성공 응답. `binding`은 서버가 값을 읽은 애노테이션 이름 |
| `ApiError` | `error/ApiError.java` | `code: String` · `message: String` · `errors: List<FieldError>` | 모든 오류 응답의 단일 형식 |
| `ApiError.FieldError` | 〃 | `field: String` · `message: String` | 화면이 입력칸을 찾는 열쇠. `field`는 `name` 또는 `age` |
| `Star` (response) | `response/Star.java` | `name: String` · `age: int` | 응답 직렬화 비교용. 제약 없음. 요청 DTO와 일부러 분리 |
| `MethodResponse` | `controller/HelloController.java` | `method` · `message` | HTTP 메서드 라우팅 확인용 |

`age`가 요청 DTO에서는 `Integer`(null 허용 → `@NotNull`로 누락 검출), 응답 DTO에서는 `int`인 것은 의도입니다. 원시형이면 누락과 0을 구분할 수 없습니다.

### 2-2. 핵심 프로세스: 요청 한 건이 응답이 되기까지

JSON 요청 `POST /hello/request/form/json`을 기준으로, 번호는 Spring MVC가 실제로 밟는 순서입니다. 각 단계에서 실패하면 **그 단계의 예외**가 발생하고, 이후 단계는 실행되지 않습니다.

| # | 단계 | 담당 | 실패 시 예외 | 응답 (`code`) |
|---|---|---|---|---|
| 1 | 라우팅: 경로·메서드 매칭 | `DispatcherServlet` → `@RequestMapping` | `NoResourceFoundException` / `HttpRequestMethodNotSupportedException` | 404 `NOT_FOUND` / 405 `METHOD_NOT_ALLOWED` |
| 2 | 미디어 타입 확인: `consumes`·`produces` | 〃 | `HttpMediaTypeNotSupportedException` / `HttpMediaTypeNotAcceptableException` | 415 `UNSUPPORTED_MEDIA_TYPE` / 406 `NOT_ACCEPTABLE` |
| 3 | 객체 만들기(바인딩): 본문·파라미터 → Java 값 | `@RequestBody`는 Jackson, `@ModelAttribute`는 `DataBinder`, `@RequestParam`·`@PathVariable`은 `ConversionService` | JSON: `HttpMessageNotReadableException` · 개별 인자: `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException` · `@ModelAttribute`: 오류가 `BindingResult`에 쌓여 4단계와 합쳐짐 | 400 `INVALID_REQUEST` (JSON·개별 인자) |
| 4 | 검증: 만들어진 값에 제약 적용 | 객체 인자는 `@Valid` → Bean Validation, 개별 인자는 클래스 `@Validated` → 메서드 검증 | `MethodArgumentNotValidException` / `ConstraintViolationException` | 400 `INVALID_INPUT` + `errors[]` |
| 5 | 핸들러 실행 | `RequestController.json()` | 분류되지 않은 예외 | 500 `INTERNAL_ERROR` |
| 6 | 응답 직렬화 | `@ResponseBody` → Jackson | — | 200 `BindingResponse` |
| 7 | 예외 → `ApiError` 변환 | `ApiExceptionHandler` (1~5단계의 모든 예외가 여기로 모임) | — | 위 표의 `code` |

핵심 규칙 하나: **3단계 실패는 객체가 없으므로 필드 정보가 없을 수 있고(`INVALID_REQUEST`), 4단계 실패는 객체가 있으므로 필드별 오류가 있다(`INVALID_INPUT`).** 단, `@ModelAttribute`는 타입 변환 실패도 `BindingResult`에 담아 4단계 예외로 던지기 때문에, 같은 `age=abc`라도 `@RequestParam`은 `INVALID_REQUEST`, `@ModelAttribute`는 `INVALID_INPUT`이 됩니다. 이 차이는 `ErrorResponseContractTest.sameWrongAgeIsClassifiedByBindingStyle`로 고정했습니다.

### 2-3. 화면과 서버의 계약

`static/js/lab.js`는 응답을 다음 두 가지로만 해석합니다.

- `response.ok`이면 본문을 `textContent`로 그대로 표시한다.
- 아니면 `data.errors[]`를 돌며 `field`가 `name`·`age`인 항목을 해당 입력칸의 `aria-invalid`와 오류 문구에 연결한다.

따라서 서버와 화면이 공유하는 계약은 `ApiError`의 세 필드와 `FieldError.field`의 값 집합뿐입니다.

## 3. 설계 트레이드오프

실제로 선택한 네 가지와, 선택하지 않은 쪽을 적습니다.

### 3-1. 요청 DTO와 응답 DTO를 같은 이름 `Star`로 두 패키지에 분리

- 선택: `request.Star`(제약 있음, `Integer`)와 `response.Star`(제약 없음, `int`)를 따로 둠.
- 대안: 하나의 `Star`를 공유.
- 이유: 응답 예제는 "객체가 어떻게 JSON이 되는가"만 보여 줘야 하는데, 공유하면 검증 애노테이션과 Swagger 스키마 설명이 응답 예제에도 붙습니다. 중복 코드 10여 줄을 감수하고 각 실습의 관심사를 분리했습니다.
- 비용: 같은 이름이 두 개라 import를 잘못 잡을 수 있습니다. 패키지 경로로 구분합니다.

### 3-2. 오류 응답을 `ResponseEntityExceptionHandler` 상속으로 통일

- 선택: Spring이 이미 상태 코드로 분류해 둔 표준 예외 처리(`ResponseEntityExceptionHandler`)를 상속하고, `handleExceptionInternal` 한 곳에서 `ApiError`로 바꿈. Spring Boot 기본 `/error` 본문(`timestamp`·`status`·`error`·`path`)은 사용하지 않음.
- 대안 A: Boot 기본 오류 본문을 그대로 사용 → 화면이 필드별 오류를 받을 수 없음.
- 대안 B: 예외마다 `@ExceptionHandler`를 따로 작성 → 상태 코드 분류를 Spring과 중복으로 유지해야 함.
- 비용: 부모 클래스가 어떤 예외를 어떤 상태로 보내는지 알아야 디버깅이 됩니다. 그 지도가 2-2 표입니다.

### 3-3. Jackson 자동 변환을 끄고 타입을 엄격하게

- 선택: `allow-coercion-of-scalars=false`, `accept-float-as-int=false`, `fail-on-unknown-properties=true`. `"33"`과 `33.5`와 모르는 속성은 400.
- 대안: 기본값(관대한 변환) 유지. 폼 바인딩처럼 `"33"`도 33으로 읽힘.
- 이유: 이 프로젝트의 목적이 "폼은 문자열, JSON은 타입이 있다"는 차이를 보이는 것이므로, 관대한 변환은 학습 목적을 지웁니다. 실제 서비스라면 클라이언트 호환성을 위해 반대로 결정할 수 있습니다.
- 비용: 프론트엔드가 `age`를 문자열로 보내면 실패합니다. 화면 코드는 숫자로 읽히는 입력만 `Number()`로 바꾸고, `abc` 같은 값은 문자열 그대로 보내 서버의 3단계 오류를 관찰할 수 있게 합니다.

### 3-4. 방문 횟수를 인스턴스 메모리의 `AtomicLong`으로

- 선택: `HtmlController`의 필드. 재시작하면 0, 서버가 여러 대면 각자 셈.
- 대안: DB 또는 외부 캐시에 저장.
- 이유: 예제의 목적은 "동적 모델 값이 템플릿에 들어가는 것"과 "공유 변수의 동시성 처리"이지 영속화가 아닙니다. 영속화를 넣으면 DB 설치가 필요해져 실행 장벽이 생깁니다.
- 비용: 수치를 운영 지표로 쓸 수 없습니다. 문서와 화면에 그 한계를 명시했습니다.

## 4. 변경 영향 범위

"X를 바꾸면 어디까지 바뀌는가"를 미리 적어 두면 변경 전에 질문 범위를 정할 수 있습니다.

| 바꾸려는 것 | 함께 바뀌어야 하는 곳 |
|---|---|
| 나이 범위(0~150) 또는 이름 길이(40) | `request/Star.java` 애노테이션과 메시지 · `RequestController`의 `@Min`·`@Max`·`@Size`(개별 인자용, 두 곳이 같아야 함) · `ApiExceptionHandler.constraints()`의 고정 문구 · `@Tag` 설명 · `docs/PRACTICE.md` 2절 표 · `MvcLabIntegrationTest`의 경계값·`longName` · `browser-smoke.mjs`의 오류 시나리오 |
| `ApiError`에 필드 추가·이름 변경 | `lab.js`의 `data.errors`·`error.field` 접근 · OpenAPI 스키마(`@Schema(implementation = ApiError.class)`는 자동 반영) · `MvcLabIntegrationTest`·`ErrorResponseContractTest`의 jsonPath · `docs/PRACTICE.md` 2절 예시 |
| 오류 `code` 값 추가·변경 | `ApiExceptionHandler`의 두 switch · 이 문서 2-2 표 · `docs/PRACTICE.md` 2절 · 테스트의 `$.code` 단언 |
| `BindingResponse` 필드 이름 | `lab.js`(표시) · 테스트 `assertBinding` · `docs/PRACTICE.md` 1절 예시 JSON |
| 실습 URL(`/hello/request/**` 등) | `lab.js`의 URL 조립 · `springdoc.paths-to-match` · `browser-smoke.mjs` · `ci.yml`의 컨테이너 확인 curl · 모든 문서의 표 |
| Jackson 변환 설정 | `application.properties` · `MvcLabIntegrationTest.invalidJsonFormat` · `ErrorResponseContractTest.jsonTypeMismatchFailsBeforeObjectExists` · `docs/PRACTICE.md` JSON 절 |
| 색상·간격 토큰 | `lab.css`의 `:root` · `docs/DESIGN.md`의 frontmatter · `tools/ui/check-ui.mjs`가 둘의 일치를 검사 |
| Spring Boot 버전 | `build.gradle` · 부모 `ResponseEntityExceptionHandler`가 다루는 예외 목록이 바뀔 수 있으므로 `wrongContentTypeAndMethod`·`ErrorResponseContractTest` 결과를 먼저 확인 |

영향 범위 밖: `controller/`·`response/`·`html/` 패키지는 `request/`·`error/`와 코드로 연결되어 있지 않습니다. 요청 바인딩을 바꿔도 HTML·응답 비교 예제는 영향이 없습니다.

## 5. 문제 추적 순서

"응답이 예상과 다르다"를 만났을 때 보는 순서입니다. 2-2의 단계 번호를 거꾸로가 아니라 **앞에서부터** 확인합니다. 앞 단계에서 실패하면 뒤 단계는 실행조차 되지 않기 때문입니다.

1. **상태 코드와 `code`를 먼저 읽는다.** 404·405는 1단계(경로·메서드), 415·406은 2단계(Content-Type·Accept), `INVALID_REQUEST`는 3단계(객체 만들기), `INVALID_INPUT`은 4단계(검증), `INTERNAL_ERROR`는 5단계(핸들러 내부)입니다. 이것만으로 봐야 할 코드가 정해집니다.
2. **요청 미리보기와 실제 요청을 비교한다.** 웹 실습의 미리보기(메서드·URL·헤더·본문) 또는 Swagger의 curl을 그대로 터미널에서 실행합니다. 화면 문제인지 서버 문제인지가 여기서 갈립니다.
3. **같은 입력을 다른 바인딩으로 보내 본다.** JSON에서만 실패하면 Jackson 설정, 폼에서만 실패하면 `DataBinder`·문자열 변환, 둘 다 실패하면 제약 자체를 봅니다.
4. **`errors[].field`가 비어 있으면 3단계 예외다.** `ApiExceptionHandler.handleExceptionInternal`에서 어떤 예외가 들어왔는지 로그 레벨을 올려 확인합니다. `logging.level.org.springframework.web=DEBUG`로 `DispatcherServlet`이 고른 핸들러와 발생한 예외가 출력됩니다.
5. **테스트로 재현해 고정한다.** `MvcLabIntegrationTest` 또는 `ErrorResponseContractTest`에 실패하는 요청을 한 줄 추가하고, 고친 뒤 통과를 확인합니다. 재현되지 않는 문제는 고쳤다고 말하지 않습니다.
6. **500이면 서버 로그를 본다.** `ApiExceptionHandler.unexpected()`가 `ERROR` 레벨로 스택 트레이스를 남깁니다. 응답 본문에는 원인을 싣지 않습니다.

[문서 목록](README.md) · [프로젝트 소개](../README.md)
