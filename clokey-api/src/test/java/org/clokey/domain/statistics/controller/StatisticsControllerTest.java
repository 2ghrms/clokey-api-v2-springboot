package org.clokey.domain.statistics.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.statistics.dto.response.ClosetUtilizationResponse;
import org.clokey.domain.statistics.dto.response.FavoriteCategoryItemsResponse;
import org.clokey.domain.statistics.dto.response.FavoriteItemsResponse;
import org.clokey.domain.statistics.dto.response.StatisticsCheckConditionResponse;
import org.clokey.domain.statistics.service.StatisticsService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(StatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatisticsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private StatisticsService statisticsService;

    @Nested
    class 통계_최소_조건_확인_요청_시 {

        @Test
        void 유효한_요청이면_통계_집계_가능_여부를_반환한다() throws Exception {
            // given
            StatisticsCheckConditionResponse response = StatisticsCheckConditionResponse.of(true);

            given(statisticsService.checkStatisticsCondition()).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/statistics/check-conditions")
                                    .contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.canAggregate").value(true));
        }
    }

    @Nested
    class 카테고리별_최애_아이템_조회_요청_시 {

        @Test
        void 유효한_요청이면_카테고리별_최애_아이템을_반환한다() throws Exception {
            // given
            FavoriteCategoryItemsResponse response =
                    FavoriteCategoryItemsResponse.of(
                            List.of(
                                    new FavoriteCategoryItemsResponse.Payload(1L, "맨투맨", 0.5, 10L),
                                    new FavoriteCategoryItemsResponse.Payload(2L, "후드티", 0.3, 6L),
                                    new FavoriteCategoryItemsResponse.Payload(3L, "반바지", 0.2, 4L)));

            given(statisticsService.getFavoriteCategoryItems(1L)).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/statistics/favorite-category-items")
                                    .param("categoryId", "1")
                                    .contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.payloads", hasSize(3)))
                    .andExpect(jsonPath("$.result.payloads[*].categoryId").value(contains(1, 2, 3)))
                    .andExpect(
                            jsonPath("$.result.payloads[*].categoryName")
                                    .value(contains("맨투맨", "후드티", "반바지")))
                    .andExpect(
                            jsonPath("$.result.payloads[*].occupancyRate")
                                    .value(contains(0.5, 0.3, 0.2)))
                    .andExpect(
                            jsonPath("$.result.payloads[*].clothCount").value(contains(10, 6, 4)));
        }
    }

    @Nested
    class 옷장_아이템_통계_조회_요청_시 {

        @Test
        void 유효한_요청이면_옷장_아이템_통계를_반환한다() throws Exception {
            // given
            FavoriteItemsResponse response =
                    FavoriteItemsResponse.of(
                            List.of(
                                    new FavoriteItemsResponse.Payload(1L, "맨투맨", 10L),
                                    new FavoriteItemsResponse.Payload(2L, "후드티", 8L),
                                    new FavoriteItemsResponse.Payload(3L, "니트", 5L)));

            given(statisticsService.getFavoriteItems()).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/statistics/favorite-items")
                                    .contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.payloads", hasSize(3)))
                    .andExpect(jsonPath("$.result.payloads[*].categoryId").value(contains(1, 2, 3)))
                    .andExpect(
                            jsonPath("$.result.payloads[*].categoryName")
                                    .value(contains("맨투맨", "후드티", "니트")))
                    .andExpect(
                            jsonPath("$.result.payloads[*].clothCount").value(contains(10, 8, 5)));
        }
    }

    @Nested
    class 옷장_활용도_조회_요청_시 {

        @Test
        void 유효한_요청이면_옷장_활용도를_반환한다() throws Exception {
            // given
            ClosetUtilizationResponse response =
                    ClosetUtilizationResponse.of(
                            15L,
                            10L,
                            List.of(
                                    new ClosetUtilizationResponse.Payload(
                                            "https://example.com/image1.jpg", "맨투맨", "나이키")),
                            List.of(
                                    new ClosetUtilizationResponse.Payload(
                                            "https://example.com/image2.jpg", "후드티", "아디다스")));

            given(statisticsService.getClosetUtilization(Season.SPRING)).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/statistics/closet-utilization")
                                    .param("season", "SPRING")
                                    .contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.utilizedCount").value(15))
                    .andExpect(jsonPath("$.result.unutilizedCount").value(10))
                    .andExpect(jsonPath("$.result.utilizedClothes", hasSize(1)))
                    .andExpect(
                            jsonPath("$.result.utilizedClothes[*].imageUrl")
                                    .value(contains("https://example.com/image1.jpg")))
                    .andExpect(jsonPath("$.result.utilizedClothes[*].name").value(contains("맨투맨")))
                    .andExpect(jsonPath("$.result.utilizedClothes[*].brand").value(contains("나이키")))
                    .andExpect(jsonPath("$.result.unutilizedClothes", hasSize(1)))
                    .andExpect(
                            jsonPath("$.result.unutilizedClothes[*].imageUrl")
                                    .value(contains("https://example.com/image2.jpg")))
                    .andExpect(
                            jsonPath("$.result.unutilizedClothes[*].name").value(contains("후드티")))
                    .andExpect(
                            jsonPath("$.result.unutilizedClothes[*].brand")
                                    .value(contains("아디다스")));
        }
    }
}
