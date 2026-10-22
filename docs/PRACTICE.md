# Spring MVC 실습 가이드

웹 실습과 Swagger UI는 실행 중인 같은 서버에 요청합니다. 응답을 미리 만들어 보여주는 방식이 아닙니다. 먼저 README의 실행 방법으로 서버를 시작합니다.

## 1. 요청 바인딩 비교

모든 예제에 이름 `민상`, 나이 `33`을 전달해 보세요.

| 방식 | 메서드·경로 | 데이터를 보내는 위치 | 서버에서 읽는 방법 |
|---|---|---|---|
| 경로 | GET `/hello/request/star/{name}/age/{age}` | URL 경로 | `@PathVariable` |
| 쿼리 인자 | GET `/hello/request/form/param` | 쿼리 문자열 | `@RequestParam` |
| 폼 인자 | POST `/hello/request/form/param` | URL 인코딩된 폼 본문 | `@RequestParam` |
| 폼 객체 | POST `/hello/request/form/model` | URL 인코딩된 폼 본문 | `@ModelAttribute` |
| 쿼리 객체 | GET `/hello/request/form/param/model` | 쿼리 문자열 | `@ModelAttribute` |
| JSON 객체 | POST `/hello/request/form/json` | JSON 본문 | `@RequestBody` |

정상 응답은 바인딩 방식과 서버가 읽은 값을 반환합니다.

```json
{
  "binding": "@RequestBody · JSON",
  "name": "민상",
  "age": 33
}
```

### 쿼리 요청

```bash
curl -G 'http://localhost:8080/hello/request/form/param' \
  --data-urlencode 'name=민상' \
  --data-urlencode 'age=33'
```

### 폼 요청

```bash
curl 'http://localhost:8080/hello/request/form/model' \
  --data-urlencode 'name=민상' \
  --data-urlencode 'age=33'
```

`Content-Type`은 `application/x-www-form-urlencoded`입니다. 브라우저 폼과 같은 형식이며, 숫자 입력도 문자열로 전달한 뒤 Spring이 정수로 변환합니다.

### JSON 요청

```bash
curl 'http://localhost:8080/hello/request/form/json' \
  -H 'Content-Type: application/json' \
  -d '{"name":"민상","age":33}'
```

JSON에서는 `age`를 숫자로 보냅니다. 이 프로젝트는 숫자 문자열의 자동 변환과 소수의 정수 변환을 허용하지 않도록 설정했습니다. `"33"`과 `33.5`는 400 응답입니다.

위 curl 예시는 Bash 기준입니다. PowerShell에서는 Swagger UI를 이용하거나 `curl.exe`에 맞게 인용 방식을 조정합니다.

## 2. 검증·오류 응답

| 입력·요청 | 기대 결과 |
|---|---|
| 이름 공백 또는 41자 이상 | 400 |
| 나이 누락·음수·151 이상 | 400 |
| 나이 0·150 | 200 |
| 나이 `abc` 또는 JSON 숫자 문자열 | 400 |
| 잘못된 JSON 문법·정의되지 않은 JSON 속성 | 400 |
| JSON 경로에 `text/plain` 본문 전송 | 415 |
| JSON 경로에 DELETE 전송 | 405 |
| 존재하지 않는 경로 | 404 |

이름에는 앞뒤 공백을 자동으로 제거하지 않습니다. 공백만으로 이루어진 이름은 거부하며, 최대 40자를 허용합니다. 경로 값이 비어 있거나 `/` 등 경로 구분에 영향을 주는 문자를 포함하면 라우팅 단계에서 요청이 거부될 수 있습니다. 일반 문자열 비교에는 쿼리·폼·JSON을 사용합니다.

```json
{
  "code": "INVALID_INPUT",
  "message": "입력값을 확인해 주세요.",
  "errors": [
    { "field": "age", "message": "나이는 150 이하여야 합니다." }
  ]
}
```

검증 오류는 `INVALID_INPUT`, 본문 문법·타입·필수 파라미터 오류는 `INVALID_REQUEST`를 사용합니다. `errors`의 순서는 보장하지 않으며 클라이언트는 `field`로 입력칸을 찾습니다. 형식 오류에는 필드 정보가 없을 수 있습니다.

웹 실습은 서버 검증을 관찰할 수 있도록 오류값도 전송합니다. 오류 필드는 문구와 `aria-invalid`로 표시하고, 입력값은 유지합니다.

## 3. 응답 형식 비교

| 경로 | 반환 타입 | Content-Type |
|---|---|---|
| `/response/json/string` | String | `text/plain;charset=UTF-8` |
| `/response/json/class` | Star 객체 | `application/json` |
| `/response/rest/json/string` | String | `text/plain;charset=UTF-8` |
| `/response/rest/json/class` | Star 객체 | `application/json` |

문자열과 객체 예제는 같은 이름·나이를 담지만 반환 타입과 미디어 타입이 다릅니다. 문자열의 Content-Type은 이 프로젝트에서 명시한 값이며, 모든 Spring 문자열 응답의 기본값을 의미하지 않습니다.

`@Controller` 예제에는 메서드에 `@ResponseBody`를 붙입니다. `@RestController` 예제에는 같은 동작이 컨트롤러 전체에 적용됩니다.

## 4. HTTP 메서드 비교

| 요청 | 응답 |
|---|---|
| GET `/api/hello` | 인사 문자열 |
| GET `/api/get` | GET 메서드 확인 JSON |
| POST `/api/post` | POST 메서드 확인 JSON |
| PUT `/api/put` | PUT 메서드 확인 JSON |
| DELETE `/api/delete` | DELETE 메서드 확인 JSON |

모든 정상 예제는 200을 반환합니다. 실제 리소스를 생성·수정·삭제하지 않으므로 201·204나 DB 동작을 흉내 내지 않습니다.

## 5. HTML과 리다이렉트

| 경로 | 동작 |
|---|---|
| `/hello.html` | static의 HTML을 직접 제공 |
| `/html/templates` | 뷰 이름으로 Thymeleaf 템플릿 렌더링 |
| `/html/dynamic` | Model의 요청 횟수를 템플릿에 전달 |
| `/html/redirect` | 302 응답 후 `/hello.html`로 이동 |
| `/static-hello` | 원본 실습 URL을 정적 리소스 리다이렉트로 유지 |
| `/hello/request/form/html` | 기존 폼 진입 URL을 웹 실습으로 연결 |

방문 횟수는 해당 애플리케이션 인스턴스가 처리한 `/html/dynamic` 요청의 누적값입니다. 사용자 수가 아니며 새로고침도 증가시킵니다. 재시작하면 초기화되고 여러 서버 간에는 공유되지 않습니다.

## 6. Swagger UI 사용

1. `/swagger-ui.html`에서 실습 그룹을 펼칩니다.
2. 실행할 API의 **Try it out**을 누릅니다.
3. 파라미터·폼 값·JSON 본문을 입력합니다.
4. **Execute**를 누르고 요청 URL, curl, 상태 코드, 응답 헤더·본문을 확인합니다.

API는 HTTP 메서드, 요청 바인딩, Controller 응답, RestController 응답 그룹으로 나눴습니다. HTML 렌더링은 웹 실습의 링크를 이용합니다.

## 7. 테스트와 실행 문제 확인

```bash
./gradlew clean build
```

HTML·API는 실제 Spring 컨텍스트와 MockMvc로 검증합니다. Mockito를 사용하는 테스트가 없어 해당 의존성은 제외했습니다. 방문 횟수는 8개 스레드에서 200번 요청한 결과가 중복 없이 증가하는지 검사합니다.

브라우저 검증은 README의 Node.js 명령을 사용합니다. `LAB_URL`로 서버 주소, `LAB_SCREENSHOTS`로 캡처 폴더, `CHROMIUM_PATH`로 브라우저 실행 파일을 지정할 수 있습니다. 기본값은 각각 `http://127.0.0.1:8080`, `build/browser-screenshots`, Playwright 기본 Chromium입니다.

| 상황 | 확인할 항목 |
|---|---|
| `JAVA_HOME`·컴파일러 오류 | Java 17 JDK 설치 여부. JRE만으로는 빌드할 수 없음 |
| 최초 의존성 다운로드 실패 | Maven Central·Gradle 배포 서버 연결 |
| 8080 포트 사용 중 | 기존 서버를 종료하거나 `./gradlew bootRun --args='--server.port=8081'` 실행 |
| 브라우저 연결 실패 | 서버 실행 여부와 브라우저 접속 주소 |
| 오류 응답이 예상과 다름 | HTTP 메서드, Content-Type, 이름·나이 타입 |

## 참고 문서

- [Spring MVC 컨트롤러 메서드](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods.html)
- [Spring Boot 3.5 문서](https://docs.spring.io/spring-boot/3.5/reference/)
- [springdoc-openapi 2.x](https://springdoc.org/v2/)
