package org.clokey.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.RegisterStatus;
import org.clokey.member.enums.Visibility;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private String email;

    @Column(unique = true)
    @NotNull
    private String clokeyId;

    @Column(length = 30)
    @NotNull
    private String nickname;

    @Embedded private OauthInfo oauthInfo;

    @Enumerated(EnumType.STRING)
    @NotNull
    private MemberStatus memberStatus;

    @Enumerated(EnumType.STRING)
    @NotNull
    private RegisterStatus registerStatus;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Visibility visibility;

    private String profileImageUrl;
    private String profileBackImageUrl;
    private String bio;
    private String deviceToken;

    private LocalDate inactiveDate;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(
            String email,
            String clokeyId,
            String nickname,
            OauthInfo oauthInfo,
            MemberStatus memberStatus,
            RegisterStatus registerStatus,
            Visibility visibility) {
        this.email = email;
        this.clokeyId = clokeyId;
        this.nickname = nickname;
        this.oauthInfo = oauthInfo;
        this.memberStatus = memberStatus;
        this.registerStatus = registerStatus;
        this.visibility = visibility;
    }

    public static Member createMember(
            String email,
            String clokeyId,
            String nickname,
            OauthInfo oauthInfo,
            MemberStatus memberStatus,
            RegisterStatus registerStatus,
            Visibility visibility) {
        return Member.builder()
                .email(email)
                .clokeyId(clokeyId)
                .nickname(nickname)
                .oauthInfo(oauthInfo)
                .memberStatus(memberStatus)
                .registerStatus(registerStatus)
                .visibility(visibility)
                .build();
    }
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    //    private List<MemberTerm> memberTermList = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    //    private List<ClokeyNotification> clokeyNotifications = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "followFrom", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<Follow> followFroms = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "followTo", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<Follow> followTos = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<Comment> comments = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<Folder> folders = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<MemberLike> memberLikes = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<Reply> replies = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    //    private List<MemberLike> memberLikeList = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    //    private List<Cloth> clothList = new ArrayList<>();
    //
    //    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    //    private List<History> historyList = new ArrayList<>();
}
