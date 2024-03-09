FROM openjdk:17-jdk-slim

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
ENTRYPOINT ["java", "-jar", "./target/spring-0.0.7-HOTFIX.jar", "--spring.profiles.active=render"]

