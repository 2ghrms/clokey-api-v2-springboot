package org.clokey.domain.history.repository;

import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.history.entity.HistoryStyle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HistoryStyleRepositoryCustomImpl implements HistoryStyleRepositoryCustom {
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsertHistoryStyles(List<HistoryStyle> styles) {
        if (styles == null || styles.isEmpty()) return;

        final String sql =
                "INSERT INTO history_style (history_id, style_id, created_at, updated_at) "
                        + "VALUES (?, ?, NOW(), NOW())";

        jdbcTemplate.batchUpdate(
                sql,
                styles,
                BATCH_SIZE,
                (PreparedStatement ps, HistoryStyle hs) -> {
                    ps.setLong(1, hs.getHistory().getId());
                    ps.setLong(2, hs.getStyle().getId());
                });
    }
}
