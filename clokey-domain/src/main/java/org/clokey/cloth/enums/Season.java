package org.clokey.cloth.enums;

public enum Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER;

    public Season next() {
        return switch (this) {
            case SPRING -> SUMMER;
            case SUMMER -> FALL;
            case FALL -> WINTER;
            case WINTER -> SPRING;
        };
    }

    public Season previous() {
        return switch (this) {
            case SPRING -> WINTER;
            case SUMMER -> SPRING;
            case FALL -> SUMMER;
            case WINTER -> FALL;
        };
    }
}
