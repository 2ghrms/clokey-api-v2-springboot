package org.clokey.history.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_history_hashtag_history_id_hashtag_id",
                    columnNames = {"history_id", "hashtag_id"})
        })
public class HashtagHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    @NotNull
    private Hashtag hashtag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id")
    @NotNull
    private History history;

    @Builder(access = AccessLevel.PRIVATE)
    private HashtagHistory(Hashtag hashtag, History history) {
        this.hashtag = hashtag;
        this.history = history;
    }

    public static HashtagHistory createHashtagHistory(Hashtag hashtag, History history) {
        return HashtagHistory.builder().hashtag(hashtag).history(history).build();
    }
}
