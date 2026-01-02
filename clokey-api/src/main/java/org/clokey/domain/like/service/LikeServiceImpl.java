package org.clokey.domain.like.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.like.entity.MemberLike;
import org.clokey.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeServiceImpl implements LikeService {

    private final MemberUtil memberUtil;

    private final MemberLikeRepository memberLikeRepository;
    private final HistoryRepository historyRepository;
    private final BlockRepository blockRepository;

    @Override
    @Transactional
    public void toggleLike(Long historyId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history =
                historyRepository
                        .findByIdWithMember(historyId)
                        .orElseThrow(
                                () -> new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND));

        final Member historyOwner = history.getMember();

        if (isBlockedByOrBlocking(currentMember.getId(), historyOwner.getId())) {
            return;
        }

        Optional<MemberLike> existingLike =
                memberLikeRepository.findByMemberIdAndHistoryId(currentMember.getId(), historyId);

        if (existingLike.isPresent()) {
            memberLikeRepository.delete(existingLike.get());
        } else {
            MemberLike newLike = MemberLike.createMemberLike(currentMember, history);
            memberLikeRepository.save(newLike);
        }
    }

    private boolean isBlockedByOrBlocking(Long fromId, Long toId) {
        return blockRepository.existsByBlockerIdAndBlockedIdOrBlockerIdAndBlockedId(
                fromId, toId,
                toId, fromId);
    }
}
