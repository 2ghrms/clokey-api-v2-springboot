package org.clokey.domain.feed.service;

import java.util.List;
import org.clokey.domain.feed.dto.response.FeedListResponse;
import org.clokey.domain.feed.query.FollowScope;

public interface FeedService {
    FeedListResponse getFeeds(
            FollowScope followScope,
            List<Long> styleIds,
            List<Long> situationIds,
            Integer size,
            String cursor);
}
