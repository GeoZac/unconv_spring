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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unconv.spring.common.AbstractControllerTest;
import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.HumidityThreshold;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.security.MethodSecurityConfig;
import com.unconv.spring.service.HumidityThresholdService;
import com.unconv.spring.web.rest.HumidityThresholdController;
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

@WebMvcTest(controllers = HumidityThresholdController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs(outputDir = "target/snippets/HumidityThreshold")
@Import(MethodSecurityConfig.class)
class HumidityThresholdControllerTest extends AbstractControllerTest {

    @MockBean private HumidityThresholdService humidityThresholdService;

    private List<HumidityThreshold> humidityThresholdList;

    private static final int DEFAULT_PAGE_SIZE = Integer.parseInt(AppConstants.DEFAULT_PAGE_SIZE);

    private static int totalPages;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .defaultRequest(
                                MockMvcRequestBuilders.get("/HumidityThreshold")
                                        .with(user("username").roles(UNCONV_USER.name())))
                        .apply(mockMvcRestDocumentationConfigurer)
                        .apply(springSecurity())
                        .build();

        this.humidityThresholdList = new ArrayList<>();
        humidityThresholdList =
                Instancio.ofList(HumidityThreshold.class)
                        .size(15)
                        .ignore(field(HumidityThreshold::getId))
                        .create();

        objectMapper.registerModule(new ProblemModule());
        objectMapper.registerModule(new ConstraintViolationProblemModule());

        totalPages = (int) Math.ceil((double) humidityThresholdList.size() / DEFAULT_PAGE_SIZE);
    }

    @Test
    void shouldFetchAllHumidityThresholds() throws Exception {
        int pageNo = 0;
        Sort sort = Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_BY);
        PageRequest pageRequest = PageRequest.of(pageNo, DEFAULT_PAGE_SIZE, sort);

        int dataSize = humidityThresholdList.size();

        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + DEFAULT_PAGE_SIZE, dataSize);
        List<HumidityThreshold> pagedReadings = humidityThresholdList.subList(start, end);

        Page<HumidityThreshold> page = new PageImpl<>(pagedReadings, pageRequest, dataSize);

        PagedResult<HumidityThreshold> humidityThresholdPagedResult = new PagedResult<>(page);
        given(
                        humidityThresholdService.findAllHumidityThresholds(
                                pageNo, DEFAULT_PAGE_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION))
                .willReturn(humidityThresholdPagedResult);

        this.mockMvc
                .perform(get("/HumidityThreshold"))
                .andDo(
                        document(
                                "shouldFetchAllHumidityThresholds",
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
    void shouldFindHumidityThresholdById() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        HumidityThreshold humidityThreshold = new HumidityThreshold(humidityThresholdId, 90, 10);
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.of(humidityThreshold));

        this.mockMvc
                .perform(get("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andDo(document("shouldFindHumidityThresholdById", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(humidityThresholdId.toString())))
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andReturn();
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenFetchingNonExistingHumidityThreshold",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewHumidityThreshold() throws Exception {
        given(humidityThresholdService.saveHumidityThreshold(any(HumidityThreshold.class)))
                .willAnswer(
                        invocation -> {
                            HumidityThreshold humidityThreshold = invocation.getArgument(0);
                            humidityThreshold.setId(UUID.randomUUID());
                            return humidityThreshold;
                        });

        HumidityThreshold humidityThreshold = new HumidityThreshold(null, 100, 0);
        this.mockMvc
                .perform(
                        post("/HumidityThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andDo(
                        document(
                                "shouldCreateNewHumidityThreshold",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andReturn();
    }

    @Test
    void shouldReturn400WhenCreateNewHumidityThresholdWithNullValues() throws Exception {
        HumidityThreshold humidityThreshold = new HumidityThreshold();

        this.mockMvc
                .perform(
                        post("/HumidityThreshold")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andDo(
                        document(
                                "shouldReturn400WhenCreateNewHumidityThresholdWithNullValues",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(
                        jsonPath(
                                "$.type",
                                is("https://zalando.github.io/problem/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("humidityThresholdDTO")))
                .andExpect(
                        jsonPath(
                                "$.violations[0].message",
                                is("Min. value must be less than Max. value")))
                .andReturn();
    }

    @Test
    void shouldUpdateHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        HumidityThreshold humidityThreshold = new HumidityThreshold(humidityThresholdId, 0, 100);
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.of(humidityThreshold));
        given(humidityThresholdService.saveHumidityThreshold(any(HumidityThreshold.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        this.mockMvc
                .perform(
                        put("/HumidityThreshold/{id}", humidityThreshold.getId())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andDo(
                        document(
                                "shouldUpdateHumidityThreshold",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andReturn();
        ;
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.empty());
        HumidityThreshold humidityThreshold = new HumidityThreshold(humidityThresholdId, 0, 100);

        this.mockMvc
                .perform(
                        put("/HumidityThreshold/{id}", humidityThresholdId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(humidityThreshold)))
                .andDo(
                        document(
                                "shouldReturn404WhenUpdatingNonExistingHumidityThreshold",
                                preprocessRequest(prettyPrint),
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        HumidityThreshold humidityThreshold = new HumidityThreshold(humidityThresholdId, 0, 100);
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.of(humidityThreshold));
        doNothing()
                .when(humidityThresholdService)
                .deleteHumidityThresholdById(humidityThreshold.getId());

        this.mockMvc
                .perform(delete("/HumidityThreshold/{id}", humidityThreshold.getId()).with(csrf()))
                .andDo(document("shouldDeleteHumidityThreshold", preprocessResponse(prettyPrint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxValue", is(humidityThreshold.getMaxValue())))
                .andExpect(jsonPath("$.minValue", is(humidityThreshold.getMinValue())))
                .andReturn();
        ;
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingHumidityThreshold() throws Exception {
        UUID humidityThresholdId = UUID.randomUUID();
        given(humidityThresholdService.findHumidityThresholdById(humidityThresholdId))
                .willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/HumidityThreshold/{id}", humidityThresholdId).with(csrf()))
                .andDo(
                        document(
                                "shouldReturn404WhenDeletingNonExistingHumidityThreshold",
                                preprocessResponse(prettyPrint)))
                .andExpect(status().isNotFound());
    }
}
