package org.clokey.domain.search.event;

import java.util.List;

public record ClothDeleteEvent(Long clothId, List<Long> historyIds) {
    public static ClothDeleteEvent of(Long clothId, List<Long> historyIds) {
        return new ClothDeleteEvent(clothId, historyIds);
    }
}
