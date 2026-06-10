package com.unconv.spring.common;

import static com.unconv.spring.consts.AppConstants.DEFAULT_PAGE_SIZE;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base abstract class for Spring Boot integration tests.
 *
 * <p>This class provides common configuration and infrastructure for full-context integration
 * tests, including:
 *
 * <ul>
 *   <li>Bootstrapping the application using {@link SpringBootTest}
 *   <li>Activating the {@code test} Spring profile
 *   <li>Initializing container-based database support
 *   <li>Configuring {@link MockMvc} with Spring Security
 * </ul>
 *
 * <p>Concrete integration test classes should extend this class and invoke {@link
 * #initializeMockMvc()} in a {@code @BeforeEach} method to ensure consistent security and request
 * setup.
 */
@ActiveProfiles({PROFILE_TEST})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = {DBContainerInitializer.class})
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    /**
     * {@link MockMvc} instance used to perform HTTP requests against the application context during
     * integration tests.
     *
     * <p>This instance is re-initialized with security configuration by calling {@link
     * #initializeMockMvc()}.
     */
    @Autowired protected MockMvc mockMvc;

    /**
     * Jackson {@link ObjectMapper} used for JSON serialization and deserialization in integration
     * tests.
     */
    @Autowired protected ObjectMapper objectMapper;

    /**
     * {@link WebApplicationContext} representing the fully initialized Spring application context
     * for integration tests.
     */
    @Autowired protected WebApplicationContext webApplicationContext;

    /** Default page size as an integer value used in pagination-related integration tests. */
    protected static final int DEFAULT_PAGE_SIZE_INT = Integer.parseInt(DEFAULT_PAGE_SIZE);

    /**
     * Initializes the {@link MockMvc} instance with default request and Spring Security
     * configuration.
     *
     * <p>The default request is executed as an authenticated user with the {@code UNCONV_USER} role
     * to simplify secured endpoint testing.
     *
     * <p>This method <strong>must</strong> be invoked in a {@code @BeforeEach} method of subclasses
     * to ensure consistent security context setup.
     */
    protected void initializeMockMvc() {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/")
                                        .with(user("UnconvUser").roles(UNCONV_USER.name())))
                        .apply(springSecurity())
                        .build();
    }
}
