package org.clokey.domain.cloth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.cloth.dto.request.ClothCreateRequest;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.response.ClothCreateResponse;
import org.clokey.domain.cloth.service.ClothService;
import org.clokey.exception.BaseCustomException;
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
    class 옷_생성_요청_시 {

        @Test
        void 유효한_요청이면_옷을_생성하고_ID를_반환한다() throws Exception {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest("testClothImageUrl1", 1L),
                                    new ClothCreateRequest("testClothImageUrl2", 1L)));

            ClothCreateResponse response = new ClothCreateResponse(List.of(1L, 2L));

            given(clothService.createCloths(request)).willReturn(response);

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
                    new ClothCreateRequests(List.of(new ClothCreateRequest(clothImageUrl, 1L)));

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
                            List.of(new ClothCreateRequest("testClothImageUrl", null)));

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
        void 카테고리가_존재하지_않을_경우_예외가_발생한다() throws Exception {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(new ClothCreateRequest("testClothImageUrl1", 999L)));
            given(clothService.createCloths(request))
                    .willThrow(
                            new BaseCustomException(CategoryErrorCode.CATEGORY_IN_BULK_NOT_FOUND));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/clothes")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("CATEGORY_4042"))
                    .andExpect(jsonPath("$.message").value("존재하지 않는 카테고리가 포함되어 있습니다."));
        }
    }
}
