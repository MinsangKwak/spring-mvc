# 보안 설계와 검증

이 문서는 현재 공개 학습 앱의 실제 공격면과 코드에 적용한 방어를 설명합니다. 이 앱은 인증·DB·파일 업로드·사용자별 비밀 데이터를 다루지 않고, POST·PUT·DELETE 예제도 상태를 저장하거나 변경하지 않습니다.

## Backend · Java

| 공격면 | 적용한 방어 | 근거와 검증 |
|---|---|---|
| 누락·범위·타입 오류 | 이름 1~40자, 나이 0~150 제약과 요청 형식별 `consumes`·`produces` 고정 | `RequestController`, `Star`, `MvcLabIntegrationTest` |
| JSON 입력 자원 사용 | 전체 본문 16 KB, 문자열 200자, 숫자 20자, 중첩 20단계 제한. Content-Length가 없거나 신뢰할 수 없는 요청도 제한된 크기만 읽고 정상 본문은 캐시해 MVC에 전달 | `JsonBodySizeFilter`, `InputLimitsConfig`, 필터 단위·MockMvc 계약 테스트 |
| 폼·멀티파트 입력 | URL 인코딩 폼 본문 16 KB, 파라미터 32개 제한. 사용하지 않는 multipart 비활성화 | `application.properties` |
| 내부 오류 노출 | 공통 `ApiError`만 반환하고 분류되지 않은 예외의 원인은 서버 로그에 기록. 기본 오류 메시지·바인딩 오류·스택·예외명 비공개 | `ApiExceptionHandler`, `application.properties`, `ErrorResponseContractTest` |
| 전달 헤더 변조 | OpenAPI `servers`를 상대 URL `/`로 고정해 `Forwarded`·`X-Forwarded-Host`를 문서에 반사하지 않음 | `OpenApiConfig`, `SecurityContractTest.forwardedHostIsNotPublishedInOpenApi` |
| 교차 출처 읽기 | CORS 허용 매핑을 만들지 않고, CSP의 `connect-src`를 같은 출처로 제한 | hostile Origin 응답에 `Access-Control-Allow-Origin`이 없음을 계약 테스트로 확인 |

## Frontend · Thymeleaf

| 공격면 | 적용한 방어 | 근거와 검증 |
|---|---|---|
| 응답·오류 기반 XSS | 사용자 입력과 서버 응답을 `textContent`로 출력하고 `innerHTML`을 사용하지 않음 | `lab.js`, hostile HTML 입력 브라우저 시나리오 |
| 서버 렌더링 삽입 | 동적 모델 값은 Thymeleaf `th:text`로 출력 | `hello-visit.html` |
| 외부 코드 로드 | 실습 화면은 `script-src 'self'`, `style-src 'self'` CSP를 사용 | `SecurityHeadersFilter`, 헤더 계약 테스트 |
| 클릭재킹·MIME 추측 | CSP `frame-ancestors 'none'`, `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff` | HTML·API 대표 응답의 헤더 계약 테스트 |
| 반사 응답 캐시 | `Cache-Control: no-store, max-age=0`과 `Pragma: no-cache` 적용 | 헤더 계약 테스트 |

## Java Swagger

공개 URL은 `/java-swagger`이며, Springdoc이 제공하는 같은 출처의 Swagger UI 번들과 `/v3/api-docs`만 사용합니다. 주소가 `/swagger-ui/index.html`로 바뀌지 않도록 별도 HTML 셸과 초기화 스크립트를 두었습니다. 기존 `/swagger-ui.html` 요청은 `/java-swagger`로 리다이렉트합니다.

Try it out은 이 학습 앱의 핵심 기능이므로 유지합니다. 실행 대상은 DB나 외부 시스템을 변경하지 않는 실습 API이며 입력 검증과 요청 형식 제한을 동일하게 거칩니다. 원격 validator 전송, 쿼리 문자열을 통한 설정 교체, 인증값 보존은 비활성화했습니다. Swagger UI가 생성하는 inline style만 전용 CSP에서 허용하고, inline script와 외부 출처 스크립트는 허용하지 않습니다.

## 공통 응답 헤더

`SecurityHeadersFilter`는 다음 정책을 모든 응답에 적용합니다.

- `Content-Security-Policy`: 앱과 Swagger 경로를 분리한 같은 출처 정책
- `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`
- `Referrer-Policy: no-referrer`, 최소 권한 `Permissions-Policy`
- `Cross-Origin-Opener-Policy: same-origin`, `Cross-Origin-Resource-Policy: same-origin`
- `Cache-Control: no-store, max-age=0`, `Pragma: no-cache`
- 프록시 전달 헤더가 정규화된 뒤 HTTPS로 판별된 요청에만 `Strict-Transport-Security`

## 의존성과 검증 범위

- `npm audit --prefix project --audit-level=high`로 검증용 Node 의존성의 high 이상 advisory를 확인합니다.
- Spring Boot 3.5.16은 Spring Framework 6.2.19 이상을 사용합니다. Spring 공식 보안 공지에서 확인한 최근 조건부 이슈는 이 저장소에 없는 Actuator, Spring Security, Jersey, Cloud Foundry, WebFlux, FreeMarker 또는 해당 뷰 처리 구성이 필요해 현재 코드 경로와 일치하지 않았습니다.
- Springdoc 2.8.17은 현재 사용 중인 Spring Boot 3.5 계열용 릴리스입니다.
- GitHub Actions는 Gradle 의존성을 해결해 빌드·테스트·브라우저·Docker 검증을 수행합니다. 별도 JVM advisory DB 스캐너는 구성하지 않았으므로 의존성 알림은 GitHub dependency graph와 공식 Spring 공지를 함께 확인해야 합니다.

`project/tests/backend-java`의 `SecurityContractTest`는 헤더, HSTS, CORS 비허용, 입력 제한, 전달 헤더 비반사, 새·구 Swagger 경로를 검증합니다. `JsonBodySizeFilterTest`는 Content-Length가 없는 입력의 16 KB 제한과 정상 본문 전달을 확인합니다. `project/tests/frontend-thymeleaf/browser-smoke.mjs`는 hostile HTML 입력이 DOM 노드가 되지 않는지와 `/java-swagger`에서 JSON·폼 요청이 실제 실행되는지를 확인합니다.

## 남은 운영 경계

이 저장소는 애플리케이션 계층의 방어를 다룹니다. Render 앞단의 요청 속도 제한, DDoS 완화, TLS 종료와 프록시의 전달 헤더 정규화는 배포 플랫폼 설정 범위입니다. 인증·CSRF 보호는 사용자 권한이나 상태 변경 기능을 추가할 때 함께 설계해야 합니다. 이 검토는 침투 테스트나 전체 공급망 SCA를 대신하지 않습니다.

## 공식 참고

- [Spring 보안 공지](https://spring.io/security/)
- [Spring Boot 3.5 시스템 요구사항](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [springdoc 속성](https://springdoc.org/properties.html)
- [Spring Security 응답 헤더 설명](https://docs.spring.io/spring-security/reference/servlet/exploits/headers.html)

[프로젝트 소개](../../../README.md) · [문서 안내](../README.md) · [구조와 설계 지도](architecture.md)
