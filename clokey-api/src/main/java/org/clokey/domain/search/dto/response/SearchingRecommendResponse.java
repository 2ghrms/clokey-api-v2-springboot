package org.clokey.domain.search.dto.response;

public record SearchingRecommendResponse(
        Long historyId, String recommendType, String title, String subTitle, String imageUrl) {}
