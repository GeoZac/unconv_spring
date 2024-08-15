package com.unconv.spring.web.advice;

import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/** Global controller advice class responsible for handling unconverted exceptions. */
@Slf4j
@ControllerAdvice
@Order(Integer.MIN_VALUE)
public class UnconvExceptionHandler implements ProblemHandling {

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Problem> handlePropertyReferenceException(
            PropertyReferenceException ex, NativeWebRequest request) {
        String path = request.getDescription(false).substring(4);
        log.error("{} occurred at path: {}", ex.getMessage(), path);

        Problem problem =
                Problem.builder()
                        .with("timestamp", OffsetDateTime.now())
                        .withTitle("Bad Request")
                        .withStatus(Status.BAD_REQUEST)
                        .withDetail("Invalid property reference: " + ex.getPropertyName())
                        .with("path", path)
                        .build();
        return create(ex, problem, request);
    }
}
