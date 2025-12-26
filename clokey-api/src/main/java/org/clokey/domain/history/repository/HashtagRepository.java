package org.clokey.domain.history.repository;

import java.util.Collection;
import java.util.List;
import org.clokey.history.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    boolean existsByName(String name);

    List<Hashtag> findAllByNameIn(Collection<String> names);
}
