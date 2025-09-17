package org.clokey.domain.coordinate.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.DailyCoordinateCreateResponse;
import org.clokey.domain.coordinate.service.CoordinateService;
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

@WebMvcTest(CoordinateController.class)
@AutoConfigureMockMvc(addFilters = false)
class CoordinateControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CoordinateService coordinateService;

    @Nested
    class 오늘의_코디_생성_요청_시 {

        @Test
        void 유효한_요청이면_오늘의_코디를_생성하고_ID를_반환한다() throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 1)));
            DailyCoordinateCreateResponse response = new DailyCoordinateCreateResponse(1L);
            given(coordinateService.createDailyCoordinate(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.dailyCoordinateId").value(1));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 오늘의_코디의_이미지_url이_null_또는_공백이면_예외가_발생한다(String coordinateImageUrl) throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            coordinateImageUrl,
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            jsonPath("$.result.coordinateImageUrl")
                                    .value("오늘의 코디의 사진은 비워둘 수 없습니다."));
        }

        @Test
        void 오늘의_코디의_Payload를_비워두면_예외가_발생한다() throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest("testImageUrl", List.of());

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.payloads").value("옷들의 정보를 비워둘 수 없습니다."));
        }

        @Test
        void 옷의_X_좌표를_비워두면_예외가_발생한다() throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, null, 200.25, 1.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.locationX").value("옷의 x좌표는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.5, -2.0})
        void 옷의_X_좌표를_음수로_입력하면_예외가_발생한다(Double locationX) throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, locationX, 200.25, 1.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.locationX").value("옷의 x좌표는 음수일 수 없습니다."));
        }

        @Test
        void 옷의_Y_좌표를_비워두면_예외가_발생한다() throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, null, 1.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.locationY").value("옷의 y좌표는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.5, -2.0})
        void 옷의_Y_좌표를_음수로_입력하면_예외가_발생한다(Double locationY) throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, locationY, 1.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.locationY").value("옷의 y좌표는 음수일 수 없습니다."));
        }

        @Test
        void 옷의_비율을_비워두면_예외가_발생한다() throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, null, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.ratio").value("옷의 비율은 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.5, -2.0})
        void 옷의_비율을_음수로_입력하면_예외가_발생한다(Double ratio) throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, ratio, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.ratio").value("옷의 비율은 음수일 수 없습니다."));
        }

        @Test
        void 옷의_순서를_비워두면_예외가_발생한다() throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, null)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.order").value("옷의 순서는 비워둘 수 없습니다."));
        }
    }
}
