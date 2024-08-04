FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the application code into the container
COPY . /app/

# Copy the application-render.properties file from the secrets file location
RUN --mount=type=secret,id=application-render.properties dst=/app/src/main/resources/application-render.properties
RUN --mount=type=secret,id=logback-spring-render.xml dst=/app/src/main/resources/logback-spring-render.xml

# Build the project with Maven Wrapper
RUN chmod +x mvnw && \
    ./mvnw clean compile

# Expose the port used by Spring Boot
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ./mvnw spring-boot:run -Dspring-boot.run.profiles=render

