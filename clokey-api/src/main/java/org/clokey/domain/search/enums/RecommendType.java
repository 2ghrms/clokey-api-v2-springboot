package org.clokey.domain.search.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendType {
    UNTRIED_STYLE("분위기 전환이 필요할 때"),
    FREQUENTLY_WORN_CATEGORY("언제 입어도 찰떡인"),
    RECENTLY_USED_HASHTAG("최근 자주 입으시는");

    private final String title;
}
