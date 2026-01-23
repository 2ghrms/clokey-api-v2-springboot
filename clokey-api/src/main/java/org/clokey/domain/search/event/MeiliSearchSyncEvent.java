package org.clokey.domain.search.event;

public record MeiliSearchSyncEvent(EntityType entityType, Long entityId) {
    public static MeiliSearchSyncEvent of(EntityType entityType, Long entityId) {
        return new MeiliSearchSyncEvent(entityType, entityId);
    }

    public enum EntityType {
        HISTORY,
        MEMBER
    }
}
