FROM eclipse-temurin:17-jdk-focal AS build
WORKDIR /application
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
COPY sonar-project.properties ./
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-focal AS builder
WORKDIR /application
ARG JAR_FILE=target/spring-0.0.9.jar
COPY --from=build /application/${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# the third stage of our build will copy the extracted layers
FROM eclipse-temurin:17-jre-alpine
ARG PORT=8080
WORKDIR /application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
EXPOSE ${PORT}
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
