package com.sabari.seatsync.repository;
import com.sabari.seatsync.entity.Show;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {
    @Query("select s from Show s join fetch s.event where s.id = :id")
    Optional<Show> findWithEventById(@Param("id") Long id);

    @Query("select s from Show s join fetch s.event where s.event.id = :eventId order by s.showDate, s.startTime")
    List<Show> findByEventDetailed(@Param("eventId") Long eventId);

    @Modifying
    @Query("delete from Show s where s.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}
