package com.unconv.spring.web.advice;

import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/** Global controller advice class responsible for handling unconverted exceptions. */
@Slf4j
@ControllerAdvice
@Order(Integer.MIN_VALUE + 1)
public class UnconvExceptionHandler implements ProblemHandling {

    private static final String TIMESTAMP = "timestamp";

    /**
     * Handles exceptions of type {@link InsufficientAuthenticationException} that occur when
     * authentication is required to access a specific endpoint but is not provided.
     *
     * <p>This method logs the error message and the path where the exception occurred, and creates
     * a {@link Problem} object containing details about the error, including the timestamp, title,
     * status, detailed message, and the request path.
     *
     * @param ex the {@link InsufficientAuthenticationException} that was thrown
     * @param request the {@link NativeWebRequest} containing the details of the web request
     * @return a {@link ResponseEntity} containing a {@link Problem} object with details about the
     *     insufficient authentication exception, along with an HTTP status of 401 (Unauthorized)
     */
    @ExceptionHandler
    public ResponseEntity<Problem> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, NativeWebRequest request) {

        String path = request.getDescription(false).substring(4);
        logError(ex, request);

        Problem problem =
                Problem.builder()
                        .with(TIMESTAMP, OffsetDateTime.now())
                        .withTitle("Insufficient Authentication")
                        .withStatus(Status.UNAUTHORIZED)
                        .withDetail("Authentication required to access this endpoint")
                        .with("path", path)
                        .build();
        return create(ex, problem, request);
    }

    /**
     * Handles {@link PropertyReferenceException} thrown when an invalid property is referenced in a
     * query or other operation.
     *
     * <p>This method captures the exception, logs it with the associated request path, and builds a
     * {@link Problem} object to return a detailed error response to the client. The response
     * includes a timestamp, title, status, detail message, and the path where the error occurred.
     *
     * @param ex the {@link PropertyReferenceException} that was thrown due to an invalid property
     *     reference
     * @param request the {@link NativeWebRequest} associated with the current request, used to
     *     extract the request path
     * @return a {@link ResponseEntity} containing a {@link Problem} object that describes the
     *     error, with an HTTP status of {@code 400 Bad Request}
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Problem> handlePropertyReferenceException(
            PropertyReferenceException ex, NativeWebRequest request) {
        String path = request.getDescription(false).substring(4);
        logError(ex, request);

        Problem problem =
                Problem.builder()
                        .with(TIMESTAMP, OffsetDateTime.now())
                        .withTitle("Bad Request")
                        .withStatus(Status.BAD_REQUEST)
                        .withDetail("Invalid property reference: " + ex.getPropertyName())
                        .with("path", path)
                        .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Problem> handleInvalidUUID(
            MethodArgumentTypeMismatchException ex, NativeWebRequest request) {
        Problem problem =
                Problem.builder()
                        .with("timestamp", OffsetDateTime.now())
                        .withTitle("Bad Request")
                        .withStatus(Status.BAD_REQUEST)
                        .withDetail("The provided string is not a valid UUID")
                        .build();
        return create(ex, problem, request);
    }

    /**
     * Handles {@link IllegalArgumentException} exceptions that occur in the application.
     *
     * <p>This method is responsible for logging the error and returning a structured problem
     * response in accordance with RFC 7807 format (Problem Details for HTTP APIs).
     *
     * @param ex the exception thrown when an invalid argument is passed, typically indicating an
     *     issue such as an invalid page index or other argument violations.
     * @param request the {@link NativeWebRequest} object, which allows access to the details of the
     *     web request that triggered the exception.
     * @return a {@link ResponseEntity} containing a {@link Problem} object with a status of 400
     *     (Bad Request), a descriptive title, the timestamp of the error, the detailed error
     *     message, and the path where the error occurred.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Problem> handleIllegalArgumentException(
            IllegalArgumentException ex, NativeWebRequest request) {
        String path = request.getDescription(false).substring(4);
        logError(ex, request);

        Problem problem =
                Problem.builder()
                        .with(TIMESTAMP, OffsetDateTime.now())
                        .withTitle("Bad Request")
                        .withStatus(Status.BAD_REQUEST)
                        .withDetail("Page index must not be less than zero")
                        .with("path", path)
                        .build();
        return create(ex, problem, request);
    }

    /**
     * Logs an error message for the given exception and request, and returns the request path.
     *
     * @param ex the exception to log.
     * @param request the {@link NativeWebRequest} from which the request path is extracted.
     */
    private void logError(Exception ex, NativeWebRequest request) {
        String path = request.getDescription(false).substring(4);
        log.error("{} occurred at path: {}", ex.getClass().getSimpleName(), path);
    }
}
