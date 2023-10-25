# Unconvetional Spring Project

## Build Status
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/GeoZac/unconv_spring/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/GeoZac/unconv_spring/tree/master)

## Coverage
[![codecov](https://codecov.io/gh/GeoZac/unconv_spring/graph/badge.svg?token=93FS5ZZLMW)](https://codecov.io/gh/GeoZac/unconv_spring)

### Run tests
`$ ./mvnw clean verify`

### Run locally
```
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
