# 폴더 구조 안내

루트에는 프로젝트 소개와 빌드·배포 진입점을 두고, 문서는 `docs`, 화면 검증 도구는 `tools/ui`, 애플리케이션은 `src`에서 관리합니다.

## 서버 코드

기준 경로는 `src/main/java/com/sparta/springmvc/`입니다. Spring Boot의 표준 디렉터리를 유지하고 실습 주제별로 패키지를 나눴습니다.

| 패키지·파일 | 역할 |
|---|---|
| `SpringMvcApplication.java` | 애플리케이션 시작 |
| `config/` | OpenAPI 설명과 Swagger 설정 |
| `controller/` | GET·POST·PUT·DELETE 라우팅 예제 |
| `request/` | 경로·쿼리·폼·JSON 요청 바인딩과 DTO |
| `response/` | Controller·RestController의 문자열·객체 응답 비교 |
| `html/` | 정적 HTML·템플릿·모델·리다이렉트 예제 |
| `error/` | 공통 오류 응답과 예외 처리 |

요청·응답 예제의 `Star`는 각 실습에서 사용하는 별도 DTO입니다. 실제 도메인 모델이나 DB 엔티티가 아닙니다.

## 화면과 설정

기준 경로는 `src/main/resources/`입니다.

| 경로 | 역할 |
|---|---|
| `templates/index.html` | 요청·응답 실습 홈 |
| `templates/hello.html` · `hello-visit.html` | Thymeleaf 렌더링 예제 |
| `static/js/lab.js` | 요청 미리보기·전송·응답 표시·오류 복구 |
| `static/css/lab.css` | 공통 스타일·반응형·접근성 상태 |
| `static/hello.html` | 정적 HTML 응답 예제 |
| `application.properties` | 로컬 기본 설정 |
| `application-render.properties` | Render 포트와 프록시 설정 |

## 검증 도구

| 위치 | 역할 |
|---|---|
| `src/test/java/com/sparta/springmvc/` | JUnit·MockMvc 서버 테스트 40개 |
| `tools/ui/check-ui.mjs` | 디자인 토큰·레이블·안전한 출력 검사 |
| `tools/ui/browser-smoke.mjs` | 실제 화면과 Swagger API 실행 검증 |
| `tools/ui/package.json` · `package-lock.json` | 검증 도구의 명령과 의존성 버전 |
| `tools/ui/premium-ui.json` | UI 검증의 대상 경로와 실행 명령 |
| `.github/workflows/ci.yml` | 빌드부터 512 MB Docker 실행까지 자동 검증 |

Node.js는 검증할 때만 필요합니다. 서버는 Gradle로 실행합니다. 검증 캡처와 로그는 `build/`에 생성하며 버전 관리에서 제외합니다. 문서용으로 선별한 화면만 `docs/images/`에 보관합니다.

## 루트에 남긴 파일

`build.gradle`, `settings.gradle`, `gradlew`, `gradlew.bat`, `gradle/wrapper/`는 빌드 진입점입니다. `Dockerfile`과 `render.yaml`은 현재 Render 설정이 참조하는 위치를 유지합니다. `.gitignore`, `.gitattributes`, `.dockerignore`는 Git과 Docker의 기본 동작을 설정합니다.

[문서 목록](README.md) · [프로젝트 소개](../README.md)
