FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY backend-java/gradlew backend-java/settings.gradle backend-java/build.gradle ./backend-java/
COPY backend-java/gradle ./backend-java/gradle
COPY backend-java/src ./backend-java/src
COPY frontend-thymeleaf/src/main/resources ./frontend-thymeleaf/src/main/resources
WORKDIR /workspace/backend-java
RUN bash ./gradlew --no-daemon bootJar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/backend-java/build/libs/spring-mvc-1.0.0-SNAPSHOT.jar ./app.jar
ENV SPRING_PROFILES_ACTIVE=render
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -XX:+UseSerialGC"
USER 10001:10001
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
