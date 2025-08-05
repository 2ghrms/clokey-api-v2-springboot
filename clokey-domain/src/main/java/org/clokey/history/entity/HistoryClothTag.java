package org.clokey.history.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.common.model.BaseEntity;

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
    @JoinColumn(name = "history_cloth_id")
    @NotNull
    private HistoryCloth historyCloth;

    @NotNull
    @Column(name = "coordinate_x")
    private Double coordinateX;

    @NotNull
    @Column(name = "coordinate_y")
    private Double coordinateY;
}
