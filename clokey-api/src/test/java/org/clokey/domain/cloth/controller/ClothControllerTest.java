package org.clokey.domain.cloth.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.cloth.dto.request.ClothCreateRequest;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.request.ClothImagesUploadRequest;
import org.clokey.domain.cloth.dto.request.ClothUpdateRequest;
import org.clokey.domain.cloth.dto.response.*;
import org.clokey.domain.cloth.service.ClothService;
import org.clokey.enums.FileExtension;
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

@WebMvcTest(ClothController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClothControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ClothService clothService;

    @Nested
    class 옷_업로드_presigned_url_발급_요청_시 {

        @Test
        void 유효한_요청이면_옷_이미지_업로드_Presigned_URL들을_반환한다() throws Exception {
            // given
            ClothImagesUploadRequest request =
                    new ClothImagesUploadRequest(
                            List.of(
                                    new ClothImagesUploadRequest.Payload(
                                            FileExtension.JPEG, "testMd5Hash1"),
                                    new ClothImagesUploadRequest.Payload(
                                            FileExtension.JPEG, "testMd5Hash2")));

            ClothImagesPresignedUrlResponse response =
                    new ClothImagesPresignedUrlResponse(
                            List.of("testPresignedUrl1", "testPresignedUrl2"));

            given(clothService.getClothUploadPresignedUrls(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/clothes/images")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.urls[0]").value("testPresignedUrl1"))
                    .andExpect(jsonPath("$.result.urls[1]").value("testPresignedUrl2"));
        }
    }

    @Nested
    class 옷_생성_요청_시 {

        @Test
        void 유효한_요청이면_옷을_생성하고_ID를_반환한다() throws Exception {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest(
                                            "testClothImageUrl1",
                                            null,
                                            null,
                                            null,
                                            List.of(Season.SPRING),
                                            1L),
                                    new ClothCreateRequest(
                                            "testClothImageUrl2",
                                            null,
                                            null,
                                            null,
                                            List.of(Season.SPRING, Season.FALL),
                                            1L)));

            ClothCreateResponse response = new ClothCreateResponse(List.of(1L, 2L));

            given(clothService.createClothes(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/clothes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.clothIds[0]").value(1))
                    .andExpect(jsonPath("$.result.clothIds[1]").value(2));
        }

        @Test
        void 빈_요청이면_예외가_발생한다() throws Exception {
            // given
            ClothCreateRequests request = new ClothCreateRequests(List.of());

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/clothes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("적어도 하나의 옷을 생성해야 합니다."));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 옷의_이미지_url이_null_또는_공백이면_예외가_발생한다(String clothImageUrl) throws Exception {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest(
                                            clothImageUrl,
                                            null,
                                            null,
                                            null,
                                            List.of(Season.SPRING),
                                            1L)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/clothes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.clothImageUrl").value("옷의 이미지 주소는 비워둘 수 없습니다."));
        }

        @Test
        void 옷의_카테고리_ID를_비워두면_예외가_발생한다() throws Exception {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest(
                                            "testClothImageUrl",
                                            null,
                                            null,
                                            null,
                                            List.of(Season.SPRING),
                                            null)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/clothes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.categoryId").value("옷의 카테고리 ID는 비워둘 수 없습니다."));
        }

        @Test
        void 옷의_계절을_비워두면_예외가_발생한다() throws Exception {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest(
                                            "testClothImageUrl", null, null, null, null, 1L)));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/clothes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.seasons").value("옷의 계절은 비워둘 수 없습니다."));
        }
    }

    @Nested
    class 카테고리별_계절_옷_추천_요청_시 {

        @Test
        void 유효한_요청이면_카테고리별로_적합한_계절_옷들을_반환한다() throws Exception {
            // given
            List<ClothRecommendListResponse> clothRecommendListResponses =
                    List.of(
                            new ClothRecommendListResponse(1L, "testImageUrl1"),
                            new ClothRecommendListResponse(2L, "testImageUrl1"));

            given(clothService.recommendCategoryClothes(null, 2, 1L, Season.SPRING))
                    .willReturn(new SliceResponse<>(clothRecommendListResponses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes/recommend")
                                    .param("size", "2")
                                    .param("categoryId", "1")
                                    .param("season", "SPRING"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].clothId").value(1))
                    .andExpect(jsonPath("$.result.content[1].clothId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_응답한다() throws Exception {
            // given
            List<ClothRecommendListResponse> clothRecommendListResponses =
                    List.of(
                            new ClothRecommendListResponse(1L, "testImageUrl1"),
                            new ClothRecommendListResponse(2L, "testImageUrl1"));

            given(clothService.recommendCategoryClothes(null, 2, 1L, Season.SPRING))
                    .willReturn(new SliceResponse<>(clothRecommendListResponses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes/recommend")
                                    .param("size", "2")
                                    .param("categoryId", "1")
                                    .param("season", "SPRING"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<ClothRecommendListResponse> clothRecommendListResponses =
                    List.of(
                            new ClothRecommendListResponse(1L, "testImageUrl1"),
                            new ClothRecommendListResponse(2L, "testImageUrl1"));

            given(clothService.recommendCategoryClothes(null, 2, 1L, Season.SPRING))
                    .willReturn(new SliceResponse<>(clothRecommendListResponses, false));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes/recommend")
                                    .param("size", "2")
                                    .param("categoryId", "1")
                                    .param("season", "SPRING"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @Test
        void 기록에_댓글이_없는_경우_빈_리스트를_응답한다() throws Exception {
            // given
            List<ClothRecommendListResponse> clothRecommendListResponses = List.of();

            given(clothService.recommendCategoryClothes(null, 2, 1L, Season.SPRING))
                    .willReturn(new SliceResponse<>(clothRecommendListResponses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes/recommend")
                                    .param("size", "2")
                                    .param("categoryId", "1")
                                    .param("season", "SPRING"));

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
                            get("/clothes/recommend")
                                    .param("size", pageSize)
                                    .param("categoryId", "1")
                                    .param("season", "SPRING"));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }

        @ParameterizedTest
        @ValueSource(strings = {"SPRINGG", "SUUUMMER", "FALL!", "WINTERS"})
        void 존재하지_않는_계절을_입력한_경우_예외가_발생한다(String season) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes/recommend")
                                    .param("size", "2")
                                    .param("categoryId", "1")
                                    .param("season", season));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }
    }

    @Nested
    class 옷_목록_조회_요청_시 {

        @Test
        void 유효한_요청이면_옷_목록을_반환한다() throws Exception {
            // given
            List<ClothListResponse> clothListResponses =
                    List.of(
                            new ClothListResponse(
                                    1L,
                                    "testImageUrl1",
                                    "testBrand1",
                                    "testName1",
                                    "testCategory1"),
                            new ClothListResponse(
                                    2L,
                                    "testImageUrl2",
                                    "testBrand2",
                                    "testName2",
                                    "testCategory2"));

            given(clothService.getClothes(null, 2, SortDirection.ASC, 1L, List.of(Season.SPRING)))
                    .willReturn(new SliceResponse<>(clothListResponses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes")
                                    .param("size", "2")
                                    .param("direction", "ASC")
                                    .param("categoryId", "1")
                                    .param("seasons", "SPRING"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].clothId").value(1))
                    .andExpect(jsonPath("$.result.content[1].clothId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-999", "0"})
        void 페이지_크기를_0_이하로_설정하면_예외가_발생한다(String pageSize) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes")
                                    .param("size", pageSize)
                                    .param("direction", "ASC")
                                    .param("categoryId", "1")
                                    .param("seasons", "SPRING"));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }

        @ParameterizedTest
        @ValueSource(strings = {"SPRINGG", "SUUUMMER", "FALL!", "WINTERS"})
        void 존재하지_않는_계절만_입력한_경우_예외가_발생한다(String season) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes")
                                    .param("size", "2")
                                    .param("direction", "ASC")
                                    .param("categoryId", "1")
                                    .param("seasons", season));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }

        @ParameterizedTest
        @ValueSource(strings = {"SPRINGG", "SUUUMMER", "FALL!", "WINTERS"})
        void 정상_계절과_잘못된_계절이_섞여_있는_경우_예외가_발생한다(String invalidSeason) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/clothes")
                                    .param("size", "2")
                                    .param("direction", "ASC")
                                    .param("categoryId", "1")
                                    .param("seasons", "SPRING", invalidSeason));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }
    }

    @Nested
    class 옷_상세_조회_요청_시 {

        @Test
        void 유효한_요청이면_옷_상세_정보를_반환한다() throws Exception {
            // given
            ClothDetailsResponse response =
                    new ClothDetailsResponse(
                            "testClothImageUrl",
                            "testParentCategory",
                            "testCategory",
                            "testName",
                            "testBrand",
                            "testClothUrl");

            given(clothService.getClothDetails(1L)).willReturn(response);

            // when & then
            ResultActions perform = mockMvc.perform(get("/clothes/1"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.clothImageUrl").value("testClothImageUrl"))
                    .andExpect(jsonPath("$.result.parentCategory").value("testParentCategory"))
                    .andExpect(jsonPath("$.result.category").value("testCategory"))
                    .andExpect(jsonPath("$.result.name").value("testName"))
                    .andExpect(jsonPath("$.result.brand").value("testBrand"))
                    .andExpect(jsonPath("$.result.clothUrl").value("testClothUrl"));
        }
    }

    @Nested
    class 옷_수정_요청_시 {

        @Test
        void 유효한_요청이면_옷을_수정하고_NO_CONTENT를_반환한다() throws Exception {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "testClothImageUrl",
                            "testClothUrl",
                            "testName",
                            "testBrand",
                            List.of(Season.SPRING),
                            1L);
            willDoNothing().given(clothService).updateCloth(1L, request);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/clothes/1")
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
        void 옷의_이미지_url이_null_또는_공백이면_예외가_발생한다(String clothImageUrl) throws Exception {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            clothImageUrl,
                            "testClothUrl",
                            "testName",
                            "testBrand",
                            List.of(Season.SPRING),
                            1L);
            ;

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/clothes/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.clothImageUrl").value("옷의 이미지 주소는 비워둘 수 없습니다."));
        }

        @Test
        void 옷의_계절을_비워둔_경우_예외가_발생한다() throws Exception {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "testClothImageURl", "testClothUrl", "testName", "testBrand", null, 1L);
            ;

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/clothes/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.seasons").value("옷의 계절은 비워둘 수 없습니다."));
        }

        @Test
        void 옷의_카테고리_ID를_비워둔_경우_예외가_발생한다() throws Exception {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "testClothImageURl",
                            "testClothUrl",
                            "testName",
                            "testBrand",
                            List.of(Season.SPRING),
                            null);
            ;

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            patch("/clothes/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.categoryId").value("옷의 카테고리 ID는 비워둘 수 없습니다."));
        }
    }

    @Nested
    class 옷_삭제_요청_시 {

        @Test
        void 유효한_요청이면_옷을_삭제한다() throws Exception {
            // given
            willDoNothing().given(clothService).deleteCloth(1L);

            // when & then
            ResultActions perform = mockMvc.perform(delete("/clothes/1"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }
}
