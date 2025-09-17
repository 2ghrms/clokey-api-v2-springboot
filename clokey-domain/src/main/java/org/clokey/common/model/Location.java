package org.clokey.common.model;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @NotNull
    @Column(name = "location_x")
    private Double locationX;

    @NotNull
    @Column(name = "location_y")
    private Double locationY;

    @Builder(access = AccessLevel.PRIVATE)
    private Location(Double locationX, Double locationY) {
        this.locationX = locationX;
        this.locationY = locationY;
    }

    public static Location createLocation(Double locationX, Double locationY) {
        return Location.builder().locationX(locationX).locationY(locationY).build();
    }
}
