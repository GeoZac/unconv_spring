package com.unconv.spring.matchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.test.web.servlet.ResultMatcher;

public class PagedResponseMatcher {
    public static ResultMatcher pagedResponse() {
        return result -> {
            jsonPath("$.data").exists().match(result);
            jsonPath("$.totalElements").exists().match(result);
            jsonPath("$.pageNumber").exists().match(result);
            jsonPath("$.totalPages").exists().match(result);
            jsonPath("$.isFirst").exists().match(result);
            jsonPath("$.isLast").exists().match(result);
            jsonPath("$.hasNext").exists().match(result);
            jsonPath("$.hasPrevious").exists().match(result);
        };
    }
}
