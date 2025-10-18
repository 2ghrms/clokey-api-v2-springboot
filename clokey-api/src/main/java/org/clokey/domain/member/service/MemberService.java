package org.clokey.domain.member.service;

import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.BlockedMemberResponse;
import org.clokey.domain.member.dto.response.DuplicatedIdCheckResponse;
import org.clokey.domain.member.dto.response.MyselfCheckResponse;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;

public interface MemberService {

    void updateProfile(ProfileUpdateRequest request);

    DuplicatedIdCheckResponse checkDuplicateClokeyId(DuplicatedIdCheckRequest request);

    void toggleFollow(Long userId);

    void togglePendingFollow(Long userId);

    void toggleBlockStatus(Long memberId);

    MyselfCheckResponse checkIsMyself(String clokeyId);

    SliceResponse<BlockedMemberResponse> getBlockedMembers(
            Long lastBlockedId, Integer size, SortDirection direction);
}
