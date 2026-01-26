package org.clokey.domain.cloth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.cloth.dto.request.ClothDetectRequest;
import org.clokey.domain.cloth.dto.request.ClothImagesUploadRequest;
import org.clokey.domain.cloth.dto.request.ClothInfoExtractRequest;
import org.clokey.domain.cloth.dto.request.HistoryStyleInferenceRequest;
import org.clokey.domain.cloth.dto.response.ClothDetectResponse;
import org.clokey.domain.cloth.dto.response.ClothImagesPresignedUrlResponse;
import org.clokey.domain.cloth.dto.response.ClothInfoExtractResponse;
import org.clokey.domain.cloth.dto.response.HistoryStyleInferenceResponse;
import org.clokey.domain.cloth.service.ClothAiService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cloth-ai")
@RequiredArgsConstructor
@Tag(name = "17. 옷 AI API", description = "옷 AI 관련 API입니다.")
@Validated
public class ClothAiController {

    private final ClothAiService clothAiService;

    @PostMapping("/images")
    @Operation(
            operationId = "ClothAi_getClothUploadPresignedUrl",
            summary = "옷 이미지 업로드용 presignedUrl 발급",
            description = "옷 이미지 업로드용 presignedUrl을 발급합니다.")
    public BaseResponse<ClothImagesPresignedUrlResponse> getClothUploadPresignedUrl(
            @Valid @RequestBody ClothImagesUploadRequest request) {
        ClothImagesPresignedUrlResponse response =
                clothAiService.getClothUploadPresignedUrls(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/extract")
    @Operation(
            operationId = "ClothAi_extractClothInfo",
            summary = "옷 정보 추출",
            description = "옷 이미지 URL을 기반으로 AI 서버에서 옷 정보를 추출합니다.")
    public BaseResponse<ClothInfoExtractResponse> extractClothInfo(
            @Valid @RequestBody ClothInfoExtractRequest request) {
        ClothInfoExtractResponse response = clothAiService.extractClothInfo(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/history-style")
    @Operation(
            operationId = "ClothAi_inferHistoryStyle",
            summary = "기록 사진 스타일 추론",
            description = "기록 이미지 URL을 통해 스타일을 추론합니다.")
    public BaseResponse<HistoryStyleInferenceResponse> inferHistoryStyle(
            @Valid @RequestBody HistoryStyleInferenceRequest request) {
        HistoryStyleInferenceResponse response = clothAiService.inferHistoryStyle(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/detect")
    @Operation(
            operationId = "ClothAi_detectClothes",
            summary = "사진에서 옷 탐지",
            description = "사진에서 내부의 옷들을 탐지합니다.")
    public BaseResponse<ClothDetectResponse> detectClothes(
            @Valid @RequestBody ClothDetectRequest request) {
        ClothDetectResponse response = clothAiService.detectClothes(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }
}
