FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew settings.gradle ./
COPY gradle ./gradle
COPY backend-java ./backend-java
COPY frontend-thymeleaf/src/main/resources ./frontend-thymeleaf/src/main/resources
RUN bash ./gradlew --no-daemon bootJar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/build/libs/spring-mvc-1.0.0-SNAPSHOT.jar ./app.jar
ENV SPRING_PROFILES_ACTIVE=render
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -XX:+UseSerialGC"
USER 10001:10001
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
