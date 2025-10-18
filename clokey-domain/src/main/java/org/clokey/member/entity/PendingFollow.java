package org.clokey.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "pending_follow",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_pending_follow_to_id_from_id",
                    columnNames = {"follow_to_id", "follow_from_id"})
        })
public class PendingFollow {

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
    public PendingFollow(Member followTo, Member followFrom) {
        this.followTo = followTo;
        this.followFrom = followFrom;
    }

    public static PendingFollow createPendingFollow(Member followFrom, Member followTo) {
        return PendingFollow.builder().followFrom(followFrom).followTo(followTo).build();
    }
}
