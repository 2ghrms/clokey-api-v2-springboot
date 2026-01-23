package org.clokey.domain.search.event;

public record HistoryDeleteEvent(Long historyId) {
    public static HistoryDeleteEvent of(Long historyId) {
        return new HistoryDeleteEvent(historyId);
    }
}
