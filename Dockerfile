# Stage 1
FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /app

COPY . /app

RUN ./mvnw package -DskipTests

# Stage 2
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

COPY --from=builder /app/target/spring-0.0.7-SNAPSHOT.jar application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]
