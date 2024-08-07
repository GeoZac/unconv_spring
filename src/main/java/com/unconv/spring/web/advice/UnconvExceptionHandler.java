package com.unconv.spring.web.advice;

import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/** Global controller advice class responsible for handling unconverted exceptions. */
@Slf4j
@ControllerAdvice
public class UnconvExceptionHandler implements ProblemHandling {
    @ExceptionHandler
    public ResponseEntity<Problem> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, NativeWebRequest request) {

        String path = request.getDescription(false).substring(4);
        log.error("{} occurred at path: {}", ex.getMessage(), path);

        Problem problem =
                Problem.builder()
                        .with("timestamp", OffsetDateTime.now())
                        .withTitle("Insufficient Authentication")
                        .withStatus(Status.UNAUTHORIZED)
                        .withDetail("Authentication required to access this endpoint")
                        .with("path", path)
                        .build();
        return create(ex, problem, request);
    }
}
