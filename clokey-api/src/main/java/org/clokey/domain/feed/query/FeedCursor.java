package org.clokey.domain.feed.query;

import java.time.LocalDateTime;
import java.util.List;

public record FeedCursor(LocalDateTime createdAt, Long feedId, List<Long> pendingFeedIds) {}
