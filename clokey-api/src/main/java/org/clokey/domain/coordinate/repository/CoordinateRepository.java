package org.clokey.domain.coordinate.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.clokey.coordinate.entity.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CoordinateRepository
        extends JpaRepository<Coordinate, Long>, CoordinateRepositoryCustom {

    @Query(
            """
    SELECT c
    FROM Coordinate c
    WHERE c.coordinateType = org.clokey.coordinate.enums.CoordinateType.DAILY
      AND FUNCTION('date', c.createdAt) = :date
      AND c.member.id = :memberId
    """)
    Optional<Coordinate> findDailyCoordinateByDateAndMemberId(LocalDate date, Long memberId);

    default boolean existsDailyCoordinateByDateAndMemberId(LocalDate date, Long memberId) {
        return findDailyCoordinateByDateAndMemberId(date, memberId).isPresent();
    }

    long countByMemberIdAndLikedTrue(Long memberId);

    List<Coordinate> findAllByLookBookId(Long lookBookId);

    @Query("select c from Coordinate c where c.member.id = :memberId and c.liked = true")
    List<Coordinate> findLikedCoordinatesByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);
}
