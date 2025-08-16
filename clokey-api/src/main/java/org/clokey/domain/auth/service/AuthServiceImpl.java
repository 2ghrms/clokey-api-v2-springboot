package org.clokey.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.enums.RegisterStatus;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberUtil memberUtil;

    private final MemberTermRepository memberTermRepository;

    @Override
    public UserStatusResponse getUserStatus() {
        final Member currentMember = memberUtil.getCurrentMember();

        if (memberTermRepository.existsByMemberId(currentMember.getId())) {
            return UserStatusResponse.of(RegisterStatus.REGISTERED);
        }
        return UserStatusResponse.of(RegisterStatus.NOT_AGREED);
    }
}
