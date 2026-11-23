# 현재 구현과 검증

현재 코드가 해결하는 문제, 선택한 설계, 자동 검증 범위를 정리합니다. 과거 버전의 화면이나 릴리스 이력 대신 지금 저장소에서 확인할 수 있는 계약만 다룹니다.

## 구현 목적

| 문제 | 현재 구현 | 확인 지점 |
|---|---|---|
| 요청 데이터의 위치와 바인딩 방식 차이를 코드만으로 비교하기 어려움 | 경로·쿼리·폼·JSON 6가지 요청을 같은 이름·나이 응답으로 비교 | `RequestController`, 웹 실습 요청 미리보기 |
| 바인딩 실패와 입력 제약 위반을 클라이언트가 구분하기 어려움 | 공통 `ApiError` 형식과 `INVALID_REQUEST`·`INVALID_INPUT` 분류 | `ApiExceptionHandler`, `ErrorResponseContractTest` |
| 문자열과 객체 응답 차이가 선언에만 드러남 | 실제 Content-Type과 응답 본문을 나란히 표시 | `ResponseController`, `ResponseRestController` |
| 서버 오류가 화면의 필드 상태와 연결되지 않음 | `errors[].field`를 기준으로 문구와 `aria-invalid` 적용 | `lab.js`, `index.html` |
| 응답 문자열을 HTML로 해석할 위험 | 결과를 `textContent`로 출력 | `lab.js`, UI 정적 검사 |
| API 실행을 별도 도구에 의존 | 웹 실습과 Swagger UI를 같은 서버에 제공 | `OpenApiConfig`, `index.html` |
| 서버·화면·배포 계약이 따로 검증됨 | Gradle·UI 정적 검사·Playwright·Docker를 CI 한 흐름으로 연결 | `.github/workflows/ci.yml` |
| 공개 학습 화면의 브라우저·입력 경계가 명시되지 않음 | JSON·폼 입력 제한, 공통 보안 헤더, 상대 OpenAPI 서버 URL과 `/java-swagger` 전용 화면 | `SecurityContractTest`, `JsonBodySizeFilterTest`, `SECURITY.md` |

## 설계 선택

| 결정 | 선택 이유 | 범위와 비용 |
|---|---|---|
| 요청 DTO와 응답 DTO를 `request.Star`·`response.Star`로 분리 | 요청 제약과 응답 직렬화 예제의 책임을 구분 | 유사한 필드가 중복되므로 패키지 경로로 구분해야 함 |
| `ResponseEntityExceptionHandler`를 확장해 `ApiError`로 변환 | Spring의 상태 분류를 활용하면서 클라이언트에 일관된 형식 제공 | Spring Boot 변경 시 상위 처리 규칙과 계약 테스트를 함께 확인해야 함 |
| Jackson의 숫자 문자열·소수 변환과 알 수 없는 속성을 엄격히 처리 | 폼의 문자열 변환과 JSON 타입 검사의 차이를 학습 | 실제 서비스에서는 기존 클라이언트 호환성을 별도로 판단해야 함 |
| 방문 횟수를 인스턴스의 `AtomicLong`으로 관리 | 저장소 없이 모델 전달과 공유 값 동시성에 집중 | 재시작하면 초기화되고 여러 인스턴스 사이에 공유되지 않음 |
| `backend-java`와 `frontend-thymeleaf`로 책임을 분리하되 루트 프로젝트 유지 | 채용 검토자가 서버·화면 코드를 바로 찾고 기존 실행·배포 명령은 유지 | `settings.gradle`과 `sourceSets`가 두 영역을 연결하는 규칙을 관리해야 함 |

## 프런트엔드 구현 계약

- 명시적 `label`, 필드별 오류 요소, `aria-invalid`를 연결합니다.
- 상태 안내는 라이브 영역과 `role="status"`를 사용합니다.
- 응답 본문은 `textContent`로 출력하고 `innerHTML`을 사용하지 않습니다.
- 키보드 포커스는 `:focus-visible`로 구분하며, 강제 색상 모드와 reduced-motion을 고려합니다.
- 760px 이하에서는 탐색과 요청·응답 패널을 세로 흐름으로 재배치합니다.
- 긴 URL과 JSON은 줄바꿈하고 결과 영역은 내용과 화면 폭에 맞춰 스크롤합니다.

이 계약은 자동 검사와 브라우저 시나리오로 회귀를 확인하지만, 모든 웹 접근성 기준 준수나 인증을 의미하지 않습니다.

## 검증 범위

| 계층 | 도구 | 확인 범위 |
|---|---|---|
| Java 단위·통합 | JUnit, MockMvc | 라우팅, 바인딩 6종, 경계값, JSON 변환, 공통 오류, 응답 헤더, HTML 렌더링, 동시 증가 |
| UI 정적 계약 | `frontend-thymeleaf/tools/ui/check-ui.mjs` | 디자인 토큰, 레이블, 필드 오류 요소, 라이브 영역, 포커스·모션 스타일, 안전한 출력 |
| 브라우저 흐름 | `frontend-thymeleaf/tools/ui/browser-smoke.mjs` | 정상·오류 입력, 키보드, 로딩·재시도, 모바일 배치, Swagger 요청 |
| 배포 컨테이너 | Docker, curl | 512 MB 환경의 시작, 상태 경로, Swagger, JSON 바인딩 응답 |

현재 브랜치의 검증 결과는 해당 커밋의 GitHub Actions 실행과 아티팩트를 기준으로 확인합니다. 문서에는 과거 커밋의 테스트 개수나 화면 캡처를 현재 성능·품질 근거로 재사용하지 않습니다.

## 실행 명령

저장소 루트에서 실행합니다.

```bash
./gradlew clean build
npm ci --prefix frontend-thymeleaf/tools/ui
npm --prefix frontend-thymeleaf/tools/ui run check:ui
```

브라우저 검사는 서버 실행 후 수행합니다.

```bash
npm --prefix frontend-thymeleaf/tools/ui exec -- playwright install chromium
npm --prefix frontend-thymeleaf/tools/ui run test:browser
```

GitHub Actions는 동일한 루트 Gradle 명령으로 JAR을 생성하고, 브라우저 검증 후 루트 Dockerfile로 컨테이너를 확인합니다. 산출물과 로그는 `build/` 아래에 생성됩니다.

## 학습 범위

요청·응답 계약, 입력 검증, 오류 처리, API 명세와 실제 요청의 일치, 화면과 서버를 연결한 검증을 다룹니다. DB·인증·영속 CRUD와 운영 환경 성능 검증은 포함하지 않습니다.

[프로젝트 소개](../README.md) · [구조와 설계 지도](STRUCTURE.md) · [실습 가이드](PRACTICE.md) · [보안 설계와 검증](SECURITY.md)
