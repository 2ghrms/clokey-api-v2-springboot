package org.clokey.term.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTerm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    @NotNull
    private Term term;

    //    @Builder(access = AccessLevel.PRIVATE)
    //    private MemberTerm(Member member, Term term) {
    //        this.member = member;
    //        this.term = term;
    //    }
    //
    //    public static MemberTerm createMemberTerm(Member member, Term term) {
    //        return MemberTerm.builder().member(member).term(term).build();
    //    }
}
