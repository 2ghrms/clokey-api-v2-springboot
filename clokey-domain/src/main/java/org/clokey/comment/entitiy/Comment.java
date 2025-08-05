package org.clokey.comment.entitiy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;
import org.clokey.history.entity.History;
import org.clokey.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    @NotNull
    private String content;

    @NotNull private boolean banned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id")
    @NotNull
    private History history;

    //    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<Reply> replies = new ArrayList<>();

    //    @Builder(access = AccessLevel.PRIVATE)
    //    private Comment(
    //            String content, Member member, History history, boolean banned) {
    //        this.content = content;
    //        this.member = member;
    //        this.history = history;
    //        this.banned = banned;
    //    }
    //
    //    public static Comment createComment(
    //            String content, Member member, History history) {
    //        return Comment.builder()
    //                .content(content)
    //                .member(member)
    //                .history(history)
    //                .banned(false)
    //                .build();
    //    }
}
