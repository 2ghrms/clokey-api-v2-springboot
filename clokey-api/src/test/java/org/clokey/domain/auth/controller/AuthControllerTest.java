package org.clokey.domain.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.enums.RegisterStatus;
import org.clokey.domain.auth.service.AuthService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean AuthService authService;

    @Nested
    class 유저의_상태를_확인할_때 {

        @Test
        void 약관에_동의한_유저의_경우_REGISTERED를_반환한다() throws Exception {
            // given
            UserStatusResponse response = new UserStatusResponse(RegisterStatus.REGISTERED);
            given(authService.getUserStatus()).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(get("/auth/my-status").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.registerStatus").value("REGISTERED"));
        }

        @Test
        void 약관에_동의하지_않은_유저의_경우_NOT_AGREED를_반환한다() throws Exception {
            // given
            UserStatusResponse response = new UserStatusResponse(RegisterStatus.NOT_AGREED);
            given(authService.getUserStatus()).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(get("/auth/my-status").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.registerStatus").value("NOT_AGREED"));
        }
    }
}
