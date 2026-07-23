package com.unconv.spring.common;

import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.consts.AppConstants;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.operation.preprocess.ContentModifyingOperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

/**
 * Base class for controller integration tests.
 *
 * <p>Provides a common {@link MockMvc} setup, shared test infrastructure, and Jackson configuration
 * for all controller tests. It also configures Spring Security, REST Docs (when available), and
 * support for RFC 7807 Problem responses.
 */
public abstract class AbstractControllerTest {

    /** MockMvc instance used to perform HTTP requests against the application context. */
    @Autowired protected MockMvc mockMvc;

    /**
     * Optional Spring REST Docs configuration.
     *
     * <p>If present, the configuration is applied when building the {@link MockMvc} instance.
     */
    @Autowired(required = false)
    protected MockMvcRestDocumentationConfigurer mockMvcRestDocumentationConfigurer;

    /** ObjectMapper used for JSON serialization, deserialization, and formatting. */
    @Autowired protected ObjectMapper objectMapper;

    /** Spring web application context used to initialize {@link MockMvc}. */
    @Autowired protected WebApplicationContext webApplicationContext;

    /** Default page size used by pagination-related controller tests. */
    protected static final int DEFAULT_PAGE_SIZE_INT =
            Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);

    /** Total number of pages returned by the most recent paginated test response. */
    protected static int totalPages;

    /**
     * Preprocessor that formats JSON request and response content with pretty printing.
     *
     * <p>If the content cannot be parsed as JSON, the original content is returned unchanged.
     */
    protected OperationPreprocessor prettyPrint =
            new ContentModifyingOperationPreprocessor(
                    (content, contentType) -> {
                        PrettyPrinter prettyPrinter =
                                new DefaultPrettyPrinter()
                                        .withArrayIndenter(
                                                DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
                        try {
                            return objectMapper
                                    .writer(prettyPrinter)
                                    .writeValueAsBytes(objectMapper.readTree(content));
                        } catch (IOException ex) {
                            return content;
                        }
                    });

    /**
     * Configures the {@link MockMvc} instance using the current {@link WebApplicationContext}.
     *
     * <p>The configuration:
     *
     * <ul>
     *   <li>Registers a default authenticated user for all requests.
     *   <li>Applies Spring Security support.
     *   <li>Applies Spring REST Docs configuration when available.
     *   <li>Registers Jackson modules for RFC 7807 Problem and constraint violation responses.
     * </ul>
     */
    protected void configureMockMvcWithObjectMapper() {
        DefaultMockMvcBuilder builder =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/")
                                        .with(user("username").roles(UNCONV_USER.name())));

        if (mockMvcRestDocumentationConfigurer != null) {
            builder = builder.apply(mockMvcRestDocumentationConfigurer);
        }

        mockMvc = builder.apply(springSecurity()).build();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }
}
