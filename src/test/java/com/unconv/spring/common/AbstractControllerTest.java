package com.unconv.spring.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

public abstract class AbstractControllerTest {

    @Autowired protected MockMvc mockMvc;

    @Autowired(required = false)
    protected MockMvcRestDocumentationConfigurer mockMvcRestDocumentationConfigurer;

    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected WebApplicationContext webApplicationContext;
}
