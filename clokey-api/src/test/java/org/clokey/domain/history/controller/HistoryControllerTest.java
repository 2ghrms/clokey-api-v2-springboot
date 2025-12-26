package org.clokey.domain.history.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.request.HistoryUpdateRequest;
import org.clokey.domain.history.dto.response.HistoryCreateResponse;
import org.clokey.domain.history.dto.response.SituationListResponse;
import org.clokey.domain.history.dto.response.StyleListResponse;
import org.clokey.domain.history.service.HistoryService;
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

@WebMvcTest(HistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HistoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private HistoryService historyService;

    @Nested
    class 기록_생성_요청_시 {

        @Test
        void 유효한_요청이면_기록을_생성하고_ID를_반환한다() throws Exception {
            // given
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testImageUrl1",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.42, 0.73),
                                                    new HistoryCreateRequest.ClothTag(
                                                            2L, 0.15, 0.85))),
                                    new HistoryCreateRequest.Payload(
                                            "testImageUrl2",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            3L, 0.33, 0.66)))));

            HistoryCreateResponse response = new HistoryCreateResponse(1L);
            given(historyService.createHistory(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType("application/json")
                                    .content(objectMapper.writeValueAsString(request)));
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.historyId").value(1L));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 내용이_null_또는_공백이면_예외가_발생한다(String content) throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            content,
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.42, 0.73)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("기록의 내용은 비워둘 수 없습니다."));
        }

        @Test
        void 내용이_120자를_초과하면_예외가_발생한다() throws Exception {
            String longContent = "a".repeat(121);
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            longContent,
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.42, 0.73)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("기록의 내용은 최대 120자까지 가능합니다."));
        }

        @Test
        void 상황ID가_null이면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            null,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.42, 0.73)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.situationId").value("상황 ID는 비워둘 수 없습니다."));
        }

        @Test
        void 스타일목록이_null이면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            null,
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.42, 0.73)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.styleIds").value("스타일 목록은 비워둘 수 없습니다."));
        }

        @Test
        void 스타일이_0개면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.42, 0.73)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.styleIds").value("스타일은 1~3개만 선택 가능합니다."));
        }

        @Test
        void 스타일이_3개_초과면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L, 3L, 4L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.42, 0.73)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.styleIds").value("스타일은 1~3개만 선택 가능합니다."));
        }

        @Test
        void 이미지목록이_null이면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            null);

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.payloads").value("기록 이미지 목록은 비워둘 수 없습니다."));
        }

        @Test
        void 이미지가_0개면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of());

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.payloads").value("이미지는 1~10개만 첨부할 수 있습니다."));
        }

        @Test
        void 이미지가_10개_초과면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null),
                                    new HistoryCreateRequest.Payload("testUrl", null)));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.payloads").value("이미지는 1~10개만 첨부할 수 있습니다."));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 이미지_URL이_null_또는_공백이면_예외가_발생한다(String imageUrl) throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(new HistoryCreateRequest.Payload(imageUrl, null)));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.imageUrl").value("기록의 사진은 비워둘 수 없습니다."));
        }

        @Test
        void 옷_ID가_null이면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            null, 0.1, 0.2)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.clothId").value("옷 ID는 비워둘 수 없습니다."));
        }

        @Test
        void X_좌표를_비워두면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, null, 0.2)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.locationX").value("옷의 x좌표는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.01, -3.14})
        void X_좌표를_음수로_입력하면_예외가_발생한다(Double locationX) throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, locationX, 0.2)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.locationX").value("옷의 x좌표는 음수일 수 없습니다."));
        }

        @Test
        void Y_좌표를_비워두면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.1, null)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.locationY").value("옷의 y좌표는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.01, -2.0})
        void Y_좌표를_음수로_입력하면_예외가_발생한다(Double locationY) throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new HistoryCreateRequest.Payload(
                                            "testUrl",
                                            List.of(
                                                    new HistoryCreateRequest.ClothTag(
                                                            1L, 0.1, locationY)))));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.locationY").value("옷의 y좌표는 음수일 수 없습니다."));
        }

        @Test
        void 해시태그_리스트에_공백문자열이_있으면_예외가_발생한다() throws Exception {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent",
                            1L,
                            List.of(1L, 2L),
                            List.of(" "),
                            List.of(new HistoryCreateRequest.Payload("url", null)));

            ResultActions perform =
                    mockMvc.perform(
                            post("/histories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result['hashtags[0]']").value("해시태그는 비워둘 수 없습니다."));
        }
    }

    @Nested
    class 기록_수정_요청_시 {

        @Test
        void 유효한_요청이면_기록을_수정한다() throws Exception {
            // given
            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            "updated content",
                            1L,
                            List.of(1L, 2L),
                            List.of("tag1", "tag2"),
                            List.of(
                                    new HistoryUpdateRequest.Payload(
                                            "image1",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            1L, 0.1, 0.2))),
                                    new HistoryUpdateRequest.Payload("image2", null)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/histories/1")
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
        void 수정_내용이_null_또는_공백이면_예외가_발생한다(String content) throws Exception {
            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            content,
                            1L,
                            List.of(1L, 2L),
                            List.of("tag1", "tag2"),
                            List.of(
                                    new HistoryUpdateRequest.Payload(
                                            "image1",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            1L, 0.1, 0.2)))));

            ResultActions perform =
                    mockMvc.perform(
                            patch("/histories/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("기록 내용은 비워둘 수 없습니다."));
        }

        @Test
        void 수정_이미지목록이_null이면_예외가_발생한다() throws Exception {
            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            "updated content", 1L, List.of(1L, 2L), List.of("tag1", "tag2"), null);

            ResultActions perform =
                    mockMvc.perform(
                            patch("/histories/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.payloads").value("기록 이미지 목록은 비워둘 수 없습니다."));
        }

        @Test
        void 수정_이미지가_0개면_예외가_발생한다() throws Exception {
            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            "updated content",
                            1L,
                            List.of(1L, 2L),
                            List.of("tag1", "tag2"),
                            List.of());

            ResultActions perform =
                    mockMvc.perform(
                            patch("/histories/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.payloads").value("이미지는 1~10개만 첨부 가능합니다."));
        }
    }

    @Nested
    class 전체_스타일_목록_요청_시 {

        @Test
        void 유효한_요청이면_전체_스타일_목록을_반환한다() throws Exception {
            // given
            StyleListResponse response =
                    new StyleListResponse(
                            List.of(
                                    new StyleListResponse.Content(1L, "testStyle1"),
                                    new StyleListResponse.Content(2L, "testStyle2"),
                                    new StyleListResponse.Content(3L, "testStyle3")));

            given(historyService.getAllStyles()).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/histories/styles").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.contents").isArray())
                    .andExpect(jsonPath("$.result.contents.length()").value(3))
                    .andExpect(jsonPath("$.result.contents[0].styleId").value(1L))
                    .andExpect(jsonPath("$.result.contents[0].name").value("testStyle1"))
                    .andExpect(jsonPath("$.result.contents[1].styleId").value(2L))
                    .andExpect(jsonPath("$.result.contents[1].name").value("testStyle2"))
                    .andExpect(jsonPath("$.result.contents[2].styleId").value(3L))
                    .andExpect(jsonPath("$.result.contents[2].name").value("testStyle3"));
        }
    }

    @Nested
    class 전체_상황_목록_요청_시 {

        @Test
        void 유효한_요청이면_전체_상황_목록을_반환한다() throws Exception {
            // given
            SituationListResponse response =
                    new SituationListResponse(
                            List.of(
                                    new SituationListResponse.Content(1L, "testSituation1"),
                                    new SituationListResponse.Content(2L, "testSituation2"),
                                    new SituationListResponse.Content(3L, "testSituation3")));

            given(historyService.getAllSituations()).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/histories/situations").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.contents").isArray())
                    .andExpect(jsonPath("$.result.contents.length()").value(3))
                    .andExpect(jsonPath("$.result.contents[0].situationId").value(1L))
                    .andExpect(jsonPath("$.result.contents[0].name").value("testSituation1"))
                    .andExpect(jsonPath("$.result.contents[1].situationId").value(2L))
                    .andExpect(jsonPath("$.result.contents[1].name").value("testSituation2"))
                    .andExpect(jsonPath("$.result.contents[2].situationId").value(3L))
                    .andExpect(jsonPath("$.result.contents[2].name").value("testSituation3"));
        }
    }
}
