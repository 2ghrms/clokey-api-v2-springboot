package org.clokey.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.entity.Member;
import org.clokey.notification.enums.ReadStatus;
import org.clokey.notification.enums.RedirectType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodiveNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    @NotNull
    private String content;

    @NotNull private String notificationImageUrl;

    // ex) historyId
    @NotNull private String redirectInfo;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RedirectType redirectType;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReadStatus readStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @Builder(access = AccessLevel.PRIVATE)
    private CodiveNotification(
            Member member,
            String content,
            String notificationImageUrl,
            String redirectInfo,
            RedirectType redirectType,
            ReadStatus readStatus) {
        this.member = member;
        this.content = content;
        this.notificationImageUrl = notificationImageUrl;
        this.redirectInfo = redirectInfo;
        this.redirectType = redirectType;
        this.readStatus = readStatus;
    }

    public static CodiveNotification createCodiveNotification(
            Member member,
            String content,
            String notificationImageUrl,
            String redirectInfo,
            RedirectType redirectType) {
        return CodiveNotification.builder()
                .member(member)
                .content(content)
                .notificationImageUrl(notificationImageUrl)
                .redirectInfo(redirectInfo)
                .redirectType(redirectType)
                .readStatus(ReadStatus.NOT_READ)
                .build();
    }

    public void updateReadStatus(ReadStatus readStatus) {
        this.readStatus = readStatus;
    }
}
