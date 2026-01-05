package org.clokey.domain.like.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.like.dto.response.LikedHistoriesResponse;
import org.clokey.domain.like.service.LikeService;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(LikeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LikeControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private LikeService likeService;

    @Nested
    class 좋아요_요청_시 {
        @Test
        void 유효한_요청이면_성공코드를_반환한다() throws Exception {
            // given
            long historyId = 1L;

            willDoNothing().given(likeService).toggleLike(historyId);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/likes")
                                    .param("historyId", String.valueOf(historyId))
                                    .contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 좋아요한_기록_조회_시 {
        @Test
        void 유효한_요청이면_좋아요한_기록을_반환한다() throws Exception {
            // given
            List<LikedHistoriesResponse.LikedHistoryPreview> previews =
                    List.of(
                            new LikedHistoriesResponse.LikedHistoryPreview(
                                    1L, "https://img.com/img1.jpg"),
                            new LikedHistoriesResponse.LikedHistoryPreview(
                                    2L, "https://img.com/img2.jpg"));

            SliceResponse<LikedHistoriesResponse.LikedHistoryPreview> sliceResponse =
                    new SliceResponse<>(previews, true);

            given(likeService.getLikedHistories(any(), anyInt())).willReturn(sliceResponse);

            ResultActions perform =
                    mockMvc.perform(
                            get("/likes/histories")
                                    .param("size", "10")
                                    .contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].id").value(1L))
                    .andExpect(
                            jsonPath("$.result.content[0].imageUrl")
                                    .value("https://img.com/img1.jpg"))
                    .andExpect(jsonPath("$.result.content[1].id").value(2L))
                    .andExpect(
                            jsonPath("$.result.content[1].imageUrl")
                                    .value("https://img.com/img2.jpg"))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<LikedHistoriesResponse.LikedHistoryPreview> previews =
                    List.of(
                            new LikedHistoriesResponse.LikedHistoryPreview(
                                    1L, "https://img.com/img1.jpg"),
                            new LikedHistoriesResponse.LikedHistoryPreview(
                                    2L, "https://img.com/img2.jpg"));

            SliceResponse<LikedHistoriesResponse.LikedHistoryPreview> sliceResponse =
                    new SliceResponse<>(previews, false);

            given(likeService.getLikedHistories(any(), anyInt())).willReturn(sliceResponse);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/likes/histories")
                                    .param("size", "10")
                                    .contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].id").value(1L))
                    .andExpect(
                            jsonPath("$.result.content[0].imageUrl")
                                    .value("https://img.com/img1.jpg"))
                    .andExpect(jsonPath("$.result.content[1].id").value(2L))
                    .andExpect(
                            jsonPath("$.result.content[1].imageUrl")
                                    .value("https://img.com/img2.jpg"))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }
    }
}
