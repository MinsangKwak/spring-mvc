# Spring MVC Request Lab

같은 값을 경로·쿼리·폼·JSON으로 보내며, 요청 형식에 따라 Spring MVC가 값을 읽고 정상·오류 응답을 반환하는 차이를 직접 비교하는 웹 실습입니다.

**곽민상 · 개인 학습 프로젝트 · UI·Java API·검증·배포 구성**

Java 17, Spring Boot 3.5.16, Spring MVC, Thymeleaf, HTML, CSS, JavaScript를 사용합니다. 반응형 비교 패널과 입력 레이블, 필드 오류, 키보드 초점, 상태 알림을 구현했습니다.

[웹 실습](https://spring-mvc-k14y.onrender.com/) · [Java Swagger](https://spring-mvc-k14y.onrender.com/java-swagger) · [상세 포트폴리오](https://www.notion.so/401cbbdd109b816ea6caeef336df0802)

무료 배포 환경은 비활동 시 정지할 수 있어 첫 접속이 늦을 수 있습니다. 입력값은 DB에 저장하지 않습니다.

## 폴더별 역할

| 위치 | 담당하는 일 | 먼저 볼 구현·문서 |
|---|---|---|
| `backend-java/` | 바인딩 6종, Bean Validation, 공통 `ApiError`, 입력 제한, OpenAPI와 Gradle·Docker 실행 구성 | [`RequestController`](backend-java/src/main/java/com/sparta/springmvc/request/RequestController.java) · [구조와 요청 흐름](project/docs/dev/architecture.md) |
| `frontend-thymeleaf/` | 요청 미리보기·응답 검사기, 반응형 레이아웃과 접근성 상태 | [`index.html`](frontend-thymeleaf/src/main/resources/templates/index.html) · [`lab.css`](frontend-thymeleaf/src/main/resources/static/css/lab.css) · [UI 기준](project/docs/design/guide.md) |
| `.github/` | Gradle·UI·브라우저·Docker 자동 검증 | [`ci.yml`](.github/workflows/ci.yml) · [검증 범위](project/docs/dev/practice.md#자동-검증-범위) |
| `project/docs/` | 구조·실습·디자인·보안·배포 근거 | [문서 안내](project/docs/README.md) |
| `project/tests/` | JUnit·MockMvc 계약과 Playwright 사용자 흐름 | [Java 테스트](project/tests/backend-java/java/com/sparta/springmvc) · [브라우저 테스트](project/tests/frontend-thymeleaf/browser-smoke.mjs) |
| `project/tools/` | UI 정적 검사와 Git 제외 규칙·Render 참고 설정 | [`check-ui.mjs`](project/tools/check-ui.mjs) · [배포 기준](project/docs/dev/deployment.md) |

Gradle 프로젝트와 Wrapper는 `backend-java/`에 있습니다. source set이 화면 자산과 `project/tests/backend-java`를 연결하며 JAR·테스트 보고서는 `backend-java/build/`에 생성됩니다.

## 직접 비교할 수 있는 것

| 주제 | 실습에서 확인하는 차이 |
|---|---|
| 요청 바인딩 | `@PathVariable`, `@RequestParam`, `@ModelAttribute`, `@RequestBody`의 입력 위치와 변환 |
| 검증과 오류 | 누락·범위·타입·JSON 문법·Content-Type 오류의 상태 코드와 공통 응답 |
| 응답 형식 | `@Controller`·`@RestController`, 문자열·객체 응답의 Content-Type |
| HTML 응답 | 정적 HTML, Thymeleaf 모델, 인스턴스 메모리 카운터, 리다이렉트 |
| API 문서 | `/java-swagger`에서 JSON·폼 요청을 실제 서버로 실행 |

DB, 인증, 영속 CRUD와 운영 트래픽 성능 측정은 학습 범위에 포함하지 않습니다.

## 실행

Java 17 JDK가 필요합니다. 저장소 루트에서 공통 제외 규칙을 한 번 적용한 뒤 실행합니다.

```bash
python3 project/tools/setup_git.py
./backend-java/gradlew -p backend-java bootRun
```

Windows에서는 첫 명령을 `py -3 project\tools\setup_git.py`, 두 번째 명령을 `.\backend-java\gradlew.bat -p backend-java bootRun`으로 실행합니다. 기본 주소는 `http://localhost:8080/`입니다.

## 검증

```bash
./backend-java/gradlew -p backend-java clean build
npm ci --prefix project --ignore-scripts
npm --prefix project run check:ui
```

브라우저 흐름은 서버를 실행한 상태에서 확인합니다.

```bash
npm --prefix project exec -- playwright install chromium
npm --prefix project run test:browser
```

GitHub Actions는 Java 테스트 결과가 없으면 실패하며, UI 정적 검사·브라우저 흐름·512 MB Docker 실행을 함께 검증합니다. 자세한 명령과 읽을 순서는 [프로젝트 문서 안내](project/docs/README.md)에 정리했습니다.

## 접근성·보안 범위

화면은 명시적 레이블, 필드 오류 연결, 라이브 상태 안내, 키보드 포커스, 좁은 화면 재배치와 안전한 `textContent` 출력을 사용합니다. 서버는 입력 크기·구조 제한, 공통 오류 응답, 같은 출처 CSP와 보안 헤더를 적용합니다. 자동 검사는 회귀 확인용이며 전체 웹 접근성 인증, 침투 테스트 또는 전체 공급망 SCA를 의미하지 않습니다.

[UI 기준](project/docs/design/guide.md) · [실습 예제](project/docs/dev/practice.md) · [보안 설계](project/docs/dev/security.md) · [Render 배포 상태·전환 절차](project/docs/dev/deployment.md)
