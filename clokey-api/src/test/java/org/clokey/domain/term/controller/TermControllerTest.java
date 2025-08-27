package org.clokey.domain.term.controller;

import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.term.dto.request.TermAgreeRequest;
import org.clokey.domain.term.dto.response.TermListResponse;
import org.clokey.domain.term.service.TermService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(TermController.class)
@AutoConfigureMockMvc(addFilters = false)
class TermControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private TermService termService;

    @Nested
    class 전체_약관_조회_요청_시 {

        @Test
        void 유효한_요청이면_전체_약관을_반환한다() throws Exception {
            // given
            TermListResponse response =
                    new TermListResponse(
                            List.of(
                                    new TermListResponse.Payload(
                                            1L, "testTerm1", "testBody1", false),
                                    new TermListResponse.Payload(
                                            2L, "testTerm2", "testBody2", false),
                                    new TermListResponse.Payload(
                                            3L, "testTerm3", "testBody3", false),
                                    new TermListResponse.Payload(
                                            4L, "testTerm4", "testBody4", true),
                                    new TermListResponse.Payload(
                                            5L, "testTerm5", "testBody5", true)));

            given(termService.getTerms()).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(get("/terms").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(
                            jsonPath("$.result.payloads[*].termId").value(contains(1, 2, 3, 4, 5)))
                    .andExpect(
                            jsonPath("$.result.payloads[*].title")
                                    .value(
                                            contains(
                                                    "testTerm1",
                                                    "testTerm2",
                                                    "testTerm3",
                                                    "testTerm4",
                                                    "testTerm5")))
                    .andExpect(
                            jsonPath("$.result.payloads[*].body")
                                    .value(
                                            contains(
                                                    "testBody1",
                                                    "testBody2",
                                                    "testBody3",
                                                    "testBody4",
                                                    "testBody5")))
                    .andExpect(
                            jsonPath("$.result.payloads[*].optional")
                                    .value(contains(false, false, false, true, true)));
        }
    }

    @Nested
    class 약관_동의_요청_시 {

        @Test
        void 유효한_요청이면_약관을_동의하고_NO_CONTENT를_반환한다() throws Exception {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(
                            List.of(
                                    new TermAgreeRequest.Payload(1L, true),
                                    new TermAgreeRequest.Payload(2L, true)));
            willDoNothing().given(termService).agreeTerm(request);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/terms")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }

        @Test
        void 약관_동의_정보를_비워두면_예외가_발생한다() throws Exception {
            // given
            TermAgreeRequest request = new TermAgreeRequest(List.of());

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/terms")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.payloads").value("약관 동의 정보는 비워둘 수 없습니다."));
        }

        @Test
        void 약관_ID를_비워두면_예외가_발생한다() throws Exception {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(List.of(new TermAgreeRequest.Payload(null, true)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/terms")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.termId").value("약관 ID는 비워둘 수 없습니다."));
        }

        @Test
        void 약관_동의_여부를_비워두면_예외가_발생한다() throws Exception {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(List.of(new TermAgreeRequest.Payload(1L, null)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/terms")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.agreed").value("약관 동의 여부는 비워둘 수 없습니다."));
        }
    }
}
