package org.clokey.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.comment.entitiy.Comment;
import org.clokey.domain.comment.event.NewCommentEvent;
import org.clokey.domain.comment.event.NewReplyEvent;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.like.event.NewLikeEvent;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.notification.dto.request.TemperatureNotificationRequest;
import org.clokey.domain.notification.dto.response.NotificationListResponse;
import org.clokey.domain.notification.dto.response.UnreadNotificationResponse;
import org.clokey.domain.notification.exception.NotificationErrorCode;
import org.clokey.domain.notification.repository.CodiveNotificationRepository;
import org.clokey.domain.term.enums.TermInfo;
import org.clokey.domain.term.exception.TermErrorCode;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.MemberStatus;
import org.clokey.notification.entity.CodiveNotification;
import org.clokey.notification.enums.NotificationType;
import org.clokey.notification.enums.ReadStatus;
import org.clokey.notification.enums.RedirectType;
import org.clokey.response.SliceResponse;
import org.clokey.util.StorageUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodiveNotificationServiceImpl implements CodiveNotificationService {

    private final MemberRepository memberRepository;
    private final CodiveNotificationRepository codiveNotificationRepository;
    private final MemberTermRepository memberTermRepository;
    private final HistoryRepository historyRepository;
    private final CommentRepository commentRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final ApplicationEventPublisher eventPublisher;

    private final MemberUtil memberUtil;
    private final StorageUtil storageUtil;

    private static final String NEW_FOLLOWER_NOTIFICATION = "%s님이 회원님의 옷장을 팔로우하기 시작했습니다.";
    private static final String NEW_PENDING_FOLLOWER_NOTIFICATION = "%s님이 회원님의 옷장에 팔로우를 요청했습니다.";
    private static final String NEW_COMMENT_NOTIFICATION = "%s님이 회원님의 기록에 댓글을 남겼습니다. : %s";
    private static final String NEW_REPLY_NOTIFICATION = "%s님이 회원님의 댓글에 답장을 남겼습니다. : %s";
    private static final String NEW_LIKE_NOTIFICATION = "%s님이 회원님의 기록을 좋아합니다.";

    private static final String TODAY_TEMPERATURE_NOTIFICATION =
            "오늘의 기온은 %d도 입니다!\n날씨에 맞는 오늘의 옷차림이 기다리고 있어요👀";
    private static final String TEMPERATURE_ICON_OBJECT_KEY =
            "icon/temperature/temperature_icon.jpeg";

    @Override
    @Transactional
    public void sendNewFollowerNotification(Long followFromId, Long followToId) {
        Member followFromMember = getMemberById(followFromId);
        Member followToMember = getMemberById(followToId);

        if (isAbleToSendNotification(followToMember)) {
            String content =
                    String.format(NEW_FOLLOWER_NOTIFICATION, followFromMember.getNickname());
            String profileImageUrl = followFromMember.getProfileImageUrl();

            Notification notification =
                    Notification.builder().setBody(content).setImage(profileImageUrl).build();

            Message message =
                    Message.builder()
                            .setToken(followToMember.getDeviceToken())
                            .setNotification(notification)
                            .putData("followFromId", String.valueOf(followFromMember.getId()))
                            .putData("nickname", followFromMember.getNickname())
                            .putData("redirectType", "MEMBER_PROFILE")
                            .build();

            CodiveNotification codiveNotification =
                    CodiveNotification.createCodiveNotification(
                            followToMember,
                            content,
                            profileImageUrl,
                            followToMember.getNickname(),
                            RedirectType.MEMBER_REDIRECT,
                            NotificationType.FOLLOW);

            codiveNotificationRepository.save(codiveNotification);
            eventPublisher.publishEvent(PushSendEvent.from(message));
        }
    }

    @Override
    @Transactional
    public void sendNewPendingFollowerNotification(Long followFromId, Long followToId) {
        Member followFromMember = getMemberById(followFromId);
        Member followToMember = getMemberById(followToId);

        if (isAbleToSendNotification(followToMember)) {
            String content =
                    String.format(
                            NEW_PENDING_FOLLOWER_NOTIFICATION, followFromMember.getNickname());
            String profileImageUrl = followFromMember.getProfileImageUrl();

            Notification notification =
                    Notification.builder().setBody(content).setImage(profileImageUrl).build();

            Message message =
                    Message.builder()
                            .setToken(followToMember.getDeviceToken())
                            .setNotification(notification)
                            .putData("followFromId", String.valueOf(followFromMember.getId()))
                            .putData("nickname", followFromMember.getNickname())
                            .putData("redirectType", "MEMBER_PROFILE")
                            .build();

            CodiveNotification codiveNotification =
                    CodiveNotification.createCodiveNotification(
                            followToMember,
                            content,
                            profileImageUrl,
                            followToMember.getNickname(),
                            RedirectType.MEMBER_REDIRECT,
                            NotificationType.FOLLOW_REQUEST);

            codiveNotificationRepository.save(codiveNotification);
            eventPublisher.publishEvent(PushSendEvent.from(message));
        }
    }

    @Override
    @Transactional
    public void sendNewCommentNotification(NewCommentEvent event) {

        Member receiver = getMemberById(event.receiverId());

        if (isAbleToSendNotification(receiver)) {
            String content =
                    String.format(
                            NEW_COMMENT_NOTIFICATION,
                            event.commenterNickname(),
                            event.commentContent());
            String profileImageUrl = event.commenterProfileImageUrl();

            Notification notification =
                    Notification.builder().setBody(content).setImage(profileImageUrl).build();

            Message message =
                    Message.builder()
                            .setToken(receiver.getDeviceToken())
                            .setNotification(notification)
                            .putData("historyId", String.valueOf(event.historyId()))
                            .putData("commentId", String.valueOf(event.commentId()))
                            .putData("commenterId", String.valueOf(event.commenterId()))
                            .build();
            CodiveNotification codiveNotification =
                    CodiveNotification.createCodiveNotification(
                            receiver,
                            content,
                            profileImageUrl,
                            String.valueOf(event.historyId()),
                            RedirectType.HISTORY_REDIRECT,
                            NotificationType.COMMENT);

            codiveNotificationRepository.save(codiveNotification);
            eventPublisher.publishEvent(PushSendEvent.from(message));
        }
    }

    @Override
    @Transactional
    public void sendNewReplyNotification(NewReplyEvent event) {
        Member receiver = getMemberById(event.receiverId());

        if (isAbleToSendNotification(receiver)) {
            String content =
                    String.format(
                            NEW_REPLY_NOTIFICATION, event.replierNickname(), event.replyContent());
            String profileImageUrl = event.replierProfileImageUrl();

            Notification notification =
                    Notification.builder().setBody(content).setImage(profileImageUrl).build();
            Message message =
                    Message.builder()
                            .setToken(receiver.getDeviceToken())
                            .setNotification(notification)
                            .putData("historyId", String.valueOf(event.historyId()))
                            .putData("parentCommentId", String.valueOf(event.parentCommentId()))
                            .putData("replierId", String.valueOf(event.replierId()))
                            .build();

            CodiveNotification codiveNotification =
                    CodiveNotification.createCodiveNotification(
                            receiver,
                            content,
                            profileImageUrl,
                            String.valueOf(event.historyId()),
                            RedirectType.HISTORY_REDIRECT,
                            NotificationType.REPLY);

            codiveNotificationRepository.save(codiveNotification);
            eventPublisher.publishEvent(PushSendEvent.from(message));
        }
    }

    @Override
    @Transactional
    public void sendNewLikeNotification(NewLikeEvent event) {
        Member receiver = getMemberById(event.receiverId());

        if (isAbleToSendNotification(receiver)) {
            String content = String.format(NEW_LIKE_NOTIFICATION, event.likerNickname());
            String profileImageUrl = event.likerProfileImageUrl();

            Notification notification =
                    Notification.builder().setBody(content).setImage(profileImageUrl).build();
            Message message =
                    Message.builder()
                            .setToken(receiver.getDeviceToken())
                            .setNotification(notification)
                            .putData("historyId", String.valueOf(event.historyId()))
                            .putData("likerId", String.valueOf(event.likerId()))
                            .putData("likerNickName", String.valueOf(event.likerNickname()))
                            .build();
            CodiveNotification codiveNotification =
                    CodiveNotification.createCodiveNotification(
                            receiver,
                            content,
                            profileImageUrl,
                            event.likerNickname(),
                            RedirectType.MEMBER_REDIRECT,
                            NotificationType.LIKE);

            codiveNotificationRepository.save(codiveNotification);
            eventPublisher.publishEvent(PushSendEvent.from(message));
        }
    }

    @Override
    @Transactional
    public void sendNewTemperatureNotification(TemperatureNotificationRequest request) {
        Member receiver = memberUtil.getCurrentMember();
        String content =
                String.format(TODAY_TEMPERATURE_NOTIFICATION, Math.round(request.temperature()));

        if (isAbleToSendNotification(receiver)) {
            String temperatureImageUrl =
                    storageUtil.buildPublicObjectUrl(TEMPERATURE_ICON_OBJECT_KEY);

            Notification notification =
                    Notification.builder().setBody(content).setImage(temperatureImageUrl).build();
            Message message =
                    Message.builder()
                            .setToken(receiver.getDeviceToken())
                            .setNotification(notification)
                            .build();

            CodiveNotification codiveNotification =
                    CodiveNotification.createCodiveNotification(
                            receiver,
                            content,
                            temperatureImageUrl,
                            "",
                            RedirectType.NONE,
                            NotificationType.TEMPERATURE_DAILY);

            codiveNotificationRepository.save(codiveNotification);
            eventPublisher.publishEvent(PushSendEvent.from(message));
        }
    }

    @Override
    public SliceResponse<NotificationListResponse> getNotificationList(
            Long lastNotificationId, Integer size) {
        Member currentMember = memberUtil.getCurrentMember();

        return SliceResponse.from(
                codiveNotificationRepository.findAllNotificationsByMemberId(
                        currentMember.getId(), lastNotificationId, size));
    }

    @Override
    public UnreadNotificationResponse existsUnreadNotification() {
        Member currentMember = memberUtil.getCurrentMember();

        boolean isUnreadNotification =
                codiveNotificationRepository.existsByMemberAndReadStatus(
                        currentMember, ReadStatus.NOT_READ);

        return new UnreadNotificationResponse(isUnreadNotification);
    }

    @Override
    @Transactional
    public void updateReadStatus(Long notificationId) {
        CodiveNotification notification = getNotificationById(notificationId);
        notification.updateReadStatus(ReadStatus.READ);
        codiveNotificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void updateAllReadStatus() {
        Member member = memberUtil.getCurrentMember();

        codiveNotificationRepository.updateAllReadStatusByMemberId(member.getId());
    }

    private Member getMemberById(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private History getHistoryById(Long historyId) {
        return historyRepository
                .findById(historyId)
                .orElseThrow(() -> new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository
                .findById(commentId)
                .orElseThrow(() -> new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    private CodiveNotification getNotificationById(Long notificationId) {
        return codiveNotificationRepository
                .findById(notificationId)
                .orElseThrow(
                        () ->
                                new BaseCustomException(
                                        NotificationErrorCode.NOTIFICATION_NOT_FOUND));
    }

    private boolean isAbleToSendNotification(Member followToMember) {

        boolean isActive = (followToMember.getMemberStatus() == MemberStatus.ACTIVE);

        boolean hasDeviceToken =
                (followToMember.getDeviceToken() != null
                        && !followToMember.getDeviceToken().isBlank());

        boolean hasAgreed =
                memberTermRepository
                        .findByMemberIdAndTermId(
                                followToMember.getId(), TermInfo.PUSH_NOTIFICATION_RECEIVE.getId())
                        .orElseThrow(() -> new BaseCustomException(TermErrorCode.TERM_NOT_FOUND))
                        .isAgreed();

        return isActive && hasDeviceToken && hasAgreed;
    }

    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPushAfterCommit(PushSendEvent event) {
        try {
            firebaseMessaging.send(event.message());
        } catch (FirebaseMessagingException e) {
            log.warn("[Notification] Firebase 전송 실패", e);
            throw new BaseCustomException(NotificationErrorCode.NOTIFICATION_FIREBASE_ERROR);
        }
    }

    private record PushSendEvent(Message message) {
        private static PushSendEvent from(Message message) {
            return new PushSendEvent(message);
        }
    }
}
