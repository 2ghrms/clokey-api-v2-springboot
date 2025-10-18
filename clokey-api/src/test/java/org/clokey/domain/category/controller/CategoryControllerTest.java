package org.clokey.domain.category.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.category.dto.response.GetCategoryListResponse;
import org.clokey.domain.category.service.CategoryService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CategoryService categoryService;

    @Nested
    class 카테고리_조회_요청_시 {

        @Test
        void 유효한_요청이면_카테고리를_반환한다() throws Exception {
            // given
            GetCategoryListResponse top1 = new GetCategoryListResponse(2L, "상의1", List.of());
            GetCategoryListResponse top2 = new GetCategoryListResponse(3L, "상의2", List.of());
            GetCategoryListResponse bottom1 = new GetCategoryListResponse(5L, "하의1", List.of());
            GetCategoryListResponse bottom2 = new GetCategoryListResponse(6L, "하의2", List.of());

            GetCategoryListResponse top =
                    new GetCategoryListResponse(1L, "상의", List.of(top1, top2));
            GetCategoryListResponse bottom =
                    new GetCategoryListResponse(4L, "하의", List.of(bottom1, bottom2));

            List<GetCategoryListResponse> response = List.of(top, bottom);

            given(categoryService.getCategoryList()).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(get("/categories").contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result[0].name").value("상의"))
                    .andExpect(jsonPath("$.result[1].name").value("하의"))
                    .andExpect(jsonPath("$.result[0].children[0].name").value("상의1"))
                    .andExpect(jsonPath("$.result[0].children[1].name").value("상의2"))
                    .andExpect(jsonPath("$.result[1].children[0].name").value("하의1"))
                    .andExpect(jsonPath("$.result[1].children[1].name").value("하의2"));
        }
    }
}
