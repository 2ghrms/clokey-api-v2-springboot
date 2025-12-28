package org.clokey.domain.statistics.dto;

/** 1차 카테고리에 속한 옷들을 집계하기 위한 통계용 DTO입니다. */
public record CategoryCountDto(Long categoryId, String categoryName, Long count) {}
