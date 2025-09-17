package org.clokey.lookbook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.common.model.BaseEntity;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LookBook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    @OneToMany(mappedBy = "lookBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Coordinate> codies = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private LookBook(String name, Member member) {
        this.name = name;
        this.member = member;
    }

    public static LookBook createLookBook(String name, Member member) {
        return LookBook.builder().name(name).member(member).build();
    }
}
