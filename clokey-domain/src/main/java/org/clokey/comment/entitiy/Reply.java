package org.clokey.comment.entitiy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(length = 100)
    private String content;

    @NotNull private boolean banned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    @NotNull
    private Comment comment;

    @Builder(access = AccessLevel.PRIVATE)
    private Reply(String content, boolean banned, Member member, Comment comment) {
        this.content = content;
        this.banned = banned;
        this.member = member;
        this.comment = comment;
    }

    public static Reply createReply(String content, Member member, Comment comment) {
        return Reply.builder()
                .content(content)
                .banned(false)
                .member(member)
                .comment(comment)
                .build();
    }
}
