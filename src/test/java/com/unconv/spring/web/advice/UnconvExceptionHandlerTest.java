package com.unconv.spring.web.advice;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;

class UnconvExceptionHandlerTest {

    @InjectMocks private UnconvExceptionHandler handler;

    @Mock private NativeWebRequest request;

    @BeforeEach
    void setup() {
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
        assertThat(path).isEqualTo(problem.getParameters().get("path"));
        assertThat(problem.getParameters().get("timestamp")).isInstanceOf(OffsetDateTime.class);
    }

    @Test
    void handlePropertyReferenceExceptionAndReturnsProblemResponse() {
        String invalidProperty = "invalidPropertyName";
        TypeInformation<?> typeInformation = ClassTypeInformation.from(String.class);
        List<PropertyPath> propertyPaths = Collections.emptyList();
        PropertyReferenceException exception =
                new PropertyReferenceException(invalidProperty, typeInformation, propertyPaths);
        String path = "/api/some-resource";
        when(request.getDescription(false)).thenReturn("uri=" + path);

        ResponseEntity<Problem> responseEntity =
                handler.handlePropertyReferenceException(exception, request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Problem problem = responseEntity.getBody();
        assertThat(problem).isNotNull();
        assert problem != null;
        assertThat(problem.getTitle()).isEqualTo("Bad Request");
        assertThat(Objects.requireNonNull(problem.getStatus()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("Invalid property reference: " + invalidProperty);
        assertThat(path).isEqualTo(problem.getParameters().get("path"));
        assertThat(problem.getParameters().get("timestamp")).isInstanceOf(OffsetDateTime.class);
    }
}
