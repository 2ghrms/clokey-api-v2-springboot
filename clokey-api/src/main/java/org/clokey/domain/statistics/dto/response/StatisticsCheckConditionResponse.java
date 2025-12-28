package org.clokey.domain.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record StatisticsCheckConditionResponse(
        @Schema(description = "통계 집계 가능 여부", example = "true") boolean canAggregate) {
    public static StatisticsCheckConditionResponse of(boolean canAggregate) {
        return new StatisticsCheckConditionResponse(canAggregate);
    }
}
