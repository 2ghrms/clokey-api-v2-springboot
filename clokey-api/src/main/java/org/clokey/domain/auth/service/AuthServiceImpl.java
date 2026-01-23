package org.clokey.domain.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.entity.Cloth;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.domain.auth.dto.AccessTokenDto;
import org.clokey.domain.auth.dto.RefreshTokenDto;
import org.clokey.domain.auth.dto.request.DeviceTokenRenewRequest;
import org.clokey.domain.auth.dto.request.TokenReissueRequest;
import org.clokey.domain.auth.dto.request.UserStatusUpdateRequest;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.enums.RegisterStatus;
import org.clokey.domain.auth.exception.AuthErrorCode;
import org.clokey.domain.auth.repository.RefreshTokenRepository;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.history.repository.HistoryClothTagRepository;
import org.clokey.domain.history.repository.HistoryHashtagRepository;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.HistoryStyleRepository;
import org.clokey.domain.image.event.ImagesDeleteEvent;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.lookbook.repository.LookBookRepository;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.member.repository.PendingFollowRepository;
import org.clokey.domain.notification.repository.CodiveNotificationRepository;
import org.clokey.domain.report.repository.ReportRepository;
import org.clokey.domain.search.event.MemberDeleteEvent;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.history.entity.HistoryImage;
import org.clokey.member.entity.Member;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberUtil memberUtil;

    private final MemberTermRepository memberTermRepository;
    private final MemberRepository memberRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 회원 탈퇴 관련 Repository
    private final HistoryRepository historyRepository;
    private final HistoryImageRepository historyImageRepository;
    private final HistoryClothTagRepository historyClothTagRepository;
    private final HistoryStyleRepository historyStyleRepository;
    private final HistoryHashtagRepository historyHashtagRepository;
    private final MemberLikeRepository memberLikeRepository;
    private final CommentRepository commentRepository;
    private final CoordinateRepository coordinateRepository;
    private final CoordinateClothRepository coordinateClothRepository;
    private final LookBookRepository lookBookRepository;
    private final ClothRepository clothRepository;
    private final CodiveNotificationRepository codiveNotificationRepository;
    private final FollowRepository followRepository;
    private final PendingFollowRepository pendingFollowRepository;
    private final BlockRepository blockRepository;
    private final ReportRepository reportRepository;

    @Override
    public UserStatusResponse getUserStatus() {
        final Member currentMember = memberUtil.getCurrentMember();

        if (memberTermRepository.existsByMemberId(currentMember.getId())) {
            return UserStatusResponse.of(RegisterStatus.REGISTERED);
        }
        return UserStatusResponse.of(RegisterStatus.NOT_AGREED);
    }

    @Override
    @Transactional
    public void renewDeviceToken(DeviceTokenRenewRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        currentMember.updateDeviceToken(request.deviceToken());
    }

    @Override
    @Transactional
    public TokenResponse reissueTokens(TokenReissueRequest request) {
        RefreshTokenDto oldRefreshToken =
                jwtTokenService.retrieveRefreshToken(request.refreshToken());

        if (oldRefreshToken == null) {
            throw new BaseCustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshTokenDto newRefreshTokenDto = jwtTokenService.reissueRefreshToken(oldRefreshToken);
        AccessTokenDto newAccessTokenDto =
                jwtTokenService.reissueAccessToken(getMember(newRefreshTokenDto));

        return TokenResponse.of(newAccessTokenDto.tokenValue(), newRefreshTokenDto.tokenValue());
    }

    @Override
    @Transactional
    public void logoutUser() {
        final Member currentMember = memberUtil.getCurrentMember();

        refreshTokenRepository
                .findById(currentMember.getId())
                .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    @Transactional
    public void updateUserStatus(UserStatusUpdateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        if (request.active()) {
            currentMember.activate();
        } else {
            currentMember.deactivate();
        }
    }

    @Override
    @Transactional
    public void withdrawMember() {
        final Member currentMember = memberUtil.getCurrentMember();
        withdrawMemberById(currentMember.getId());
    }

    // TODO: (중요 메모) 절대로 - 카테고리, 상황, 약관, 스타일은 삭제하면 안됩니다!! 미리 적재된 데이터입니다.
    @Override
    @Transactional
    public void withdrawMemberById(Long memberId) {
        final Member currentMember =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(
                                () -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<String> imageUrlsToDelete = new ArrayList<>();

        // 1. History 관련 데이터 삭제
        List<History> histories =
                historyRepository.findAll().stream()
                        .filter(h -> h.getMember().getId().equals(memberId))
                        .collect(Collectors.toList());

        List<Long> historyIds = histories.stream().map(History::getId).collect(Collectors.toList());

        if (!historyIds.isEmpty()) {
            // HistoryClothTag 삭제
            List<Long> historyImageIds =
                    historyImageRepository.findAll().stream()
                            .filter(hi -> historyIds.contains(hi.getHistory().getId()))
                            .map(HistoryImage::getId)
                            .collect(Collectors.toList());

            historyImageIds.forEach(
                    historyImageId ->
                            historyClothTagRepository
                                    .findByHistoryImageId(historyImageId)
                                    .forEach(historyClothTagRepository::delete));

            // HistoryImage 삭제 및 이미지 URL 수집
            List<HistoryImage> historyImages =
                    historyImageRepository.findAll().stream()
                            .filter(hi -> historyIds.contains(hi.getHistory().getId()))
                            .collect(Collectors.toList());

            historyImages.forEach(
                    hi -> {
                        if (hi.getImageUrl() != null) {
                            imageUrlsToDelete.add(hi.getImageUrl());
                        }
                    });
            historyImageRepository.deleteAll(historyImages);

            // HistoryHashtag 삭제
            historyIds.forEach(
                    historyId ->
                            historyHashtagRepository
                                    .findByHistoryId(historyId)
                                    .forEach(historyHashtagRepository::delete));

            // HistoryStyle 삭제
            historyIds.forEach(
                    historyId ->
                            historyStyleRepository
                                    .findByHistoryId(historyId)
                                    .forEach(historyStyleRepository::delete));

            // MemberLike 삭제 (해당 회원이 좋아요한 것)
            memberLikeRepository.findAll().stream()
                    .filter(ml -> ml.getMember().getId().equals(memberId))
                    .forEach(memberLikeRepository::delete);

            // Comment 삭제
            commentRepository.findAll().stream()
                    .filter(c -> c.getMember().getId().equals(memberId))
                    .forEach(commentRepository::delete);

            // History 삭제
            historyRepository.deleteAll(histories);
        }

        // 2. Coordinate 관련 데이터 삭제
        List<Coordinate> coordinates =
                coordinateRepository.findAll().stream()
                        .filter(c -> c.getMember().getId().equals(memberId))
                        .collect(Collectors.toList());

        List<Long> coordinateIds =
                coordinates.stream().map(Coordinate::getId).collect(Collectors.toList());

        if (!coordinateIds.isEmpty()) {
            // CoordinateCloth 삭제
            coordinateIds.forEach(coordinateClothRepository::deleteAllByCoordinateId);

            // Coordinate 이미지 URL 수집 및 삭제
            coordinates.forEach(
                    c -> {
                        if (c.getImageUrl() != null) {
                            imageUrlsToDelete.add(c.getImageUrl());
                        }
                    });
            coordinateRepository.deleteAll(coordinates);
        }

        // 3. LookBook 삭제
        lookBookRepository.findAll().stream()
                .filter(lb -> lb.getMember().getId().equals(memberId))
                .forEach(lookBookRepository::delete);

        // 4. Cloth 관련 데이터 삭제
        List<Cloth> clothes =
                clothRepository.findAll().stream()
                        .filter(c -> c.getMember().getId().equals(memberId))
                        .collect(Collectors.toList());

        List<Long> clothIds = clothes.stream().map(Cloth::getId).collect(Collectors.toList());

        if (!clothIds.isEmpty()) {
            // Cloth 이미지 URL 수집 및 삭제
            clothes.forEach(
                    c -> {
                        if (c.getClothImageUrl() != null) {
                            imageUrlsToDelete.add(c.getClothImageUrl());
                        }
                    });
            clothRepository.deleteAll(clothes);
        }

        // 6. MemberTerm 삭제
        memberTermRepository.findAll().stream()
                .filter(mt -> mt.getMember().getId().equals(memberId))
                .forEach(memberTermRepository::delete);

        // 7. CodiveNotification 삭제
        codiveNotificationRepository.findAll().stream()
                .filter(cn -> cn.getMember().getId().equals(memberId))
                .forEach(codiveNotificationRepository::delete);

        // 8. Follow 삭제 (followFrom, followTo 모두)
        followRepository.findAll().stream()
                .filter(
                        f ->
                                f.getFollowFrom().getId().equals(memberId)
                                        || f.getFollowTo().getId().equals(memberId))
                .forEach(followRepository::delete);

        // 9. PendingFollow 삭제
        pendingFollowRepository.findAll().stream()
                .filter(
                        pf ->
                                pf.getFollowFrom().getId().equals(memberId)
                                        || pf.getFollowTo().getId().equals(memberId))
                .forEach(pendingFollowRepository::delete);

        // 10. Block 삭제 (blocker, blocked 모두)
        blockRepository.findAll().stream()
                .filter(
                        b ->
                                b.getBlocker().getId().equals(memberId)
                                        || b.getBlocked().getId().equals(memberId))
                .forEach(blockRepository::delete);

        // 11. Report 삭제 (reporter, reported 모두)
        reportRepository.findAll().stream()
                .filter(
                        r ->
                                r.getReporter().getId().equals(memberId)
                                        || r.getReported().getId().equals(memberId))
                .forEach(reportRepository::delete);

        // 12. RefreshToken 삭제
        refreshTokenRepository.findById(memberId).ifPresent(refreshTokenRepository::delete);

        // 13. Member 프로필 이미지 URL 수집
        if (currentMember.getProfileImageUrl() != null) {
            imageUrlsToDelete.add(currentMember.getProfileImageUrl());
        }
        if (currentMember.getProfileBackImageUrl() != null) {
            imageUrlsToDelete.add(currentMember.getProfileBackImageUrl());
        }

        // 14. 이미지 삭제 이벤트 발행 (비동기 처리)
        if (!imageUrlsToDelete.isEmpty()) {
            eventPublisher.publishEvent(ImagesDeleteEvent.of(imageUrlsToDelete));
        }

        // 15. Member 삭제
        memberRepository.delete(currentMember);

        // 16. MeiliSearch에 동기화
        eventPublisher.publishEvent(MemberDeleteEvent.of(memberId, historyIds));
    }

    private Member getMember(RefreshTokenDto refreshTokenDto) {
        return memberRepository
                .findById(refreshTokenDto.memberId())
                .orElseThrow(() -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
