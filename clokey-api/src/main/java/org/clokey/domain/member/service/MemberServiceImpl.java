package org.clokey.domain.member.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.BlockedMemberResponse;
import org.clokey.domain.member.dto.response.DuplicatedIdCheckResponse;
import org.clokey.domain.member.dto.response.MyselfCheckResponse;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.Visibility;
import org.clokey.response.SliceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberUtil memberUtil;

    private final MemberRepository memberRepository;
    private final BlockRepository blockRepository;

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

    @Override
    @Transactional
    public void toggleBlockStatus(Long memberId) {
        final Member blocker = memberUtil.getCurrentMember();
        final Member blocked = getMemberById(memberId);

        validateSelfBlock(blocker.getId(), blocked.getId());

        Optional<Block> existingBlock =
                blockRepository.findByBlockerIdAndBlockedId(blocker.getId(), blocked.getId());

        if (existingBlock.isPresent()) {
            blockRepository.delete(existingBlock.get());
        } else {
            Block block = Block.createBlock(blocker, blocked);
            blockRepository.save(block);
        }
    }

    @Override
    public MyselfCheckResponse checkIsMyself(String clokeyId) {
        validateExistsClokeyId(clokeyId);
        Member currentMember = memberUtil.getCurrentMember();

        boolean isMyself = currentMember.getClokeyId().equals(clokeyId);

        return MyselfCheckResponse.of(isMyself);
    }

    @Override
    public SliceResponse<BlockedMemberResponse> getBlockedMembers(
            Long lastBlockedId, Integer size, SortDirection direction) {
        Member currentMember = memberUtil.getCurrentMember();

        return SliceResponse.from(
                blockRepository.findAllByBlockerId(
                        currentMember.getId(), lastBlockedId, size, direction));
    }

    private void validateVisualizeBannedMember(Member member, ProfileUpdateRequest request) {
        boolean banned = member.getMemberStatus().equals(MemberStatus.BANNED);
        boolean changeToPublic = request.visibility().equals(Visibility.PUBLIC);
        if (banned && changeToPublic) {
            throw new BaseCustomException(MemberErrorCode.BANNED_MEMBER_TO_PUBLIC);
        }
    }

    private void validateSelfBlock(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BaseCustomException(MemberErrorCode.SELF_BLOCK_UNAVAILABLE);
        }
    }

    private void validateExistsClokeyId(String clokeyId) {
        if (!memberRepository.existsByClokeyId(clokeyId)) {
            throw new BaseCustomException(MemberErrorCode.CLOKEY_ID_NOT_FOUND);
        }
    }

    private Member getMemberById(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
