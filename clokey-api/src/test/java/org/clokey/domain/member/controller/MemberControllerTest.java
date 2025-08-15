package org.clokey.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.service.MemberService;
import org.clokey.member.enums.Visibility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MemberService memberService;

    @Nested
    class 프로필_수정_요청_시 {

        @Test
        void 유효한_요청이면_성공코드를_반환한다() throws Exception {
            // given
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            "testNickname",
                            "testClokeyId",
                            "testBio",
                            Visibility.PUBLIC,
                            "https://img.example.com/bg.jpg",
                            "https://img.example.com/bg.jpg");

            willDoNothing().given(memberService).updateProfile(any(ProfileUpdateRequest.class));

            // when
            ResultActions perform =
                    mockMvc.perform(
                            patch("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 닉네임_비어있으면_예외가_발생한다(String nickname) throws Exception {
            // given
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            nickname,
                            "testClokeyId",
                            "testBio",
                            Visibility.PRIVATE,
                            "https://img.example.com/bg.jpg",
                            "https://img.example.com/bg.jpg");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            patch("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.nickname").value("닉네임은 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void 클로키아이디_비어있으면_예외가_발생한다(String clokeyId) throws Exception {
            // given
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            "testNickname",
                            clokeyId,
                            "testBio",
                            Visibility.PRIVATE,
                            "https://img.example.com/bg.jpg",
                            "https://img.example.com/bg.jpg");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            patch("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.clokeyId").value("Clokey ID는 비워둘 수 없습니다."));
        }

        @Test
        void 바이오가_100자를_초과하면_예외가_발생한다() throws Exception {
            // given
            String longBio = "a".repeat(101); // 101자
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            "testNickname",
                            "testClokeyId",
                            longBio,
                            Visibility.PRIVATE,
                            "https://img.example.com/bg.jpg",
                            "https://img.example.com/bg.jpg");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            patch("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.bio").value("바이오는 100자를 넘길 수 없습니다."));
        }
    }
}
