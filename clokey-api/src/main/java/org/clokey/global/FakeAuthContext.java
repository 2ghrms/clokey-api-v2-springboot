package org.clokey.global;

import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.OauthProvider;
import org.clokey.member.enums.RegisterStatus;
import org.clokey.member.enums.Visibility;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FakeAuthContext {

    private final MemberRepository memberRepository;

    public Member getCurrentMember() {
        Member member =
                Member.createMember(
                        "tempEmail",
                        "tempClokeyId",
                        "tempNickname",
                        OauthInfo.createOauthInfo("tempOid", OauthProvider.KAKAO),
                        MemberStatus.ACTIVE,
                        RegisterStatus.REGISTERED,
                        Visibility.PUBLIC);

        memberRepository.save(member);
        return member;
    }
}
