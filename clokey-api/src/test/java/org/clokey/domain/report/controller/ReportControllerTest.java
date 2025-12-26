package org.clokey.domain.report.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.clokey.domain.report.dto.request.ReportCreateRequest;
import org.clokey.domain.report.dto.response.ReportCreateResponse;
import org.clokey.domain.report.service.ReportService;
import org.clokey.report.enums.ReportReason;
import org.clokey.report.enums.TargetType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean ReportService reportService;

    @Nested
    class 신고_생성_요청_시 {

        @Test
        void 유효한_요청이면_신고를_생성하고_ID를_반환한다() throws Exception {
            // given
            ReportCreateRequest request =
                    new ReportCreateRequest(
                            1L,
                            TargetType.COMMENT,
                            ReportReason.SWEARING_AND_CURSING,
                            "댓글에 인신모욕이 있어요 ㅠㅠ");
            ReportCreateResponse response = new ReportCreateResponse(1L);

            given(reportService.createReport(request)).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/reports")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("COMMON201"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.reportId").value(1));
        }

        @Test
        void 콘텐츠_ID를_비워두면_예외가_발생한다() throws Exception {
            // given
            ReportCreateRequest request =
                    new ReportCreateRequest(
                            null,
                            TargetType.COMMENT,
                            ReportReason.SWEARING_AND_CURSING,
                            "댓글에 인신모욕이 있어요 ㅠㅠ");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/reports")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(false))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("COMMON400"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.result.targetId")
                                    .value("신고 컨텐츠 ID는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {"DONGYEOP", "GENIUS"})
        void 신고_컨텐츠_타입을_비워두거나_지원하지_않는_형식이면_예외가_발생한다(String type) throws Exception {
            // given
            ReportCreateRequest request =
                    new ReportCreateRequest(
                            1L,
                            TargetType.from(type),
                            ReportReason.SWEARING_AND_CURSING,
                            "댓글에 인신모욕이 있어요 ㅠㅠ");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/reports")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(false))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("COMMON400"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.result.targetType")
                                    .value("신고 컨텐츠 타입은 비워둘 수 없으며 COMMENT/REPLY/HISTORY 존재"));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {"BABO", "WOW"})
        void 신고_사유를_비워두거나_지원하지_않는_형식이면_예외가_발생한다(String reason) throws Exception {
            // given
            ReportCreateRequest request =
                    new ReportCreateRequest(
                            1L, TargetType.COMMENT, ReportReason.from(reason), "댓글에 인신모욕이 있어요 ㅠㅠ");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/reports")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(false))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("COMMON400"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.result.reportReason")
                                    .value("신고 사유는 비워둘 수 없습니다."));
        }
    }
}
