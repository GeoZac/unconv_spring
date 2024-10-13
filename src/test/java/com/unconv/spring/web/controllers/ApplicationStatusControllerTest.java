package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractControllerTest;
import java.nio.charset.Charset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = com.unconv.spring.web.rest.ApplicationStatusController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/ApplicationStatus")
class ApplicationStatusControllerTest extends AbstractControllerTest {

    @MockBean private BuildProperties buildProperties;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(mockMvcRestDocumentationConfigurer)
                        .build();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());
    }

    @Test
    void shouldFetchAppVersion() throws Exception {

        given(buildProperties.getVersion()).willReturn("0.0.9");

        this.mockMvc
                .perform(get("/public/status/version").characterEncoding(Charset.defaultCharset()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", not("application/json")))
                .andDo(document("shouldFetchAppVersion", preprocessResponse(prettyPrint)))
                .andExpect(
                        jsonPath(
                                "$",
                                is(
                                        "0.0.9\nThis endpoint is deprecated and will be removed in future version. Please use /v1/version.")))
                .andReturn();
    }

    @Test
    void shouldFetchAppVersionWithJSONResponse() throws Exception {

        given(buildProperties.getVersion()).willReturn("0.0.9");

        this.mockMvc
                .perform(
                        get("/public/status/v1/version")
                                .characterEncoding(Charset.defaultCharset()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", is("application/json")))
                .andDo(
                        document(
                                "shouldFetchAppVersionWithJSONResponse",
                                preprocessResponse(prettyPrint)))
                .andExpect(jsonPath("$.version", is("0.0.9")))
                .andReturn();
    }
}
