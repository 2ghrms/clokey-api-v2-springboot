package org.clokey.domain.like.repository;

import org.clokey.domain.like.dto.response.LikedHistoriesResponse;
import org.clokey.domain.like.dto.response.LikedMembersResponse;
import org.springframework.data.domain.Slice;

public interface MemberLikeRepositoryCustom {

    Slice<LikedHistoriesResponse.LikedHistoryPreview> findLikedHistoriesSliceByMemberId(
            Long memberId, Long lastLikeId, Integer size);

    Slice<LikedMembersResponse.LikedMemberPreview> findLikedMembersSliceByHistoryId(
            Long historyId, Long lastLikeId, Integer size);
}
