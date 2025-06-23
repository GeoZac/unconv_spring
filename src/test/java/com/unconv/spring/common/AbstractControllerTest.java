package com.unconv.spring.common;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.operation.preprocess.ContentModifyingOperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

public abstract class AbstractControllerTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired(required = false)
    protected MockMvcRestDocumentationConfigurer mockMvcRestDocumentationConfigurer;

    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected WebApplicationContext webApplicationContext;

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
}
