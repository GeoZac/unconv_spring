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

public abstract class AbstractControllerTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired(required = false)
    protected MockMvcRestDocumentationConfigurer mockMvcRestDocumentationConfigurer;

    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected WebApplicationContext webApplicationContext;

    protected static final int DEFAULT_PAGE_SIZE_INT =
            Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);

    protected static int totalPages;

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

    protected void initializeMockMvc() {
        DefaultMockMvcBuilder builder =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/")
                                        .with(user("username").roles(UNCONV_USER.name())));

        if (mockMvcRestDocumentationConfigurer != null) {
            builder = builder.apply(mockMvcRestDocumentationConfigurer);
        }

        mockMvc = builder.apply(springSecurity()).build();
    }
}
