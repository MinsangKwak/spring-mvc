# 문서·검증 안내

`docs/`는 기술 문서, 자동 테스트, 검증 도구와 검증 전용 Node 의존성을 함께 관리합니다. 애플리케이션 실행 코드는 `backend-java/`와 `frontend-thymeleaf/`에 두고, 검증 자산은 제품 코드와 분리해 이 폴더에서 찾을 수 있게 했습니다.

## 저장소 폴더 역할

| 위치 | 역할 |
|---|---|
| `../backend-java/` | Java 17·Spring MVC 서버 코드, 설정과 Gradle 빌드 파일 |
| `../frontend-thymeleaf/` | Thymeleaf 템플릿, HTML·CSS·JavaScript 정적 자산 |
| `tests/backend-java/` | Gradle `test` source set으로 실행하는 JUnit·MockMvc 테스트 |
| `tests/frontend-thymeleaf/` | 실행 중인 서버를 확인하는 Playwright 브라우저 테스트 |
| `tools/` | 디자인 토큰·레이블·상태·안전한 출력의 정적 검사와 설정 |
| `package.json` · `package-lock.json` | UI 정적·브라우저 검증 전용 Node 명령과 고정 의존성 |
| `diagrams/` | 현재 요청·응답 구조를 설명하는 벡터 다이어그램 |
| `../.github/workflows/ci.yml` | Gradle, UI, 브라우저와 Docker 검증을 연결하는 자동화 |
| `../Dockerfile` · `../render.yaml` | 루트 JAR 경로를 유지하는 컨테이너·Render 배포 설정 |

Node.js와 Playwright는 검증에만 사용합니다. Spring Boot 실행과 Render 컨테이너에는 필요하지 않으며, Docker 빌드 컨텍스트에서도 `docs/`를 제외합니다.

## 문서 지도

| 문서 | 확인할 내용 |
|---|---|
| [구조와 설계 지도](STRUCTURE.md) | 서버·화면 코드 지도, 요청 처리 순서, 변경 영향 범위 |
| [실습 가이드](PRACTICE.md) | 바인딩·오류·응답·HTML·Swagger 실행 예제 |
| [현재 구현과 검증](IMPROVEMENTS.md) | 해결한 문제, 설계 결정, 자동 검증 범위 |
| [UI 디자인 기준](DESIGN.md) | 디자인 토큰, 반응형 레이아웃, 퍼블리싱 계약 |
| [보안 설계와 검증](SECURITY.md) | 공격면, 실제 방어, 검증 근거와 운영 경계 |
| [Render 배포](DEPLOYMENT.md) | 컨테이너와 공개 배포 설정·확인 경로 |
| [작업 안내](AGENTS.md) | 폴더 규칙, 변경 시 검증, 보안·주석 기준 |

## 실행·검증 명령

모든 명령은 저장소 루트에서 실행합니다.

| 목적 | 명령 |
|---|---|
| 애플리케이션 실행 | `./gradlew bootRun` |
| Java 빌드·테스트 | `./gradlew clean build` |
| UI 검증 의존성 설치 | `npm ci --prefix docs --ignore-scripts` |
| UI 정적 계약 검사 | `npm --prefix docs run check:ui` |
| Chromium 설치 | `npm --prefix docs exec -- playwright install chromium` |
| 브라우저 흐름 검사 | 서버 실행 후 `npm --prefix docs run test:browser` |
| Node 고위험 의존성 감사 | `npm audit --prefix docs --audit-level=high` |
| 배포 이미지 빌드 | `docker build -t spring-mvc-lab .` |

`backend-java/build.gradle`은 `docs/tests/backend-java/java`를 테스트 소스 경로로 지정합니다. 따라서 테스트를 `docs/`에 두더라도 루트 `./gradlew clean build`가 실제 Java 테스트를 컴파일하고 실행합니다. CI는 JUnit XML과 실행된 `testcase`가 없으면 실패해 `NO-SOURCE` 통과를 허용하지 않습니다. 브라우저 캡처와 테스트 보고서는 버전 관리하지 않는 루트 `build/` 아래에 생성됩니다.

[프로젝트 소개](../README.md)
