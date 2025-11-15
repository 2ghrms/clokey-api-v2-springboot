package org.clokey.domain.notification.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.google.firebase.messaging.FirebaseMessaging;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.clokey.IntegrationTest;
import org.clokey.domain.member.event.NewFollowerEvent;
import org.clokey.domain.member.event.NewPendingFollowerEvent;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.member.service.MemberService;
import org.clokey.domain.notification.repository.CodiveNotificationRepository;
import org.clokey.domain.term.enums.TermInfo;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.clokey.member.enums.Visibility;
import org.clokey.notification.entity.CodiveNotification;
import org.clokey.term.entity.MemberTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
public class NotificationServiceTest extends IntegrationTest {

    @Autowired private CodiveNotificationService notificationService;
    @Autowired private MemberService memberService;
    @Autowired private CodiveNotificationRepository notificationRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ApplicationEvents applicationEvents;

    @MockitoBean private MemberUtil memberUtil;
    @MockitoBean private FirebaseMessaging firebaseMessaging;
    @MockitoBean private MemberTermRepository memberTermRepository;

    @Nested
    class 공개_계정에_팔로우_할_때 {

        @BeforeEach
        void setUp() throws Exception {

            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testCodiveId1",
                            "followFrom",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            member1.updateProfile(
                    "followFrom",
                    "testCodiveId1",
                    "example.com",
                    "example2.com",
                    "한줄소개~",
                    Visibility.PUBLIC);
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testCodiveId2",
                            "followTo",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            member2.updateDeviceToken("test-device-token-for-member2");
            memberRepository.saveAll(List.of(member1, member2));

            given(memberUtil.getCurrentMember()).willReturn(member1);

            MemberTerm mockAgreement = Mockito.mock(MemberTerm.class);
            given(mockAgreement.isAgreed()).willReturn(true);

            given(
                            memberTermRepository.findByMemberIdAndTermId(
                                    eq(2L), eq(TermInfo.PUSH_NOTIFICATION_RECEIVE.getId())))
                    .willReturn(Optional.of(mockAgreement));
        }

        @Test
        void 유효한_요청이면_새_팔로우_이벤트를_발행한다() throws Exception {
            // when
            memberService.toggleFollow(2L);

            // then
            var events = applicationEvents.stream(NewFollowerEvent.class).toList();
            Assertions.assertThat(events).hasSize(1);
            Assertions.assertThat(events.getFirst().followToId()).isEqualTo(2L);
        }

        @Test
        void 유효한_요청이면_새_팔로우_알림을_저장한다() {
            // when
            notificationService.sendNewFollowerNotification(1L, 2L);

            // then
            Optional<CodiveNotification> notification = notificationRepository.findById(1L);

            assertThat(notification.get().getMember().getId()).isEqualTo(1L);
            assertThat(notification.get().getContent())
                    .isEqualTo("followFrom님이 회원님의 옷장을 팔로우하기 시작했습니다.");
        }
    }

    @Nested
    class 비공개_계정에_팔로우_할_때 {

        @BeforeEach
        void setUp() throws Exception {

            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testCodiveId1",
                            "followFrom",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            member1.updateProfile(
                    "followFrom",
                    "testCodiveId1",
                    "example.com",
                    "example2.com",
                    "한줄소개~",
                    Visibility.PUBLIC);
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testCodiveId2",
                            "followTo",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            member2.updateDeviceToken("test-device-token-for-member2");
            member2.changeVisibility();
            memberRepository.saveAll(List.of(member1, member2));

            given(memberUtil.getCurrentMember()).willReturn(member1);

            MemberTerm mockAgreement = Mockito.mock(MemberTerm.class);
            given(mockAgreement.isAgreed()).willReturn(true);

            given(
                            memberTermRepository.findByMemberIdAndTermId(
                                    eq(2L), eq(TermInfo.PUSH_NOTIFICATION_RECEIVE.getId())))
                    .willReturn(Optional.of(mockAgreement));
        }

        @Test
        void 유효한_요청이면_새_팔로우_요청_이벤트를_발행한다() throws Exception {
            // when
            memberService.togglePendingFollow(2L);

            // then
            var events = applicationEvents.stream(NewPendingFollowerEvent.class).toList();
            Assertions.assertThat(events).hasSize(1);
            Assertions.assertThat(events.getFirst().followToId()).isEqualTo(2L);
        }

        @Test
        void 유효한_요청이면_새_팔로우_요청_알림을_저장한다() {
            // when
            notificationService.sendNewPendingFollowerNotification(1L, 2L);

            // then
            Optional<CodiveNotification> notification = notificationRepository.findById(1L);

            assertThat(notification.get().getMember().getId()).isEqualTo(1L);
            assertThat(notification.get().getContent())
                    .isEqualTo("followFrom님이 회원님의 옷장에 팔로우를 요청했습니다.");
        }
    }
}
