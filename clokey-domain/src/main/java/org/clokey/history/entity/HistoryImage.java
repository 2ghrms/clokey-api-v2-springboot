package org.clokey.history.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id")
    @NotNull
    private History history;

    //    @OneToMany(mappedBy = "historyImage", cascade = CascadeType.ALL, orphanRemoval = true)
    //    private List<HistoryClothTag> clothTags = new ArrayList<>();

    //    @Builder(access = AccessLevel.PRIVATE)
    //    private HistoryImage(String imageUrl, History history) {
    //        this.imageUrl = imageUrl;
    //        this.history = history;
    //    }
    //
    //    public static HistoryImage createHistoryImage(String imageUrl, History history) {
    //        return HistoryImage.builder().imageUrl(imageUrl).history(history).build();
    //    }
}
