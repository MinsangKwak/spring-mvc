# Backend · Java

Spring MVC의 라우팅, 요청 바인딩, 입력 검증, 응답 직렬화와 공통 오류 계약을 담당합니다.

## 구조

| 위치 | 역할 |
|---|---|
| `src/main/java/com/sparta/springmvc/controller/` | HTTP 메서드 라우팅 예제 |
| `src/main/java/com/sparta/springmvc/request/` | 경로·쿼리·폼·JSON 바인딩과 입력 DTO |
| `src/main/java/com/sparta/springmvc/response/` | 문자열·객체 응답 비교 |
| `src/main/java/com/sparta/springmvc/html/` | 정적·Thymeleaf·리다이렉트 응답 |
| `src/main/java/com/sparta/springmvc/error/` | 공통 `ApiError`와 예외 분류 |
| `src/main/java/com/sparta/springmvc/security/` | 입력 파서 제한과 응답 보안 헤더 |
| `src/main/java/com/sparta/springmvc/config/` | OpenAPI·Swagger 설정 |
| `src/main/resources/` | 로컬·Render 서버 설정 |
| `src/test/java/com/sparta/springmvc/` | JUnit·MockMvc 계약 테스트 |
| `build.gradle` | Java 17, Spring Boot 의존성과 전체 프로젝트 `sourceSets` |

## 빌드 계약

이 폴더에 빌드 파일을 두지만 Gradle 프로젝트 디렉터리는 저장소 루트입니다. 루트 `settings.gradle`이 이 파일을 연결하고, `sourceSets`가 `frontend-thymeleaf`의 템플릿과 정적 자산을 같은 Spring Boot JAR에 포함합니다.

저장소 루트에서 실행합니다.

```bash
./gradlew clean build
./gradlew bootRun
```

[프로젝트 소개](../README.md) · [구조와 설계 지도](../docs/STRUCTURE.md) · [보안 설계와 검증](../docs/SECURITY.md)
