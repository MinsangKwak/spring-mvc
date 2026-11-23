# Spring MVC 저장소 작업 안내

이 저장소는 Spring MVC 요청·응답 학습 앱입니다. 현재 구현과 검증 가능한 사실만 유지하고, 과거 화면·릴리스 이력·검증 수치를 현재 근거처럼 추가하지 않습니다.

## 구조 규칙

- Java 실행 코드는 `backend-java/src/main/`, Thymeleaf·HTML·CSS·JavaScript는 `frontend-thymeleaf/src/main/resources/`에서 관리합니다.
- Java 테스트는 `project/tests/backend-java/java/`, 브라우저 테스트는 `project/tests/frontend-thymeleaf/`, 정적 검사 도구는 `project/tools/`에서 관리합니다.
- 검증용 Node 의존성과 명령은 `project/package.json` 하나로 관리합니다. 기능별 중첩 `package.json`을 추가하지 않습니다.
- Markdown은 루트 `README.md`와 기존 라이선스 파일을 제외하고 `project/docs/` 안에 둡니다. 중복 설명은 기존 문서에 합치고 별도 README를 늘리지 않습니다.
- `backend-java/settings.gradle`과 `backend-java/build.gradle`이 두 실행 영역과 `project/tests`를 연결합니다. Gradle Wrapper와 `build/libs/` 산출물은 `backend-java/`에 두고 Docker 빌드 컨텍스트는 저장소 루트를 사용합니다.
- 저장소 공통 제외 규칙은 `project/.gitignore`가 원본이며, 첫 작업 전에 `python3 project/tools/setup_git.py`로 로컬 `.git/info/exclude`에 적용합니다.
- 경로를 바꾸면 Gradle source set, npm script, CI 캐시·명령, 문서 링크와 Docker 제외 규칙을 함께 확인합니다.

## 구현·보안 범위

- 이 앱은 인증·DB·파일 업로드·영속 CRUD를 구현하지 않습니다. POST·PUT·DELETE 예제도 서버 상태를 변경하지 않습니다.
- 사용자 입력과 서버 응답은 HTML로 해석하지 않고 `textContent` 또는 Thymeleaf의 안전한 텍스트 출력을 사용합니다.
- 입력 크기·구조 제한, 공통 오류 응답, 같은 출처 CSP, 보안 헤더와 상대 OpenAPI 서버 경로를 변경할 때는 관련 계약 테스트를 함께 수정합니다.
- `/java-swagger`는 같은 출처 자산과 `/v3/api-docs`를 사용합니다. 원격 validator, 쿼리 설정 덮어쓰기와 인증값 보존을 다시 켜지 않습니다.
- CORS 비허용을 모든 교차 사이트 요청이나 CSRF 방어로 확대 설명하지 않습니다. 전체 접근성 인증, 침투 테스트 또는 전체 공급망 SCA를 완료했다고 표현하지 않습니다.

## 검증 기준

- 서버·Java 변경: `./backend-java/gradlew -p backend-java clean build`
- 템플릿·CSS·JavaScript·디자인 토큰 변경: `npm --prefix project run check:ui`
- 사용자 흐름·포커스·오류·Swagger 변경: 서버 실행 후 `npm --prefix project run test:browser`
- 배포·Gradle·리소스 경로 변경: `docker build -f backend-java/Dockerfile -t spring-mvc-lab .`과 대표 curl 계약 확인
- 경로 통합 변경: Java 테스트가 `NO-SOURCE`가 아니라 실제로 실행됐는지 테스트 보고서 수를 확인

## 작성 기준

- 코드 주석은 필요한 경우에만 한 줄로 작성합니다.
- JavaScript 주석은 `//`를 사용하고, HTML 파일의 스타일 주석은 한 줄 `/* ... */` 안에서 작성합니다.
- 주석에 이모티콘이나 이모지를 사용하지 않습니다.
- 문서와 오류 문구는 결론을 먼저 쓰고 같은 설명을 반복하지 않습니다.
