package com.unconv.spring.web.controllers;

import static com.unconv.spring.consts.AppConstants.DEFAULT_SORT_BY;
import static com.unconv.spring.consts.AppConstants.DEFAULT_SORT_DIRECTION;
import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;
import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.TemperatureThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.security.MethodSecurityConfig;
import com.unconv.spring.service.TemperatureThresholdService;
import com.unconv.spring.web.rest.TemperatureThresholdController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@WebMvcTest(controllers = TemperatureThresholdController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/TemperatureThreshold")
@Import(MethodSecurityConfig.class)
class TemperatureThresholdControllerTest extends AbstractControllerTest {
    @MockBean private TemperatureThresholdService temperatureThresholdService;

    private List<TemperatureThreshold> temperatureThresholdList;

    private static final int DEFAULT_PAGE_SIZE = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/TemperatureThreshold")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
                        .apply(springSecurity())
                        .build();

        this.temperatureThresholdList = new ArrayList<>();
        temperatureThresholdList =
                Instancio.ofList(TemperatureThreshold.class)
                        .size(15)
                        .ignore(field(TemperatureThreshold::getId))
                        .create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());

        totalPages = (int) Math.ceil((double) temperatureThresholdList.size() / DEFAULT_PAGE_SIZE);
    }

    @Test
    void shouldFetchAllTemperatureThresholds() throws Exception {
        int pageNo = 0;
        Sort sort = Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_BY);
        PageRequest pageRequest = PageRequest.of(pageNo, DEFAULT_PAGE_SIZE, sort);

        int dataSize = temperatureThresholdList.size();

        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + DEFAULT_PAGE_SIZE, dataSize);
        List<TemperatureThreshold> pagedReadings = temperatureThresholdList.subList(start, end);

        Page<TemperatureThreshold> page = new PageImpl<>(pagedReadings, pageRequest, dataSize);

        PagedResult<TemperatureThreshold> temperatureThresholdPagedResult = new PagedResult<>(page);
        given(
                        temperatureThresholdService.findAllTemperatureThresholds(
                                pageNo, DEFAULT_PAGE_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION))
                .willReturn(temperatureThresholdPagedResult);

        this.mockMvc
                .perform(get("/TemperatureThreshold"))
                .andDo(
                        document(
                                "shouldFetchAllTemperatureThresholds",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(DEFAULT_PAGE_SIZE)))
                .andExpect(jsonPath("$.totalElements", is(dataSize)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(dataSize < DEFAULT_PAGE_SIZE)))
                .andExpect(jsonPath("$.hasNext", is(dataSize > DEFAULT_PAGE_SIZE)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindTemperatureThresholdById() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(temperatureThresholdId, 100, -100);
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.of(temperatureThreshold));

        this.mockMvc
                .perform(get("/TemperatureThreshold/{id}", temperatureThresholdId).with(csrf()))
                .andDo(
                        document(
                                "shouldFindTemperatureThresholdById",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(temperatureThresholdId.toString())))
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(temperatureThreshold.getMinValue())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/TemperatureThreshold/{id}", temperatureThresholdId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingNonExistingTemperatureThreshold",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewTemperatureThreshold() throws Exception {
        given(temperatureThresholdService.saveTemperatureThreshold(any(TemperatureThreshold.class)))
                .willAnswer(
                        invocation -> {
                            TemperatureThreshold temperatureThreshold = invocation.getArgument(0);
                            temperatureThreshold.setId(UUID.randomUUID());
                            return temperatureThreshold;
                        });

        TemperatureThreshold temperatureThreshold = new TemperatureThreshold(null, 100, 0);
        this.mockMvc
                .perform(
                        post("/TemperatureThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andDo(
                        document(
                                "shouldCreateNewTemperatureThreshold",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(temperatureThreshold.getMinValue())))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewTemperatureThresholdWithNullValues() throws Exception {
        TemperatureThreshold temperatureThreshold = new TemperatureThreshold();

        this.mockMvc
                .perform(
                        post("/TemperatureThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewTemperatureThresholdWithNullValues",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("temperatureThresholdDTO")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Min. value must be less than Max. value")))
                .andReturn();
    }

    @Test
    void shouldUpdateTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(temperatureThresholdId, 0, 100);
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.of(temperatureThreshold));
        given(temperatureThresholdService.saveTemperatureThreshold(any(TemperatureThreshold.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/TemperatureThreshold/{id}", temperatureThreshold.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andDo(
                        document(
                                "shouldUpdateTemperatureThreshold",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(temperatureThreshold.getMinValue())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.empty());
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(temperatureThresholdId, 0, 100);

        this.mockMvc
                .perform(
                        put("/TemperatureThreshold/{id}", temperatureThresholdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(temperatureThreshold)))
                .andDo(
                        document(
                                "shouldReturn404WhenUpdatingNonExistingTemperatureThreshold",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        TemperatureThreshold temperatureThreshold =
                new TemperatureThreshold(temperatureThresholdId, 0, 100);
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.of(temperatureThreshold));
        doNothing()
                .when(temperatureThresholdService)
                .deleteTemperatureThresholdById(temperatureThreshold.getId());

        this.mockMvc
                .perform(
                        delete("/TemperatureThreshold/{id}", temperatureThreshold.getId())
                                .with(csrf()))
                .andDo(
                        document(
                                "shouldDeleteTemperatureThreshold",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(temperatureThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(temperatureThreshold.getMinValue())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingTemperatureThreshold() throws Exception {
        UUID temperatureThresholdId = UUID.randomUUID();
        given(temperatureThresholdService.findTemperatureThresholdById(temperatureThresholdId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/TemperatureThreshold/{id}", temperatureThresholdId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenDeletingNonExistingTemperatureThreshold",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }
}
