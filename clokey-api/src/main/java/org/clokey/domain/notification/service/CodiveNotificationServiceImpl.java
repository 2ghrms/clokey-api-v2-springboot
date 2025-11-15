package org.clokey.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.notification.exception.NotificationErrorCode;
import org.clokey.domain.notification.repository.CodiveNotificationRepository;
import org.clokey.domain.term.enums.TermInfo;
import org.clokey.domain.term.exception.TermErrorCode;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.MemberStatus;
import org.clokey.notification.entity.CodiveNotification;
import org.clokey.notification.enums.RedirectType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodiveNotificationServiceImpl implements CodiveNotificationService {

    private final MemberRepository memberRepository;
    private final CodiveNotificationRepository codiveNotificationRepository;
    private final MemberTermRepository memberTermRepository;
    private final FirebaseMessaging firebaseMessaging;

    private static final String NEW_FOLLOWER_NOTIFICATION = "%s님이 회원님의 옷장을 팔로우하기 시작했습니다.";
    private static final String NEW_PENDING_FOLLOWER_NOTIFICATION = "%s님이 회원님의 옷장에 팔로우를 요청했습니다.";

    @Override
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
                            .putData("clokeyId", followFromMember.getClokeyId())
                            .putData("redirectType", "MEMBER_PROFILE")
                            .build();

            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                throw new BaseCustomException(NotificationErrorCode.NOTIFICATION_FIREBASE_ERROR);
            }

            CodiveNotification codiveNotification =
                    CodiveNotification.createCodiveNotification(
                            followFromMember,
                            content,
                            profileImageUrl,
                            followFromMember.getClokeyId(),
                            RedirectType.MEMBER_REDIRECT);

            codiveNotificationRepository.save(codiveNotification);
        }
    }

    @Override
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
                            .putData("clokeyId", followFromMember.getClokeyId())
                            .putData("redirectType", "MEMBER_PROFILE")
                            .build();

            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                throw new BaseCustomException(NotificationErrorCode.NOTIFICATION_FIREBASE_ERROR);
            }

            CodiveNotification codiveNotification =
                    CodiveNotification.createCodiveNotification(
                            followFromMember,
                            content,
                            profileImageUrl,
                            followFromMember.getClokeyId(),
                            RedirectType.MEMBER_REDIRECT);

            codiveNotificationRepository.save(codiveNotification);
        }
    }

    private Member getMemberById(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));
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
}
