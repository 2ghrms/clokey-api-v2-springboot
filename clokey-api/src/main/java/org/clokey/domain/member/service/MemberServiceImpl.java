package org.clokey.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
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
        // 사용자 확인
        Long memberId = memberUtil.getCurrentMember().getId();
        final Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(
                                () -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 사용자 상태 체크 및 유효성 검증
        validateVisualizeBannedMember(member, request);

        String profileImageUrl;
        if (request.profileImageUrl() == null || request.profileImageUrl().isBlank()) {
            profileImageUrl = null;
            // 추후 S3에서 삭제
        } else {
            profileImageUrl = request.profileImageUrl();
        }

        String profileBackImageUrl;
        if (request.profileBackImageUrl() == null || request.profileBackImageUrl().isBlank()) {
            profileBackImageUrl = null;
            // 추후 S3에서 삭제
        } else {
            profileBackImageUrl = request.profileBackImageUrl();
        }

        // 프로필 업데이트
        member.updateProfile(
                request.nickname(),
                request.clokeyId(),
                profileImageUrl,
                profileBackImageUrl,
                request.bio(),
                request.visibility());

        // Elasticsearch 동기화였던 부분 삭제

    }

    private void validateVisualizeBannedMember(Member member, ProfileUpdateRequest request) {
        boolean banned = member.getMemberStatus().equals(MemberStatus.BANNED);
        boolean changeToPublic = request.visibility().equals(Visibility.PUBLIC);
        if (banned && changeToPublic) {
            throw new BaseCustomException(MemberErrorCode.BANNED_MEMBER_TO_PUBLIC);
        }
    }
}
