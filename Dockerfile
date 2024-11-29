FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /application
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-focal AS builder
WORKDIR /application
ARG JAR_FILE=target/spring-0.0.9.jar
COPY --from=build /application/target/${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# the third stage of our build will copy the extracted layers
FROM eclipse-temurin:17-jre-focal
WORKDIR /application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
