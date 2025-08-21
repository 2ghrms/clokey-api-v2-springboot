package org.clokey.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.DuplicatedIdCheckResponse;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.Visibility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberUtil memberUtil;

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void updateProfile(ProfileUpdateRequest request) {

        final Member currentMember = memberUtil.getCurrentMember();

        validateVisualizeBannedMember(currentMember, request);

        // s3 삭제 로직 구현 이후에 반영 필요 -> 배경 및 프로필 이미지를 없애버리는 경우

        currentMember.updateProfile(
                request.nickname(),
                request.clokeyId(),
                request.profileImageUrl(),
                request.profileBackImageUrl(),
                request.bio(),
                request.visibility());
    }

    @Override
    public DuplicatedIdCheckResponse checkDuplicateClokeyId(DuplicatedIdCheckRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        boolean duplicated =
                !request.clokeyId().equals(currentMember.getClokeyId())
                        && memberRepository.existsByClokeyId(request.clokeyId());

        return DuplicatedIdCheckResponse.of(duplicated);
    }

    private void validateVisualizeBannedMember(Member member, ProfileUpdateRequest request) {
        boolean banned = member.getMemberStatus().equals(MemberStatus.BANNED);
        boolean changeToPublic = request.visibility().equals(Visibility.PUBLIC);
        if (banned && changeToPublic) {
            throw new BaseCustomException(MemberErrorCode.BANNED_MEMBER_TO_PUBLIC);
        }
    }
}
