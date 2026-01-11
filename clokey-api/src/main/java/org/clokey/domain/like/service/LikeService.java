package org.clokey.domain.like.service;

import org.clokey.domain.like.dto.response.LikedHistoriesResponse;
import org.clokey.domain.like.dto.response.LikedMembersResponse;
import org.clokey.response.SliceResponse;

public interface LikeService {
    SliceResponse<LikedMembersResponse.LikedMemberPreview> getLikedMembers(
            Long historyId, Long lastLikedId, Integer size);

    SliceResponse<LikedHistoriesResponse.LikedHistoryPreview> getLikedHistories(
            Long lastLikedId, Integer size);

    void toggleLike(Long historyId);
}
