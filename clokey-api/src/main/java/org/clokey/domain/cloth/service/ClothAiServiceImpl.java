package org.clokey.domain.cloth.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.category.entity.Category;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.dto.request.ClothDetectAiRequestDTO;
import org.clokey.domain.cloth.dto.request.ClothDetectRequest;
import org.clokey.domain.cloth.dto.request.ClothImagesUploadRequest;
import org.clokey.domain.cloth.dto.request.ClothInfoExtractAiRequestDTO;
import org.clokey.domain.cloth.dto.request.ClothInfoExtractRequest;
import org.clokey.domain.cloth.dto.request.HistoryStyleInferenceAiRequestDTO;
import org.clokey.domain.cloth.dto.request.HistoryStyleInferenceRequest;
import org.clokey.domain.cloth.dto.response.ClothDetectAiResponseDTO;
import org.clokey.domain.cloth.dto.response.ClothDetectResponse;
import org.clokey.domain.cloth.dto.response.ClothImagesPresignedUrlResponse;
import org.clokey.domain.cloth.dto.response.ClothInfoExtractAiResponseDTO;
import org.clokey.domain.cloth.dto.response.ClothInfoExtractResponse;
import org.clokey.domain.cloth.dto.response.HistoryStyleInferenceAiResponseDTO;
import org.clokey.domain.cloth.dto.response.HistoryStyleInferenceResponse;
import org.clokey.domain.cloth.exception.ClothAiErrorCode;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.exception.SituationErrorCode;
import org.clokey.domain.history.exception.StyleErrorCode;
import org.clokey.enums.FileExtension;
import org.clokey.enums.ImageType;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.properties.WebClientProperties;
import org.clokey.util.StorageUtil;
import org.clokey.util.WebClientUtil;
import org.springframework.stereotype.Service;

// FIXME: 외부 API와 연동되는 부분으로 절대로 Transaction을 붙여서 DB Connection pool을 낭비하지 말 것 ! (현재는 필요한 부분에서
// Transaction Util을 사용하세요)
// FIXME: 현재는 Tomcat Thread Pool을 점유하고 있는 비효율적인 구조이기 때문에 나중에 비동기 처리를 통해 트래픽이 생길 경우 최적화가 필요합니다.
@Service
@RequiredArgsConstructor
public class ClothAiServiceImpl implements ClothAiService {

    private final MemberUtil memberUtil;
    private final CategoryRepository categoryRepository;
    private final StorageUtil storageUtil;
    private final WebClientUtil webClientUtil;
    private final WebClientProperties webClientProperties;

    @Override
    public ClothImagesPresignedUrlResponse getClothUploadPresignedUrls(
            ClothImagesUploadRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        // 중요 :  md5 해시로 변조 확인을 하기 때문에 들어온 순서대로 반환해야함!!
        List<String> presignedUrls =
                request.payloads().stream()
                        .map(
                                req ->
                                        storageUtil.createPresignedUrl(
                                                ImageType.CLOTH_IMAGE,
                                                currentMember.getId(),
                                                req.fileExtension(),
                                                req.md5Hashes()))
                        .toList();

        return ClothImagesPresignedUrlResponse.of(presignedUrls);
    }

    @Override
    public ClothInfoExtractResponse extractClothInfo(ClothInfoExtractRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final List<String> clothImageUrls = request.clothImageUrls();

        validateImageUrls(clothImageUrls);

        // AI Server에게 N개의 사진을 전처리한 후 업로드할 수 있는 presignedUrl을 넘겨줍니다.
        List<String> presignedUrls =
                createPresignedUrls(currentMember.getId(), clothImageUrls.size());

        ClothInfoExtractAiResponseDTO aiResponse;
        try {
            aiResponse =
                    webClientUtil
                            .postToAiServer(
                                    webClientProperties.clothInferencePath(),
                                    new ClothInfoExtractAiRequestDTO(clothImageUrls, presignedUrls),
                                    ClothInfoExtractAiResponseDTO.class)
                            .block();
        } catch (Exception e) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_REQUEST_FAILED);
        }

        if (aiResponse == null) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_REQUEST_FAILED);
        }

        if (!Boolean.TRUE.equals(aiResponse.isSuccess())) {
            throw new BaseCustomException(mapAiErrorCode(aiResponse.errorCode()));
        }

        if (aiResponse.result() == null || aiResponse.result().isEmpty()) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_INVALID_RESPONSE);
        }

        if (aiResponse.result().size() != clothImageUrls.size()) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_RESULT_MISMATCH);
        }

        List<ClothInfoExtractAiResponseDTO.ResultItem> resultItems = aiResponse.result();
        List<ClothInfoExtractResponse.Payload> payloads =
                new java.util.ArrayList<>(resultItems.size());

        Set<Long> categoryIds =
                resultItems.stream()
                        .map(ClothInfoExtractAiResponseDTO.ResultItem::categories)
                        .filter(categories -> categories != null && !categories.isEmpty())
                        .map(categories -> categories.get(0).id())
                        .collect(Collectors.toSet());

        Map<Long, Category> categoryMap =
                categoryRepository.findAllByIdWithParent(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));

        for (int i = 0; i < resultItems.size(); i++) {
            ClothInfoExtractAiResponseDTO.ResultItem resultItem = resultItems.get(i);
            String clothImageUrl = resultItem.uploadedUrl();

            List<ClothInfoExtractAiResponseDTO.CategoryItem> categories = resultItem.categories();
            if (categories == null || categories.isEmpty()) {
                throw new BaseCustomException(ClothErrorCode.ClOTH_NOT_FOUND);
            }
            ClothInfoExtractAiResponseDTO.CategoryItem categoryItem = categories.get(0);
            Category category = categoryMap.get(categoryItem.id());
            if (category == null) {
                throw new BaseCustomException(CategoryErrorCode.CATEGORY_NOT_FOUND);
            }
            Category parentCategory = category.getParent();

            List<ClothInfoExtractAiResponseDTO.SeasonItem> seasonItems = resultItem.seasons();
            if (seasonItems == null || seasonItems.isEmpty()) {
                throw new BaseCustomException(ClothErrorCode.ClOTH_NOT_FOUND);
            }

            List<Season> seasons = new java.util.ArrayList<>(seasonItems.size());
            for (ClothInfoExtractAiResponseDTO.SeasonItem seasonItem : seasonItems) {
                seasons.add(convertSeasonNameToEnum(seasonItem.name()));
            }

            payloads.add(
                    new ClothInfoExtractResponse.Payload(
                            clothImageUrl,
                            seasons,
                            parentCategory != null ? parentCategory.getId() : null,
                            parentCategory != null ? parentCategory.getName() : null,
                            category.getId(),
                            category.getName()));
        }

        return ClothInfoExtractResponse.of(payloads);
    }

    private Season convertSeasonNameToEnum(String seasonName) {
        return switch (seasonName) {
            case "봄" -> Season.SPRING;
            case "여름" -> Season.SUMMER;
            case "가을" -> Season.FALL;
            case "겨울" -> Season.WINTER;
            default -> throw new BaseCustomException(ClothErrorCode.ClOTH_NOT_FOUND);
        };
    }

    @Override
    public HistoryStyleInferenceResponse inferHistoryStyle(HistoryStyleInferenceRequest request) {
        final String historyImageUrl = request.historyImageUrl();

        validateImageUrl(historyImageUrl);

        HistoryStyleInferenceAiResponseDTO aiResponse;
        try {
            aiResponse =
                    webClientUtil
                            .postToAiServer(
                                    webClientProperties.styleInferencePath(),
                                    new HistoryStyleInferenceAiRequestDTO(historyImageUrl),
                                    HistoryStyleInferenceAiResponseDTO.class)
                            .block();
        } catch (Exception e) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_REQUEST_FAILED);
        }

        if (aiResponse.result() == null) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_INVALID_RESPONSE);
        }

        HistoryStyleInferenceAiResponseDTO.Result result = aiResponse.result();

        if (result.situations() == null || result.situations().isEmpty()) {
            throw new BaseCustomException(SituationErrorCode.SITUATION_NOT_FOUND);
        }
        HistoryStyleInferenceAiResponseDTO.SituationItem situationItem = result.situations().get(0);

        if (result.styles() == null || result.styles().isEmpty()) {
            throw new BaseCustomException(StyleErrorCode.STYLE_NOT_FOUND);
        }

        List<HistoryStyleInferenceResponse.StylePayload> styles =
                result.styles().stream()
                        .map(
                                style ->
                                        new HistoryStyleInferenceResponse.StylePayload(
                                                style.id(), style.name()))
                        .toList();

        return HistoryStyleInferenceResponse.of(situationItem.id(), situationItem.name(), styles);
    }

    @Override
    public ClothDetectResponse detectClothes(ClothDetectRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final String imageUrl = request.imageUrl();

        validateImageUrl(imageUrl);

        List<String> presignedUrls = createPresignedUrls(currentMember.getId(), 10);

        ClothDetectAiResponseDTO aiResponse;
        try {
            aiResponse =
                    webClientUtil
                            .postToAiServer(
                                    webClientProperties.clothDetectPath(),
                                    new ClothDetectAiRequestDTO(imageUrl, presignedUrls),
                                    ClothDetectAiResponseDTO.class)
                            .block();
        } catch (Exception e) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_REQUEST_FAILED);
        }

        if (aiResponse == null) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_REQUEST_FAILED);
        }

        if (!Boolean.TRUE.equals(aiResponse.isSuccess())) {
            throw new BaseCustomException(mapAiErrorCode(aiResponse.errorCode()));
        }

        if (aiResponse.result() == null || aiResponse.result().uploadedUrls() == null) {
            throw new BaseCustomException(ClothAiErrorCode.AI_SERVER_INVALID_RESPONSE);
        }

        List<ClothDetectResponse.Payload> payloads =
                aiResponse.result().uploadedUrls().stream()
                        .map(ClothDetectResponse.Payload::new)
                        .toList();

        return ClothDetectResponse.of(payloads);
    }

    private void validateImageUrls(List<String> imageUrls) {
        if (!storageUtil.doAllFilesExistByUrls(imageUrls)) {
            throw new BaseCustomException(ClothErrorCode.ClOTH_NOT_FOUND);
        }
    }

    private void validateImageUrl(String imageUrl) {
        if (!storageUtil.doesFileExistByUrl(imageUrl)) {
            throw new BaseCustomException(HistoryErrorCode.HISTORY_IMAGE_NOT_FOUND);
        }
    }

    private ClothAiErrorCode mapAiErrorCode(String aiErrorCode) {
        if (aiErrorCode == null || aiErrorCode.isBlank()) {
            return ClothAiErrorCode.AI_SERVER_INVALID_RESPONSE;
        }

        return switch (aiErrorCode) {
            case "S3_DOWNLOAD_FAILED" -> ClothAiErrorCode.AI_S3_DOWNLOAD_FAILED;
            case "S3_UPLOAD_FAILED" -> ClothAiErrorCode.AI_S3_UPLOAD_FAILED;
            case "INVAILED_METHOD", "INVALID_METHOD" -> ClothAiErrorCode.AI_INVALID_METHOD;
            case "UNEXPECTED_EXCEPTION" -> ClothAiErrorCode.AI_UNEXPECTED_EXCEPTION;
            case "DETECT_EMPTY" -> ClothAiErrorCode.AI_DETECT_EMPTY;
            case "CROP_EMPTY" -> ClothAiErrorCode.AI_CROP_EMPTY;
            default -> ClothAiErrorCode.AI_SERVER_INVALID_RESPONSE;
        };
    }

    // TODO : 현재 AI 서버와 비동기 처리와 더불어 양방향 통신을 고려하지 않고 있습니다. 따라서, MD5 해시를 통한 무결성 검증이 불가능하며, JPEG로 고정할
    // 것을 요청해야합니다.
    private List<String> createPresignedUrls(Long memberId, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(
                        i ->
                                storageUtil.createPresignedUrlWithoutMd5(
                                        ImageType.CLOTH_IMAGE, memberId, FileExtension.JPEG))
                .toList();
    }
}
