package org.clokey.domain.term.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.term.dto.TermAgreeRequest;
import org.clokey.domain.term.service.TermService;
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

@WebMvcTest(TermController.class)
@AutoConfigureMockMvc(addFilters = false)
class TermControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private TermService termService;

    @Nested
    class 약관_동의_요청_시 {

        @Test
        void 유효한_요청이면_약관을_동의하고_NO_CONTENT를_반환한다() throws Exception {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(
                            "testDeviceToken",
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

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void Device_Token을_비워두면_예외가_발생한다(String deviceToken) throws Exception {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(
                            deviceToken,
                            List.of(
                                    new TermAgreeRequest.Payload(1L, true),
                                    new TermAgreeRequest.Payload(2L, true)));

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
                    .andExpect(jsonPath("$.result.deviceToken").value("Device Token은 비워둘 수 없습니다."));
        }

        @Test
        void 약관_동의_정보를_비워두면_예외가_발생한다() throws Exception {
            // given
            TermAgreeRequest request = new TermAgreeRequest("testDeviceToken", List.of());

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
                    new TermAgreeRequest(
                            "testDeviceToken", List.of(new TermAgreeRequest.Payload(null, true)));

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
                    new TermAgreeRequest(
                            "testDeviceToken", List.of(new TermAgreeRequest.Payload(1L, null)));

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
