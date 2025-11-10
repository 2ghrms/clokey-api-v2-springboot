package org.clokey.history.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.cloth.entity.Cloth;
import org.clokey.common.model.BaseEntity;
import org.clokey.common.model.Location;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryClothTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_image_id")
    @NotNull
    private HistoryImage historyImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id")
    @NotNull
    private Cloth cloth;

    @Embedded @NotNull private Location location;

    @Builder(access = AccessLevel.PRIVATE)
    private HistoryClothTag(HistoryImage historyImage, Cloth cloth, Location location) {
        this.historyImage = historyImage;
        this.cloth = cloth;
        this.location = location;
    }

    public static HistoryClothTag createHistoryClothTag(
            HistoryImage historyImage, Cloth cloth, Double locationX, Double locationY) {
        return HistoryClothTag.builder()
                .historyImage(historyImage)
                .cloth(cloth)
                .location(Location.createLocation(locationX, locationY))
                .build();
    }
}
