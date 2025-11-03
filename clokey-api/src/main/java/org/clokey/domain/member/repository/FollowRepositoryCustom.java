package org.clokey.domain.member.repository;

import org.clokey.domain.member.dto.response.FollowMemberResponse;
import org.springframework.data.domain.Slice;

public interface FollowRepositoryCustom {
    Slice<FollowMemberResponse> findAllFollowingsByMemberId(
            Long currentId, Long targetId, Long lastFollowId, Integer size);

    Slice<FollowMemberResponse> findAllFollowersByMemberId(
            Long currentId, Long targetId, Long lastFollowId, Integer size);
}
