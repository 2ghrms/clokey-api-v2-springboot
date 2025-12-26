package org.clokey.domain.history.repository;

import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.history.entity.HistoryHashtag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HistoryHashtagRepositoryCustomImpl implements HistoryHashtagRepositoryCustom {
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsertHistoryHashtags(List<HistoryHashtag> links) {
        if (links == null || links.isEmpty()) return;

        final String sql =
                "INSERT INTO history_hashtag (history_id, hashtag_id, created_at, updated_at) "
                        + "VALUES (?, ?, NOW(), NOW())";

        jdbcTemplate.batchUpdate(
                sql,
                links,
                BATCH_SIZE,
                (PreparedStatement ps, HistoryHashtag hh) -> {
                    ps.setLong(1, hh.getHistory().getId());
                    ps.setLong(2, hh.getHashtag().getId());
                });
    }
}
