package com.unconv.spring.matchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.test.web.servlet.ResultMatcher;

public class PagedResponseMatcher {
    public static ResultMatcher pagedResponse(int dataSize, int defaultPageSize, int totalPages) {
        return result -> {
            jsonPath("$.data.size()").value(defaultPageSize).match(result);
            jsonPath("$.totalElements").value(dataSize).match(result);
            jsonPath("$.pageNumber").value(0).match(result);
            jsonPath("$.totalPages").value(totalPages).match(result);
            jsonPath("$.isFirst").value(true).match(result);
            jsonPath("$.isLast").value(dataSize < defaultPageSize).match(result);
            jsonPath("$.hasNext").value(dataSize > defaultPageSize).match(result);
            jsonPath("$.hasPrevious").value(false).match(result);
        };
    }
}
