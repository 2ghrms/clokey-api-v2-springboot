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
public class HistoryHashtag extends BaseEntity {

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
    private HistoryHashtag(History history, Hashtag hashtag) {
        this.history = history;
        this.hashtag = hashtag;
    }

    public static HistoryHashtag createHistoryHashtag(History history, Hashtag hashtag) {
        return HistoryHashtag.builder().history(history).hashtag(hashtag).build();
    }
}
