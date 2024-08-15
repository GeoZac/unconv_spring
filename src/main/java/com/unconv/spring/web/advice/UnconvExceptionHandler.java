package com.unconv.spring.web.advice;

import java.time.OffsetDateTime;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/** Global controller advice class responsible for handling unconverted exceptions. */
@ControllerAdvice
public class UnconvExceptionHandler implements ProblemHandling {

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Problem> handlePropertyReferenceException(
            PropertyReferenceException ex, NativeWebRequest request) {

        Problem problem =
                Problem.builder()
                        .with("timestamp", OffsetDateTime.now())
                        .withTitle("Bad Request")
                        .withStatus(Status.BAD_REQUEST)
                        .build();
        return create(ex, problem, request);
    }
}
