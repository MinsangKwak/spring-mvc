# Render 배포

저장소는 비공개로 유지하면서 실행 화면과 Swagger UI를 공개합니다. GitHub에서 코드·테스트·배포 설정을 관리하고 Render는 Spring Boot 실행을 담당합니다.

## 처음 배포하기

Render에서 GitHub 접근 권한을 연결하고 New → Web Service를 선택합니다.

| 설정 | 값 |
|---|---|
| Repository | MinsangKwak/spring-mvc |
| Name | spring-mvc 또는 사용 가능한 이름 |
| Language | Docker |
| Branch | main |
| Region | Singapore |
| Root Directory | 비워두기 |
| Dockerfile Path | ./Dockerfile |
| Compute | Free · $0/month |
| Health Check Path | /api/hello |
| Auto-Deploy | After CI Checks Pass |

Docker Command와 별도 빌드·시작 명령은 지정하지 않습니다. Dockerfile의 ENTRYPOINT로 실행합니다. DB·API 키·별도 환경변수 입력은 필요하지 않습니다.

수동으로 Web Service를 생성한 경우 render.yaml이 대시보드 설정에 자동 적용되지는 않습니다. 표의 값을 직접 설정합니다. Blueprint로 생성할 때는 저장소 루트의 render.yaml을 사용합니다. 두 방식을 동시에 실행해 서비스를 중복 생성하지 않습니다.

## 컨테이너 구성

- 빌드 단계: Java 17 JDK에서 Gradle Wrapper로 bootJar 생성
- 실행 단계: Java 17 JRE에서 비관리자 UID로 JAR 실행
- 메모리: 무료 인스턴스의 512 MB를 고려해 Java 최대 힙을 256 MB로 제한
- 포트: Render의 PORT 환경변수를 사용하고, 없으면 10000으로 실행
- 프록시: 전달된 HTTPS 정보를 반영해 Swagger 요청 URL을 생성

힙 제한은 전체 프로세스 메모리 제한이 아닙니다. 실제 메모리 사용량과 시작 시간은 Render 로그에서 확인합니다. 데이터는 DB에 저장하지 않으며 방문 횟수는 재시작하면 초기화됩니다.

## 배포 후 확인

Render가 발급한 주소에서 다음 경로를 확인합니다.

1. `/`: 웹 실습 화면
2. `/api/hello`: 정상 인사 응답
3. `/swagger-ui.html`: Swagger UI에서 JSON·폼 요청 실행
4. `/v3/api-docs`: servers URL이 실제 HTTPS 주소인지 확인

무료 인스턴스는 비활동 시 정지해 첫 접속에 대기 시간이 생길 수 있습니다. 실제 지원 자료에는 배포 완료 후 확인한 주소만 기재합니다.

## 로컬에서 같은 설정 확인

```bash
docker build -t spring-mvc-lab .
docker run --rm -p 10000:10000 -e PORT=10000 spring-mvc-lab
```

서버 실행 후 `http://localhost:10000/`과 `http://localhost:10000/swagger-ui.html`을 확인합니다.

## 참고

- https://render.com/docs/docker
- https://render.com/docs/web-services
- https://render.com/docs/blueprint-spec
