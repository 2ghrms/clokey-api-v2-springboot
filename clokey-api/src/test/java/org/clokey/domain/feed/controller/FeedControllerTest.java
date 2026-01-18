package org.clokey.domain.feed.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.clokey.domain.feed.dto.response.FeedListResponse;
import org.clokey.domain.feed.query.FollowScope;
import org.clokey.domain.feed.service.FeedService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(FeedController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FeedControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private FeedService feedService;

    @Nested
    class 피드_조회_요청_시 {

        @Test
        void 유효한_요청이면_피드_리스트를_반환한다() throws Exception {
            // given
            FeedListResponse response =
                    FeedListResponse.of(
                            List.of(
                                    new FeedListResponse.FeedItemResponse(
                                            10L,
                                            LocalDateTime.parse("2025-01-01T12:00:00"),
                                            "https://image.test/10.png",
                                            true,
                                            new FeedListResponse.FeedAuthorResponse(
                                                    2L,
                                                    "clokey2",
                                                    "https://image.test/p2.png",
                                                    true))),
                            "nextCursorValue",
                            true);

            given(
                            feedService.getFeeds(
                                    eq(FollowScope.FOLLOWING),
                                    eq(List.of(1L, 2L)),
                                    eq(List.of(3L)),
                                    eq(10),
                                    eq("cursorValue")))
                    .willReturn(response);
            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/feeds")
                                    .param("followScope", "FOLLOWING")
                                    .param("styleIds", "1,2")
                                    .param("situationIds", "3")
                                    .param("size", "10")
                                    .param("cursor", "cursorValue"));
            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.items[0].feedId").value(10L))
                    .andExpect(
                            jsonPath("$.result.items[0].imageUrl")
                                    .value("https://image.test/10.png"))
                    .andExpect(jsonPath("$.result.items[0].isLiked").value(true))
                    .andExpect(jsonPath("$.result.items[0].author.memberId").value(2L))
                    .andExpect(jsonPath("$.result.items[0].author.clokeyId").value("clokey2"))
                    .andExpect(
                            jsonPath("$.result.items[0].author.profileImageUrl")
                                    .value("https://image.test/p2.png"))
                    .andExpect(jsonPath("$.result.items[0].author.isFollowing").value(true))
                    .andExpect(jsonPath("$.result.nextCursor").value("nextCursorValue"))
                    .andExpect(jsonPath("$.result.hasNext").value(true));
        }
    }
}
