package org.clokey.domain.search.dto.response;

public record SearchedMemberResponse(
        Long memberId, String profileImageUrl, String nickname, String clokeyId) {}
