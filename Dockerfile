FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /application
COPY target/ .
RUN ls
# Create a temporary directory
RUN mkdir /tmp/cache_bust

# Copy a dummy file to the temporary directory (change the dummy_file.txt to a file in your project)
COPY dummy_file.txt /tmp/cache_bust/

# Run chmod and mvnw
RUN chmod +x mvnw && \
    ./mvnw clean package -Prender -DskipTests

# Remove the temporary directory
RUN rm -rf /tmp/cache_bust
ARG JAR_FILE=target/spring-0.0.7-HOTFIX.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# the second stage of our build will copy the extracted layers
FROM eclipse-temurin:17-jre-focal
WORKDIR /application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
