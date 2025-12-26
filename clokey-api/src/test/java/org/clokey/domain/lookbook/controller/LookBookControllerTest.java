package org.clokey.domain.lookbook.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.lookbook.dto.request.LookBookCreateRequest;
import org.clokey.domain.lookbook.dto.request.LookBookUpdateRequest;
import org.clokey.domain.lookbook.dto.response.CoordinateListResponse;
import org.clokey.domain.lookbook.dto.response.LookBookCreateResponse;
import org.clokey.domain.lookbook.dto.response.LookBookListResponse;
import org.clokey.domain.lookbook.service.LookBookService;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(LookBookController.class)
@AutoConfigureMockMvc(addFilters = false)
class LookBookControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private LookBookService lookBookService;

    @Nested
    class 룩북_생성_요청_시 {

        @Test
        void 유효한_요청이면_룩북을_생성한다() throws Exception {
            // given
            LookBookCreateRequest request = new LookBookCreateRequest("testName");
            LookBookCreateResponse response = new LookBookCreateResponse(1L);
            given(lookBookService.createLookBook(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/lookbooks")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.lookBookId").value(1));
        }

        @Test
        void 룩북의_이름을_비워둔_경우_예외가_발생한다() throws Exception {
            // given
            LookBookCreateRequest request = new LookBookCreateRequest(null);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/lookbooks")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.name").value("룩북의 이름은 비워둘 수 없습니다."));
        }
    }

    @Nested
    class 룩북_수정_요청_시 {

        @Test
        void 유효한_요청이면_룩북을_수정한다() throws Exception {
            // given
            LookBookUpdateRequest request = new LookBookUpdateRequest("testName");
            willDoNothing().given(lookBookService).updateLookBook(1L, request);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/lookbooks/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }

        @Test
        void 룩북의_이름을_비워둔_경우_예외가_발생한다() throws Exception {
            // given
            LookBookUpdateRequest request = new LookBookUpdateRequest(null);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/lookbooks/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.name").value("룩북의 이름은 비워둘 수 없습니다."));
        }
    }

    @Nested
    class 룩북_삭제_요청_시 {

        @Test
        void 유효한_요청이면_룩북을_삭제한다() throws Exception {
            // given
            willDoNothing().given(lookBookService).deleteLookBook(1L);

            // when & then
            ResultActions perform =
                    mockMvc.perform(delete("/lookbooks/1").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 룩북_전체_조회_요청_시 {

        @Test
        void 정렬_조건이_ASC이면_lookBookId를_오름차순으로_응답한다() throws Exception {
            // given
            List<LookBookListResponse> responses =
                    List.of(
                            new LookBookListResponse(1L, "testName", "testImageUrl"),
                            new LookBookListResponse(2L, "testName", "testImageUrl"),
                            new LookBookListResponse(3L, "testName", "testImageUrl"));

            given(lookBookService.getLookBooks(null, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(responses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(get("/lookbooks").param("size", "3").param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].lookBookId").value(1))
                    .andExpect(jsonPath("$.result.content[1].lookBookId").value(2))
                    .andExpect(jsonPath("$.result.content[2].lookBookId").value(3))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 정렬_조건이_DESC이면_lookBookId를_내림차순으로_응답한다() throws Exception {
            // given
            List<LookBookListResponse> responses =
                    List.of(
                            new LookBookListResponse(3L, "testName", "testImageUrl"),
                            new LookBookListResponse(2L, "testName", "testImageUrl"),
                            new LookBookListResponse(1L, "testName", "testImageUrl"));

            given(lookBookService.getLookBooks(null, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(responses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(get("/lookbooks").param("size", "3").param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].lookBookId").value(3))
                    .andExpect(jsonPath("$.result.content[1].lookBookId").value(2))
                    .andExpect(jsonPath("$.result.content[2].lookBookId").value(1))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_응답한다() throws Exception {
            // given
            List<LookBookListResponse> responses =
                    List.of(
                            new LookBookListResponse(1L, "testName", "testImageUrl"),
                            new LookBookListResponse(2L, "testName", "testImageUrl"),
                            new LookBookListResponse(3L, "testName", "testImageUrl"));

            given(lookBookService.getLookBooks(null, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(responses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(get("/lookbooks").param("size", "3").param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<LookBookListResponse> responses =
                    List.of(
                            new LookBookListResponse(1L, "testName", "testImageUrl"),
                            new LookBookListResponse(2L, "testName", "testImageUrl"),
                            new LookBookListResponse(3L, "testName", "testImageUrl"));

            given(lookBookService.getLookBooks(null, 4, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(responses, false));

            // when & then
            ResultActions perform =
                    mockMvc.perform(get("/lookbooks").param("size", "4").param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @Test
        void 룩북이_없는_경우_빈_리스트를_응답한다() throws Exception {
            // given
            List<LookBookListResponse> responses = List.of();

            given(lookBookService.getLookBooks(null, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(responses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(get("/lookbooks").param("size", "3").param("direction", "ASC"));

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
                            get("/lookbooks").param("size", pageSize).param("direction", "ASC"));

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
                    mockMvc.perform(get("/lookbooks").param("size", "1").param("direction", sort));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }
    }

    @Nested
    class 개별_룩북_코디_목록_조회_요청_시 {

        @Test
        void 정렬_조건이_ASC이면_coordinateId를_오름차순으로_응답한다() throws Exception {
            // given
            List<CoordinateListResponse> responses =
                    List.of(
                            new CoordinateListResponse(1L, "testName", true, "testImageUrl"),
                            new CoordinateListResponse(2L, "testName", true, "testImageUrl"),
                            new CoordinateListResponse(3L, "testName", true, "testImageUrl"));

            given(lookBookService.getCoordinates(1L, null, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(responses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/lookbooks/1").param("size", "3").param("direction", "ASC"));

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
            List<CoordinateListResponse> responses =
                    List.of(
                            new CoordinateListResponse(3L, "testName", true, "testImageUrl"),
                            new CoordinateListResponse(2L, "testName", true, "testImageUrl"),
                            new CoordinateListResponse(1L, "testName", true, "testImageUrl"));

            given(lookBookService.getCoordinates(1L, null, 3, SortDirection.DESC))
                    .willReturn(new SliceResponse<>(responses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/lookbooks/1").param("size", "3").param("direction", "DESC"));

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
            List<CoordinateListResponse> responses =
                    List.of(
                            new CoordinateListResponse(3L, "testName", true, "testImageUrl"),
                            new CoordinateListResponse(2L, "testName", true, "testImageUrl"),
                            new CoordinateListResponse(1L, "testName", true, "testImageUrl"));

            given(lookBookService.getCoordinates(1L, null, 3, SortDirection.DESC))
                    .willReturn(new SliceResponse<>(responses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/lookbooks/1").param("size", "3").param("direction", "DESC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<CoordinateListResponse> responses =
                    List.of(
                            new CoordinateListResponse(3L, "testName", true, "testImageUrl"),
                            new CoordinateListResponse(2L, "testName", true, "testImageUrl"),
                            new CoordinateListResponse(1L, "testName", true, "testImageUrl"));

            given(lookBookService.getCoordinates(1L, null, 3, SortDirection.DESC))
                    .willReturn(new SliceResponse<>(responses, false));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/lookbooks/1").param("size", "3").param("direction", "DESC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @Test
        void 룩북에_코디가_없는_경우_빈_리스트를_응답한다() throws Exception {
            // given
            List<CoordinateListResponse> responses = List.of();

            given(lookBookService.getCoordinates(1L, null, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(responses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/lookbooks/1").param("size", "3").param("direction", "ASC"));

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
                            get("/lookbooks/1").param("size", pageSize).param("direction", "ASC"));

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
                            get("/lookbooks/1").param("size", "1").param("direction", sort));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }
    }
}
