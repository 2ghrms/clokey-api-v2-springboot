package org.clokey.coordinate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CoordinateCloth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded @NotNull private Location location;

    @Positive @NotNull private Double ratio;

    // 옷의 각도 (시계 방향)
    @NotNull private Double degree;

    @NotNull
    @Column(name = "`order`")
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinate_id")
    @NotNull
    private Coordinate coordinate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id")
    @NotNull
    private Cloth cloth;

    @Builder(access = AccessLevel.PRIVATE)
    private CoordinateCloth(
            Location location,
            Double ratio,
            Double degree,
            int order,
            Coordinate coordinate,
            Cloth cloth) {
        this.location = location;
        this.ratio = ratio;
        this.degree = degree;
        this.order = order;
        this.coordinate = coordinate;
        this.cloth = cloth;
    }

    public static CoordinateCloth createCoordinateCloth(
            Double locationX,
            Double locationY,
            Double ratio,
            Double degree,
            int order,
            Coordinate coordinate,
            Cloth cloth) {
        return CoordinateCloth.builder()
                .location(Location.createLocation(locationX, locationY))
                .ratio(ratio)
                .degree(degree)
                .order(order)
                .coordinate(coordinate)
                .cloth(cloth)
                .build();
    }

    public void updateCoordinateCloth(
            Double locationX, Double locationY, Double ratio, Double degree, int order) {
        location.updateLocation(locationX, locationY);
        this.ratio = ratio;
        this.degree = degree;
        this.order = order;
    }
}
