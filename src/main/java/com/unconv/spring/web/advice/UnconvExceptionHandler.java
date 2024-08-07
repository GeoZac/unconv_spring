package com.unconv.spring.web.advice;

import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/** Global controller advice class responsible for handling unconverted exceptions. */
@ControllerAdvice
public class UnconvExceptionHandler implements ProblemHandling {
    @ExceptionHandler
    public ResponseEntity<Problem> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, NativeWebRequest request) {

        Problem problem =
                Problem.builder()
                        .with("timestamp", OffsetDateTime.now())
                        .withTitle("Insufficient Authentication")
                        .withStatus(Status.UNAUTHORIZED)
                        .withDetail("Authentication required to access this endpoint")
                        .build();
        return create(ex, problem, request);
    }
}
