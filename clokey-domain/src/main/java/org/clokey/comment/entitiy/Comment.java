package org.clokey.comment.entitiy;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany(mappedBy = "comment")
    private List<Reply> replies = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Comment(String content, boolean banned, Member member, History history) {
        this.content = content;
        this.banned = banned;
        this.member = member;
        this.history = history;
    }

    public static Comment createComment(String content, Member member, History history) {
        return Comment.builder()
                .content(content)
                .banned(false)
                .member(member)
                .history(history)
                .build();
    }
}
