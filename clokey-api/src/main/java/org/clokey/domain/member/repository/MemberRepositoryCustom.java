package org.clokey.domain.member.repository;

import org.clokey.domain.member.dto.response.MemberInfoResponse;

public interface MemberRepositoryCustom {

    public MemberInfoResponse findMemberInfoById(Long currentId, Long targetId);
}
