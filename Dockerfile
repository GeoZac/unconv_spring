#FROM maven:3.8.3-jdk-17-slim AS builder
#WORKDIR /app
#COPY pom.xml .
#RUN mvn dependency:go-offline
#COPY src/ ./src/
#RUN mvn clean package -DskipTests
#ARG JAR_FILE=target/spring-0.0.1-SNAPSHOT.jar
#COPY ${JAR_FILE} /app/application.jar
FROM openjdk:16-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the application code into the container
COPY . /app/

# Copy the application-render.properties file from the secrets file location
RUN --mount=type=secret,id=application-render.properties dst=/app/src/main/resources/application-render.properties

# Build the project with Maven Wrapper
RUN chmod +x mvnw && \
    ./mvnw clean package -Prender -DskipTests

# Expose the port used by Spring Boot
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "./target/spring-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=render"]


#FROM adoptopenjdk/openjdk17:alpine-jre
#WORKDIR /app
#COPY --from=builder /app/dependencies/ ./
#COPY --from=builder /app/spring-boot-loader/ ./
#COPY --from=builder /app/snapshot-dependencies/ ./
#COPY --from=builder /app/application/ ./
#ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
