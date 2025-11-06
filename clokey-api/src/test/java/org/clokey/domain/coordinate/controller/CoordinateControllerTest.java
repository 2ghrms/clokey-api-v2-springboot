package org.clokey.domain.coordinate.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.clokey.domain.coordinate.dto.request.CoordinateAutoCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateManualCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateUpdateRequest;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.*;
import org.clokey.domain.coordinate.service.CoordinateService;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;
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
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));
            CoordinateCreateResponse response = new CoordinateCreateResponse(1L);
            given(coordinateService.createDailyCoordinate(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.coordinateId").value(1));
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
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
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
                            post("/coordinate/daily")
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
                                            1L, null, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
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
                                            1L, locationX, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
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
                                            1L, 100.5, null, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
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
                                            1L, 100.5, locationY, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
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
                                            1L, 100.5, 200.25, null, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
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
                                            1L, 100.5, 200.25, ratio, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
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
                                            1L, 100.5, 200.25, 1.0, 50.0, null)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.order").value("옷의 순서는 비워둘 수 없습니다."));
        }

        @Test
        void 옷의_각도를_비워두면_예외가_발생한다() throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, null, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("옷의 각도는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.5, -2.0})
        void 옷의_각도가_음수면_예외가_발생한다(Double degree) throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, degree, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("각도는 0도 이상이어야 합니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {360.1, 1000.1})
        void 옷의_각도가_360도를_넘어가면_예외가_발생한다(Double degree) throws Exception {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, degree, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/daily")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("각도는 360도 이하여야 합니다."));
        }
    }

    @Nested
    class 코디_수동_생성_요청_시 {

        @Test
        void 유효한_요청이면_코디를_생성하고_ID를_반환한다() throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));
            CoordinateCreateResponse response = new CoordinateCreateResponse(1L);
            given(coordinateService.createCoordinateManual(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.coordinateId").value(1));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 코디의_이미지_url이_null_또는_공백이면_예외가_발생한다(String coordinateImageUrl) throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            coordinateImageUrl,
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            jsonPath("$.result.coordinateImageUrl").value("코디의 사진은 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 코디의_이름이_null_또는_공백이면_예외가_발생한다(String name) throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            name,
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.name").value("코디의 이름은 비워둘 수 없습니다."));
        }

        @Test
        void 코디의_메모가_100자를_넘어가면_예외가_발생한다() throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "t".repeat(101),
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.memo").value("메모는 최대 100자까지 입력할 수 있습니다."));
        }

        @Test
        void 룩북IO를_비워두면_예외가_발생한다() throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            null,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.lookBookId").value("룩북 ID는 비워둘 수 없습니다."));
        }

        @Test
        void 코디의_Payload를_비워두면_예외가_발생한다() throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl", "testName", "testMemo", 1L, List.of());

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
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
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, null, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
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
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, locationX, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
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
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, null, 1.0, 50.0, 1)));
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
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
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, locationY, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
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
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.24, null, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
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
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, ratio, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
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
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, null)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.order").value("옷의 순서는 비워둘 수 없습니다."));
        }

        @Test
        void 옷의_각도를_비워두면_예외가_발생한다() throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, null, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("옷의 각도는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.5, -2.0})
        void 옷의_각도가_음수면_예외가_발생한다(Double degree) throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, degree, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("각도는 0도 이상이어야 합니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {360.1, 1000.1})
        void 옷의_각도가_360도를_넘어가면_예외가_발생한다(Double degree) throws Exception {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, degree, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/manual")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("각도는 360도 이하여야 합니다."));
        }
    }

    @Nested
    class 코디_자동_생성_요청_시 {

        @Test
        void 유효한_요청이면_코디를_자동으로_생성하고_ID를_반환한다() throws Exception {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("testName", "testMemo", 1L, 1L);

            CoordinateCreateResponse response = new CoordinateCreateResponse(1L);
            given(coordinateService.createCoordinateAuto(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/auto")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.coordinateId").value(1));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 코디의_이름이_null_또는_공백이면_예외가_발생한다(String name) throws Exception {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest(name, "testMemo", 1L, 1L);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/auto")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.name").value("코디의 이름은 비워둘 수 없습니다."));
        }

        @Test
        void 코디의_메모가_100자를_넘어가면_예외가_발생한다() throws Exception {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("testName", "t".repeat(101), 1L, 1L);
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/auto")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.memo").value("메모는 최대 100자까지 입력할 수 있습니다."));
        }

        @Test
        void 일일_코디_IO를_비워두면_예외가_발생한다() throws Exception {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("testName", "testMemo", null, 1L);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/auto")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            jsonPath("$.result.dailyCoordinateId").value("일일 코디 ID는 비워둘 수 없습니다."));
        }

        @Test
        void 룩북IO를_비워두면_예외가_발생한다() throws Exception {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("testName", "testMemo", 1L, null);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/coordinate/auto")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.lookBookId").value("룩북 ID는 비워둘 수 없습니다."));
        }
    }

    @Nested
    class 코디_업데이트_요청_시 {

        @Test
        void 유효한_요청이면_코디를_업데이트_한다() throws Exception {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));
            willDoNothing().given(coordinateService).updateCoordinate(1L, request);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
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
        void 코디의_이미지_url이_null_또는_공백이면_예외가_발생한다(String coordinateImageUrl) throws Exception {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            coordinateImageUrl,
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            jsonPath("$.result.coordinateImageUrl").value("코디의 사진은 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 코디의_이름이_null_또는_공백이면_예외가_발생한다(String name) throws Exception {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            name,
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.name").value("코디의 이름은 비워둘 수 없습니다."));
        }

        @Test
        void 코디의_메모가_100자를_넘어가면_예외가_발생한다() throws Exception {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "t".repeat(101),
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.memo").value("메모는 최대 100자까지 입력할 수 있습니다."));
        }

        @Test
        void 코디의_Payload를_비워두면_예외가_발생한다() throws Exception {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest("testUrl", "testName", "testMemo", List.of());

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
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
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, null, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
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
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, locationX, 200.25, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
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
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, null, 1.0, 50.0, 1)));
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
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
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, locationY, 1.0, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
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
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, null, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
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
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, ratio, 50.0, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
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
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, null)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.order").value("옷의 순서는 비워둘 수 없습니다."));
        }

        @Test
        void 옷의_각도를_비워두면_예외가_발생한다() throws Exception {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, null, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("옷의 각도는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.5, -2.0})
        void 옷의_각도가_음수면_예외가_발생한다(Double degree) throws Exception {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, degree, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("각도는 0도 이상이어야 합니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {360.1, 1000.1})
        void 옷의_각도가_360도를_넘어가면_예외가_발생한다(Double degree) throws Exception {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, degree, 1)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.degree").value("각도는 360도 이하여야 합니다."));
        }
    }

    @Nested
    class 코디ㅡ삭제_요청_시 {

        @Test
        void 유효한_요청이면_코디를_삭제_한다() throws Exception {
            // given
            willDoNothing().given(coordinateService).deleteCoordinate(1L);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            delete("/coordinate/1").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 오늘의_코디_목록_조회_요청_시 {

        @Test
        void 정렬_조건이_ASC이면_coordinateId를_오름차순으로_응답한다() throws Exception {
            // given
            List<DailyCoordinateListResponse> dailyCoordinates =
                    List.of(
                            new DailyCoordinateListResponse(
                                    1L, "testImageUrl1", LocalDateTime.of(2025, 1, 1, 1, 1)),
                            new DailyCoordinateListResponse(
                                    2L, "testImageUrl1", LocalDateTime.of(2025, 1, 2, 1, 1)),
                            new DailyCoordinateListResponse(
                                    3L, "testImageUrl1", LocalDateTime.of(2025, 1, 3, 1, 1)));

            given(coordinateService.getDailyCoordinates(1L, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(dailyCoordinates, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/daily")
                                    .param("lastCoordinateId", "1")
                                    .param("size", "3")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].coordinateId").value(1))
                    .andExpect(jsonPath("$.result.content[1].coordinateId").value(2))
                    .andExpect(jsonPath("$.result.content[2].coordinateId").value(3))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 정렬_조건이_DESC이면_coordinateId를_내림차순으로_응답한다() throws Exception {
            // given
            List<DailyCoordinateListResponse> dailyCoordinates =
                    List.of(
                            new DailyCoordinateListResponse(
                                    3L, "testImageUrl1", LocalDateTime.of(2025, 1, 3, 1, 1)),
                            new DailyCoordinateListResponse(
                                    2L, "testImageUrl1", LocalDateTime.of(2025, 1, 2, 1, 1)),
                            new DailyCoordinateListResponse(
                                    1L, "testImageUrl1", LocalDateTime.of(2025, 1, 1, 1, 1)));

            given(coordinateService.getDailyCoordinates(3L, 3, SortDirection.DESC))
                    .willReturn(new SliceResponse<>(dailyCoordinates, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/daily")
                                    .param("lastCoordinateId", "3")
                                    .param("size", "3")
                                    .param("direction", "DESC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].coordinateId").value(3))
                    .andExpect(jsonPath("$.result.content[1].coordinateId").value(2))
                    .andExpect(jsonPath("$.result.content[2].coordinateId").value(1))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_응답한다() throws Exception {
            // given
            List<DailyCoordinateListResponse> dailyCoordinates =
                    List.of(
                            new DailyCoordinateListResponse(
                                    1L, "testImageUrl1", LocalDateTime.of(2025, 1, 1, 1, 1)),
                            new DailyCoordinateListResponse(
                                    2L, "testImageUrl1", LocalDateTime.of(2025, 1, 2, 1, 1)),
                            new DailyCoordinateListResponse(
                                    3L, "testImageUrl1", LocalDateTime.of(2025, 1, 3, 1, 1)));

            given(coordinateService.getDailyCoordinates(1L, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(dailyCoordinates, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/daily")
                                    .param("lastCoordinateId", "1")
                                    .param("size", "3")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<DailyCoordinateListResponse> dailyCoordinates =
                    List.of(
                            new DailyCoordinateListResponse(
                                    1L, "testImageUrl1", LocalDateTime.of(2025, 1, 1, 1, 1)),
                            new DailyCoordinateListResponse(
                                    2L, "testImageUrl1", LocalDateTime.of(2025, 1, 2, 1, 1)),
                            new DailyCoordinateListResponse(
                                    3L, "testImageUrl1", LocalDateTime.of(2025, 1, 3, 1, 1)));

            given(coordinateService.getDailyCoordinates(1L, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(dailyCoordinates, false));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/daily")
                                    .param("lastCoordinateId", "1")
                                    .param("size", "2")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].coordinateId").value(1))
                    .andExpect(jsonPath("$.result.content[1].coordinateId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @Test
        void 기록에_댓글이_없는_경우_빈_리스트를_응답한다() throws Exception {
            // given
            List<DailyCoordinateListResponse> dailyCoordinates = List.of();

            given(coordinateService.getDailyCoordinates(null, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(dailyCoordinates, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/daily").param("size", "2").param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content").isEmpty())
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-999", "0"})
        void 페이지_크기를_0_이하로_설정하면_예외가_발생한다(String pageSize) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/daily")
                                    .param("size", pageSize)
                                    .param("direction", "ASC"));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ASCC", "DESCC", "OLDEST", "NEWEST"})
        void 존재하지_않는_정렬_기준을_입력한_경우_예외가_발생한다(String sort) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/daily").param("size", "1").param("direction", sort));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }
    }

    @Nested
    class 코디_Preview_조회_요청_시 {

        @Test
        void 유효한_요청이면_코디_Preview를_반환한다() throws Exception {
            // given
            CoordinatePreviewResponse response =
                    new CoordinatePreviewResponse(1L, "testImageUrl", "testName", "testMemo");
            given(coordinateService.getCoordinatePreview(1L)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/1/preview").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.coordinateId").value(1))
                    .andExpect(jsonPath("$.result.imageUrl").value("testImageUrl"))
                    .andExpect(jsonPath("$.result.coordinateName").value("testName"))
                    .andExpect(jsonPath("$.result.coordinateMemo").value("testMemo"));
        }
    }

    @Nested
    class 코디_Details_조회_요청_시 {

        @Test
        void 유효한_요청이면_코디_Details를_반환한다() throws Exception {
            // given
            List<CoordinateDetailsListResponse> response =
                    List.of(
                            new CoordinateDetailsListResponse(
                                    1L,
                                    50.2,
                                    60.1,
                                    1.5,
                                    240.1,
                                    1,
                                    "testImageUrl1",
                                    "testBrand1",
                                    "testName1",
                                    "testCategoryName1",
                                    "testParentCategoryName1"),
                            new CoordinateDetailsListResponse(
                                    2L,
                                    50.2,
                                    60.1,
                                    1.5,
                                    240.1,
                                    2,
                                    "testImageUrl2",
                                    "testBrand2",
                                    "testName2",
                                    "testCategoryName2",
                                    "testParentCategoryName2"));

            given(coordinateService.getCoordinateDetails(1L)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/coordinate/1/details").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result[0].coordinateClothId").value(1))
                    .andExpect(jsonPath("$.result[0].locationX").value(50.2))
                    .andExpect(jsonPath("$.result[0].locationY").value(60.1))
                    .andExpect(jsonPath("$.result[0].ratio").value(1.5))
                    .andExpect(jsonPath("$.result[0].degree").value(240.1))
                    .andExpect(jsonPath("$.result[0].order").value(1))
                    .andExpect(jsonPath("$.result[0].imageUrl").value("testImageUrl1"))
                    .andExpect(jsonPath("$.result[0].brand").value("testBrand1"))
                    .andExpect(jsonPath("$.result[0].name").value("testName1"))
                    .andExpect(jsonPath("$.result[0].category").value("testCategoryName1"))
                    .andExpect(
                            jsonPath("$.result[0].parentCategory").value("testParentCategoryName1"))
                    .andExpect(jsonPath("$.result[1].coordinateClothId").value(2))
                    .andExpect(jsonPath("$.result[1].locationX").value(50.2))
                    .andExpect(jsonPath("$.result[1].locationY").value(60.1))
                    .andExpect(jsonPath("$.result[1].ratio").value(1.5))
                    .andExpect(jsonPath("$.result[1].degree").value(240.1))
                    .andExpect(jsonPath("$.result[1].order").value(2))
                    .andExpect(jsonPath("$.result[1].imageUrl").value("testImageUrl2"))
                    .andExpect(jsonPath("$.result[1].brand").value("testBrand2"))
                    .andExpect(jsonPath("$.result[1].name").value("testName2"))
                    .andExpect(jsonPath("$.result[1].category").value("testCategoryName2"))
                    .andExpect(
                            jsonPath("$.result[1].parentCategory")
                                    .value("testParentCategoryName2"));
        }
    }

    @Nested
    class 코디_좋아요_토글_요청_시 {

        @Test
        void 유효한_요청이면_코디_좋아요를_토글한다() throws Exception {
            // given
            willDoNothing().given(coordinateService).toggleCoordinateLike(1L);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/coordinate/1/like").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 최애_코디_조회_요청_시 {

        @Test
        void 유효한_요청이면_최애_코디_정보를_반환한다() throws Exception {
            // given
            List<FavoriteCoordinateResponse> response =
                    List.of(
                            new FavoriteCoordinateResponse(1L, "testImageUrl1"),
                            new FavoriteCoordinateResponse(2L, "testImageUrl2"));

            given(coordinateService.getFavoriteCoordinates()).willReturn(response);

            // when & then
            ResultActions perform = mockMvc.perform(get("/coordinate/my-favorites"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result[0].coordinateId").value(1))
                    .andExpect(jsonPath("$.result[0].imageUrl").value("testImageUrl1"))
                    .andExpect(jsonPath("$.result[1].coordinateId").value(2))
                    .andExpect(jsonPath("$.result[1].imageUrl").value("testImageUrl2"));
        }
    }
}
