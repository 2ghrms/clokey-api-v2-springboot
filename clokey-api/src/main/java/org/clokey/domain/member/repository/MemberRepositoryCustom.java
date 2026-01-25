package org.clokey.domain.member.repository;

import org.clokey.domain.member.dto.response.MemberInfoResponse;
import org.clokey.domain.member.dto.response.MyInfoResponse;

public interface MemberRepositoryCustom {

    public MemberInfoResponse findMemberInfoById(Long currentId, Long targetId);

    public MyInfoResponse findMyInfoById(Long memberId);
}
