package org.clokey.domain.member.service;

import org.clokey.domain.member.dto.request.DuplicatedNicknameCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.*;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;

public interface MemberService {

    void updateProfile(ProfileUpdateRequest request);

    DuplicatedIdCheckResponse checkDuplicateNickname(DuplicatedNicknameCheckRequest request);

    void toggleFollow(Long userId);

    void togglePendingFollow(Long userId);

    void toggleBlockStatus(Long memberId);

    MyselfCheckResponse checkIsMyself(String nickname);

    SliceResponse<BlockedMemberResponse> getBlockedMembers(
            Long lastBlockedId, Integer size, SortDirection direction);

    SliceResponse<FollowMemberResponse> getFollows(
            Long memberId, Long lastFollowId, boolean isFollowing, Integer size);

    MemberInfoResponse getMemberInfo(Long memberId);

    MyInfoResponse getMyInfo();
}
