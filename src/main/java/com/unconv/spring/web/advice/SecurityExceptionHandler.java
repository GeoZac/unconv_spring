package com.unconv.spring.web.advice;

import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

@ControllerAdvice
public class SecurityExceptionHandler implements SecurityAdviceTrait {
    @Override
    public ResponseEntity<Problem> handleAccessDenied(
            AccessDeniedException e, NativeWebRequest request) {

        Problem problem =
                Problem.builder()
                        .with("timestamp", OffsetDateTime.now())
                        .withTitle("Forbidden")
                        .withStatus(Status.FORBIDDEN)
                        .withDetail("You are not authorized to access this endpoint")
                        .build();
        return create(e, problem, request);
    }
}
