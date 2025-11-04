package org.clokey.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.cloth.entity.Cloth;
import org.clokey.comment.entitiy.Comment;
import org.clokey.common.model.BaseEntity;
import org.clokey.folder.entity.Folder;
import org.clokey.history.entity.History;
import org.clokey.like.entity.MemberLike;
import org.clokey.member.enums.MemberRole;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.Visibility;
import org.clokey.notification.entity.ClokeyNotification;
import org.clokey.term.entity.MemberTerm;

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
    private MemberRole memberRole;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Visibility visibility;

    private String profileImageUrl;
    private String profileBackImageUrl;

    @Column(length = 100)
    private String bio;

    private String deviceToken;

    private LocalDate inactiveDate;

    @OneToMany(mappedBy = "member")
    private List<MemberTerm> memberTermList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<ClokeyNotification> clokeyNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "followFrom")
    private List<Follow> followFroms = new ArrayList<>();

    @OneToMany(mappedBy = "followTo")
    private List<Follow> followTos = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Folder> folders = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<MemberLike> memberLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<MemberLike> memberLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Cloth> clothList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<History> historyList = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Member(
            String email,
            String clokeyId,
            String nickname,
            OauthInfo oauthInfo,
            MemberStatus memberStatus,
            MemberRole memberRole,
            Visibility visibility) {
        this.email = email;
        this.clokeyId = clokeyId;
        this.nickname = nickname;
        this.oauthInfo = oauthInfo;
        this.memberStatus = memberStatus;
        this.memberRole = memberRole;
        this.visibility = visibility;
    }

    public static Member createMember(
            String email, String clokeyId, String nickname, OauthInfo oauthInfo) {
        return Member.builder()
                .email(email)
                .clokeyId(clokeyId)
                .nickname(nickname)
                .oauthInfo(oauthInfo)
                .memberStatus(MemberStatus.ACTIVE)
                .memberRole(MemberRole.USER)
                .visibility(Visibility.PUBLIC)
                .build();
    }

    public void updateProfile(
            String nickname,
            String clokeyId,
            String profileImageUrl,
            String profileBackImageUrl,
            String bio,
            Visibility visibility) {
        this.nickname = nickname;
        this.clokeyId = clokeyId;
        this.profileImageUrl = profileImageUrl;
        this.profileBackImageUrl = profileBackImageUrl;
        this.bio = bio;
        this.visibility = visibility;
    }

    public void updateMemberStatus(MemberStatus memberStatus) {
        this.memberStatus = memberStatus;
    }

    public void changeVisibility() {
        this.visibility =
                (this.visibility == Visibility.PUBLIC) ? Visibility.PRIVATE : Visibility.PUBLIC;
    }

    public void updateDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
