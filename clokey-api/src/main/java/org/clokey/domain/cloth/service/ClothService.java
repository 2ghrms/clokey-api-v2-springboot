package org.clokey.domain.cloth.service;

import java.util.List;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.request.ClothImagesUploadRequest;
import org.clokey.domain.cloth.dto.request.ClothUpdateRequest;
import org.clokey.domain.cloth.dto.response.*;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;

public interface ClothService {

    ClothImagesPresignedUrlResponse getClothUploadPresignedUrls(ClothImagesUploadRequest request);

    ClothCreateResponse createClothes(ClothCreateRequests list);

    SliceResponse<ClothRecommendListResponse> recommendCategoryClothes(
            Long lastClothId, int size, Long categoryId, Season season);

    SliceResponse<ClothListResponse> getClothes(
            Long lastClothId,
            int size,
            SortDirection direction,
            Long categoryId,
            List<Season> seasons);

    ClothDetailsResponse getClothDetails(Long clothId);

    void updateCloth(Long clothId, ClothUpdateRequest request);

    void deleteCloth(Long clothId);
}
