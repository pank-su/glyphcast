# Stage 1: Build
FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew :tgbot:shadowJar --no-daemon

# Stage 2: Run
FROM openjdk:17-slim
RUN mkdir /app
COPY --from=build /home/gradle/src/tgbot/build/libs/*.jar /app/tgbot.jar
WORKDIR /app

ENTRYPOINT ["java", "-jar", "tgbot.jar"]
