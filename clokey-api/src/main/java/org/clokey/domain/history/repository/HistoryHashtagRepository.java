package org.clokey.domain.history.repository;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.clokey.history.entity.HistoryHashtag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistoryHashtagRepository
        extends JpaRepository<HistoryHashtag, Long>, HistoryHashtagRepositoryCustom {

    @EntityGraph(attributePaths = "hashtag")
    List<HistoryHashtag> findByHistoryId(Long historyId);

    @Query(
            """
        select hh from HistoryHashtag hh
        join fetch hh.hashtag
        where hh.history.id = :historyId
    """)
    List<HistoryHashtag> findAllByHistoryIdWithHashtag(@Param("historyId") Long historyId);
}
