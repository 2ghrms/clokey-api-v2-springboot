package org.clokey.like.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;
import org.clokey.history.entity.History;
import org.clokey.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member_like",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_member_like_member_id_history_id",
                    columnNames = {"member_id", "history_id"})
        })
public class MemberLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id")
    @NotNull
    private History history;

    //    @Builder(access = AccessLevel.PRIVATE)
    //    private MemberLike(Member member, History history) {
    //        this.member = member;
    //        this.history = history;
    //    }
    //
    //    public static MemberLike createMemberLike(Member member, History history) {
    //        return MemberLike.builder().member(member).history(history).build();
    //    }
}
