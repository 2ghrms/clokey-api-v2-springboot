package org.clokey.domain.member.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.member.dto.request.DuplicatedNicknameCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.*;
import org.clokey.domain.member.event.NewFollowerEvent;
import org.clokey.domain.member.event.NewPendingFollowerEvent;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.member.repository.PendingFollowRepository;
import org.clokey.domain.search.event.MeiliSearchSyncEvent;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Follow;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.PendingFollow;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.Visibility;
import org.clokey.response.SliceResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberUtil memberUtil;
    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final PendingFollowRepository pendingFollowRepository;
    private final BlockRepository blockRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void updateProfile(ProfileUpdateRequest request) {

        final Member currentMember = memberUtil.getCurrentMember();

        validateVisualizeBannedMember(currentMember, request);

        // s3 삭제 로직 구현 이후에 반영 필요 -> 배경 및 프로필 이미지를 없애버리는 경우

        currentMember.updateProfile(
                request.nickname(), request.profileImageUrl(), request.bio(), request.visibility());

        // Member 동기화
        eventPublisher.publishEvent(
                MeiliSearchSyncEvent.of(
                        MeiliSearchSyncEvent.EntityType.MEMBER, currentMember.getId()));

        // Member의 모든 History 동기화
        List<Long> historyIds = historyRepository.findAllIdsByMemberId(currentMember.getId());
        for (Long historyId : historyIds) {
            eventPublisher.publishEvent(
                    MeiliSearchSyncEvent.of(MeiliSearchSyncEvent.EntityType.HISTORY, historyId));
        }
    }

    @Override
    public DuplicatedIdCheckResponse checkDuplicateNickname(
            DuplicatedNicknameCheckRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        boolean duplicated =
                !request.nickname().equals(currentMember.getNickname())
                        && memberRepository.existsByNickname(request.nickname());

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
    public MyselfCheckResponse checkIsMyself(String nickname) {
        validateExistsNickname(nickname);
        Member currentMember = memberUtil.getCurrentMember();

        boolean isMyself = currentMember.getNickname().equals(nickname);

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

    @Override
    public SliceResponse<FollowMemberResponse> getFollows(
            Long memberId, Long lastFollowId, boolean isFollowing, Integer size) {
        Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getMemberById(memberId);
        SliceResponse<FollowMemberResponse> response;

        if (!currentMember.equals(targetMember)) {
            validatePrivacy(currentMember, targetMember);

            validateBlocked(currentMember, targetMember);
        }

        if (isFollowing) {
            return SliceResponse.from(
                    followRepository.findAllFollowingsByMemberId(
                            currentMember.getId(), memberId, lastFollowId, size));
        } else {
            return SliceResponse.from(
                    followRepository.findAllFollowersByMemberId(
                            currentMember.getId(), memberId, lastFollowId, size));
        }
    }

    @Override
    public MemberInfoResponse getMemberInfo(Long memberId) {
        Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getMemberById(memberId);

        if (!currentMember.getId().equals(targetMember.getId())) {
            validateBlockedMutual(currentMember.getId(), targetMember.getId());
        }

        return memberRepository.findMemberInfoById(currentMember.getId(), memberId);
    }

    @Override
    public MyInfoResponse getMyInfo() {
        Member currentMember = memberUtil.getCurrentMember();
        return memberRepository.findMyInfoById(currentMember.getId());
    }

    private void validateVisualizeBannedMember(Member member, ProfileUpdateRequest request) {
        boolean banned = member.getMemberStatus().equals(MemberStatus.BANNED);
        boolean changeToPublic = request.visibility().equals(Visibility.PUBLIC);
        if (banned && changeToPublic) {
            throw new BaseCustomException(MemberErrorCode.BANNED_MEMBER_TO_PUBLIC);
        }
    }

    @Override
    @Transactional
    public void toggleFollow(Long userId) {
        final Member followFrom = memberUtil.getCurrentMember();
        final Member followTo = getMemberById(userId);

        validateFollowMyself(followFrom, followTo);
        validateNotBlocked(followFrom.getId(), followTo.getId());
        validatePrivate(followTo);

        Optional<Follow> existing =
                followRepository.findByFollowFrom_IdAndFollowTo_Id(
                        followFrom.getId(), followTo.getId());

        if (existing.isPresent()) {
            followRepository.delete(existing.get());
        } else {
            followRepository.save(Follow.createFollow(followFrom, followTo));
            eventPublisher.publishEvent(new NewFollowerEvent(followFrom.getId(), followTo.getId()));
        }
    }

    @Override
    @Transactional
    public void togglePendingFollow(Long userId) {
        final Member followFrom = memberUtil.getCurrentMember();
        final Member followTo = getMemberById(userId);

        validateFollowMyself(followFrom, followTo);
        validateNotBlocked(followFrom.getId(), followTo.getId());
        validatePublic(followTo);

        Optional<PendingFollow> pending =
                pendingFollowRepository.findByFollowFrom_IdAndFollowTo_Id(
                        followFrom.getId(), followTo.getId());

        if (pending.isPresent()) {
            pendingFollowRepository.delete(pending.get());
            return;
        }

        Optional<Follow> follow =
                followRepository.findByFollowFrom_IdAndFollowTo_Id(
                        followFrom.getId(), followTo.getId());

        if (follow.isPresent()) {
            followRepository.delete(follow.get());
            return;
        }

        PendingFollow newPending = PendingFollow.createPendingFollow(followFrom, followTo);
        pendingFollowRepository.save(newPending);
        eventPublisher.publishEvent(
                new NewPendingFollowerEvent(followFrom.getId(), followTo.getId()));
    }

    private void validateSelfBlock(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BaseCustomException(MemberErrorCode.SELF_BLOCK_UNAVAILABLE);
        }
    }

    private void validateExistsNickname(String nickname) {
        if (!memberRepository.existsByNickname(nickname)) {
            throw new BaseCustomException(MemberErrorCode.NICKNAME_NOT_FOUND);
        }
    }

    private void validatePrivacy(Member currentMember, Member targetMember) {
        if (!currentMember.getId().equals(targetMember.getId())
                && targetMember.getVisibility().equals(Visibility.PRIVATE)) {
            if (!followRepository.existsByFollowFrom_IdAndFollowTo_Id(
                    currentMember.getId(), targetMember.getId())) {
                throw new BaseCustomException(MemberErrorCode.PRIVATE_MEMBER_ACCESS_DENIED);
            }
        }
    }

    private void validateBlocked(Member currentMember, Member targetMember) {
        if (blockRepository.existsByBlockerIdAndBlockedId(
                targetMember.getId(), currentMember.getId())) {
            throw new BaseCustomException(MemberErrorCode.BLOCKED_MEMBER_ACCESS_DENIED);
        }
    }

    private void validateBlockedMutual(Long currentMemberId, Long targetMemberId) {
        if (blockRepository.existsByBlockerIdAndBlockedIdOrBlockerIdAndBlockedId(
                currentMemberId, targetMemberId, targetMemberId, currentMemberId)) {
            throw new BaseCustomException(MemberErrorCode.BLOCKED_MEMBER_ACCESS_DENIED);
        }
    }

    private void validateFollowMyself(Member followFrom, Member followTo) {
        if (followFrom.getId().equals(followTo.getId())) {
            throw new BaseCustomException(MemberErrorCode.CANNOT_FOLLOW_MYSELF);
        }
    }

    private void validateNotBlocked(Long fromId, Long toId) {
        if (blockRepository.existsByBlockerIdAndBlockedId(fromId, toId)
                || blockRepository.existsByBlockerIdAndBlockedId(toId, fromId)) {
            throw new BaseCustomException(MemberErrorCode.CANNOT_FOLLOW_BLOCKED);
        }
    }

    private void validatePrivate(Member member) {
        if (member.getVisibility().equals(Visibility.PRIVATE)) {
            throw new BaseCustomException(MemberErrorCode.MUST_REQUEST_FOLLOW);
        }
    }

    private void validatePublic(Member member) {
        if (member.getVisibility().equals(Visibility.PUBLIC)) {
            throw new BaseCustomException(MemberErrorCode.MUST_FOLLOW);
        }
    }

    private Member getMemberByNickname(String nickname) {
        return memberRepository
                .findByNickname(nickname)
                .orElseThrow(() -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Member getMemberById(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
