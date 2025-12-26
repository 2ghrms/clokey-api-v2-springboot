package org.clokey.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "follow",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_follow_follow_to_id_follow_from_id",
                    columnNames = {"follow_to_id", "follow_from_id"})
        })
public class Follow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_to_id")
    @NotNull
    private Member followTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follow_from_id")
    @NotNull
    private Member followFrom;

    @Builder(access = AccessLevel.PRIVATE)
    public Follow(Member followTo, Member followFrom) {
        this.followTo = followTo;
        this.followFrom = followFrom;
    }

    public static Follow createFollow(Member followFrom, Member followTo) {
        return Follow.builder().followFrom(followFrom).followTo(followTo).build();
    }
}
