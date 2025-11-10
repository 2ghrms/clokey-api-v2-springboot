package org.clokey.domain.history.repository;

import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.history.entity.HistoryClothTag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HistoryClothTagRepositoryCustomImpl implements HistoryClothTagRepositoryCustom {
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsertHistoryClothTags(List<HistoryClothTag> tags) {
        if (tags == null || tags.isEmpty()) return;

        final String sql =
                "INSERT INTO history_cloth_tag (history_image_id, cloth_id, location_x, location_y, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, NOW(), NOW())";

        jdbcTemplate.batchUpdate(
                sql,
                tags,
                BATCH_SIZE,
                (PreparedStatement ps, HistoryClothTag t) -> {
                    ps.setLong(1, t.getHistoryImage().getId());
                    ps.setLong(2, t.getCloth().getId());
                    ps.setDouble(3, t.getLocation().getLocationX());
                    ps.setDouble(4, t.getLocation().getLocationY());
                });
    }
}
