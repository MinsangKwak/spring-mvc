# 프로젝트 문서 안내

문서는 구현 코드를 읽기 전에 관심 영역과 검증 방법을 빠르게 찾도록 구성했습니다. 실행 코드는 저장소 루트의 `backend-java/`와 `frontend-thymeleaf/`, 문서·테스트·검증 도구는 `project/`에서 관리합니다.

## 무엇이 궁금한가요?

| 관심 영역 | 먼저 읽을 문서 | 바로 확인하는 방법 |
|---|---|---|
| 요청이 라우팅·바인딩·검증·응답으로 이어지는 구조 | [구조와 요청 흐름](dev/architecture.md) | `./backend-java/gradlew -p backend-java test`와 `project/tests/backend-java/` |
| 경로·쿼리·폼·JSON 요청과 오류를 직접 비교하는 방법 | [실습 예제](dev/practice.md) | `./backend-java/gradlew -p backend-java bootRun` 후 웹 실습·Java Swagger |
| 반응형 레이아웃·레이블·포커스·상태 안내 기준 | [UI·접근성 가이드](design/guide.md) | `npm --prefix project run check:ui`과 브라우저 검사 |
| 입력 제한·XSS·오류 노출·CSP·Swagger 방어 범위 | [보안 설계와 검증](dev/security.md) | `SecurityContractTest`, `JsonBodySizeFilterTest`, `npm audit` |
| Docker·Render 설정과 배포 후 확인 경로 | [Render 배포](dev/deployment.md) | `docker build -f backend-java/Dockerfile` 후 `/`, `/java-swagger`, `/v3/api-docs` 확인 |

## `project/`의 역할

| 위치 | 역할 | 실행 코드와 연결되는 지점 |
|---|---|---|
| `docs/` | 현재 구조·실습·디자인·보안·배포 근거 | 구현 파일과 테스트의 읽기 순서 제공 |
| `tests/backend-java/` | JUnit·MockMvc 계약 테스트 | Gradle `test` source set |
| `tests/frontend-thymeleaf/` | 실제 서버 대상 Playwright 흐름 | `package.json`의 `test:browser` |
| `tools/` | UI 정적 검사와 Git 제외 규칙·Render 참고 설정 | `package.json`의 `check:ui`, `setup_git.py`, `render.yaml` |
| `package.json` · `package-lock.json` | 검증 전용 Node 명령과 고정 의존성 | CI의 npm 캐시·설치 기준 |

Node.js와 Playwright는 검증에만 사용합니다. Spring Boot 실행과 Render 컨테이너에는 필요하지 않으며, Docker 빌드 컨텍스트에서도 `project/`를 제외합니다.

## 검증 명령

모든 명령은 저장소 루트에서 실행합니다.

```bash
python3 project/tools/setup_git.py
./backend-java/gradlew -p backend-java clean build
npm ci --prefix project --ignore-scripts
npm --prefix project run check:ui
```

서버 실행 후 브라우저 흐름을 확인합니다.

```bash
npm --prefix project exec -- playwright install chromium
npm --prefix project run test:browser
```

검증용 Node 의존성의 high 이상 advisory는 다음 명령으로 확인합니다.

```bash
npm audit --prefix project --audit-level=high
```

`backend-java/build.gradle`은 `project/tests/backend-java/java`를 테스트 소스로 지정합니다. CI는 JUnit XML과 실행된 `testcase`가 없으면 실패해 `NO-SOURCE` 통과를 허용하지 않습니다. 브라우저 캡처·테스트 보고서·로그는 버전 관리하지 않는 `backend-java/build/` 아래에 생성됩니다.

[프로젝트 소개](../../README.md) · [작업 규칙](AGENTS.md)
