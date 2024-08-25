package com.unconv.spring.web.advice;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;

class UnconvExceptionHandlerTest {

    @InjectMocks private UnconvExceptionHandler handler;

    @Mock private NativeWebRequest request;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleInsufficientAuthenticationExceptionAndReturnsProblemResponse() {
        InsufficientAuthenticationException exception =
                new InsufficientAuthenticationException("Authentication is required");
        String path = "/AnyValidPath";
        when(request.getDescription(false)).thenReturn("uri=" + path);

        ResponseEntity<Problem> responseEntity =
                handler.handleInsufficientAuthenticationException(exception, request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Problem problem = responseEntity.getBody();
        assertThat(problem).isNotNull();
        assert problem != null;
        assertThat(problem.getTitle()).isEqualTo("Insufficient Authentication");
        assertThat(Objects.requireNonNull(problem.getStatus()).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problem.getDetail())
                .isEqualTo("Authentication required to access this endpoint");
        assertThat(problem.getParameters().get("path")).isEqualTo(path);
        assertThat(problem.getParameters().get("timestamp")).isInstanceOf(OffsetDateTime.class);
    }
}
