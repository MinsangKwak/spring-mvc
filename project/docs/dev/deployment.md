# Render 배포

Spring Boot 애플리케이션을 Render에서 실행해 웹 실습과 Java Swagger를 제공합니다. 코드·테스트·배포 설정은 GitHub에서 관리합니다.

현재 배포 주소: [웹 실습](https://spring-mvc-k14y.onrender.com/) · [Java Swagger](https://spring-mvc-k14y.onrender.com/java-swagger)

## 연결 서비스의 현재 설정

2026-11-23에 Render에서 확인한 연결 서비스는 `MyWorkspace`의 `spring-mvc`입니다. 저장소 파일을 옮겨도 아래 Dashboard 설정은 자동으로 바뀌지 않습니다.

| 설정 | 값 |
|---|---|
| Repository | MinsangKwak/spring-mvc |
| Name | spring-mvc |
| Root Directory | 비워둠 |
| Docker Context | . |
| Dockerfile Path | ./Dockerfile |
| Auto-Deploy | 활성화 · Commit마다 배포 |
| Health Check Path | 비워둠 · 플랫폼 기본 TCP 확인 |

## 이 구조를 병합하기 전 변경

연결 서비스의 Dockerfile Path를 `./backend-java/Dockerfile`로 저장한 뒤 이 구조를 `main`에 병합해야 합니다. Root Directory는 비워두고 Docker Context는 `.`을 유지합니다. 현재 저장소 변경만으로 이 Dashboard 값은 바뀌지 않습니다.

Docker Command와 별도 빌드·시작 명령은 지정하지 않습니다. Dockerfile의 ENTRYPOINT로 실행합니다. DB·API 키·별도 환경변수 입력은 필요하지 않습니다.

`project/tools/render.yaml`은 같은 구조로 새 Blueprint를 만들 때 참고하는 목표 설정입니다. 현재 연결 서비스에 자동 적용되지 않으며, 기존 서비스와 함께 Blueprint를 생성하면 서비스가 중복될 수 있습니다.

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
3. `/java-swagger`: 주소를 유지한 Swagger UI에서 JSON·폼 요청 실행
4. `/v3/api-docs`: servers URL이 상대 경로 `/`로 고정되어 있는지 확인

무료 인스턴스는 비활동 시 정지해 첫 접속에 대기 시간이 생길 수 있습니다.

## 로컬에서 같은 설정 확인

```bash
docker build -f backend-java/Dockerfile -t spring-mvc-lab .
docker run --rm -p 10000:10000 -e PORT=10000 spring-mvc-lab
```

서버 실행 후 `http://localhost:10000/`과 `http://localhost:10000/java-swagger`를 확인합니다. 기존 `/swagger-ui.html`은 새 경로로 리다이렉트합니다.

## 참고

- https://render.com/docs/docker
- https://render.com/docs/web-services
- https://render.com/docs/blueprint-spec

[프로젝트 소개](../../../README.md) · [문서 안내](../README.md) · [보안 경계](security.md)
