package org.clokey.domain.cloth.service;

import org.clokey.domain.cloth.dto.request.ClothDetectRequest;
import org.clokey.domain.cloth.dto.request.ClothImagesUploadRequest;
import org.clokey.domain.cloth.dto.request.ClothInfoExtractRequest;
import org.clokey.domain.cloth.dto.request.HistoryStyleInferenceRequest;
import org.clokey.domain.cloth.dto.response.ClothDetectResponse;
import org.clokey.domain.cloth.dto.response.ClothImagesPresignedUrlResponse;
import org.clokey.domain.cloth.dto.response.ClothInfoExtractResponse;
import org.clokey.domain.cloth.dto.response.HistoryStyleInferenceResponse;

public interface ClothAiService {

    ClothImagesPresignedUrlResponse getClothUploadPresignedUrls(ClothImagesUploadRequest request);

    ClothInfoExtractResponse extractClothInfo(ClothInfoExtractRequest request);

    HistoryStyleInferenceResponse inferHistoryStyle(HistoryStyleInferenceRequest request);

    ClothDetectResponse detectClothes(ClothDetectRequest request);
}
