package org.clokey.domain.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.clokey.domain.auth.dto.request.DeviceTokenRenewRequest;
import org.clokey.domain.auth.dto.request.TokenReissueRequest;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.enums.RegisterStatus;
import org.clokey.domain.auth.service.AuthService;
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

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean AuthService authService;

    @Nested
    class 유저의_상태_확인_요청_시 {

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

    @Nested
    class 디바이스_토큰_갱신_요청_시 {

        @Test
        void 유효한_요청이면_디바이스_토큰을_갱신하고_NO_CONTENT를_반환한다() throws Exception {
            // given
            DeviceTokenRenewRequest request = new DeviceTokenRenewRequest("testDeviceToken");
            willDoNothing().given(authService).renewDeviceToken(request);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/auth/device-token")
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
            DeviceTokenRenewRequest request = new DeviceTokenRenewRequest(deviceToken);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/auth/device-token")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.deviceToken").value("Device Token은 비워둘 수 없습니다."));
        }
    }

    @Nested
    class 토큰_재발급_요청_시 {

        @Test
        void 유효한_요청이면_토큰을_재발급_후_반환한다() throws Exception {
            // given
            TokenReissueRequest request = new TokenReissueRequest("oldRefreshToken");
            TokenResponse response = new TokenResponse("newAccessToken", "newRefreshToken");
            given(authService.reissueTokens(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/auth/reissue-token")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.accessToken").value("newAccessToken"))
                    .andExpect(jsonPath("$.result.refreshToken").value("newRefreshToken"));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 리프레시_토큰이_null_또는_공백이면_예외가_발생한다(String refreshToken) throws Exception {
            // given
            TokenReissueRequest request = new TokenReissueRequest(refreshToken);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/auth/reissue-token")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.refreshToken").value("리프레시 토큰은 비워둘 수 없습니다."));
        }
    }

    @Nested
    class 로그아웃_요청_시 {

        @Test
        void 리프레시_토큰을_만료_처리하고_NO_CONTENT를_반환한다() throws Exception {
            // given
            willDoNothing().given(authService).logoutUser();

            // when & then
            ResultActions perform = mockMvc.perform(post("/auth/logout"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }
}
