package com.sabari.seatsync.repository;
import com.sabari.seatsync.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByShowIdOrderByRowLabelAscSeatNumberAsc(Long showId);

    long countByShowId(Long showId);

    /** SELECT ... FOR UPDATE: other transactions touching these rows wait until we commit. Ordered by id to avoid deadlocks. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :ids order by s.id")
    List<Seat> lockByIds(@Param("ids") Collection<Long> ids);

    @Query("select count(s) from Seat s where s.show.id = :showId and (s.status = com.sabari.seatsync.entity.SeatStatus.AVAILABLE "
         + "or (s.status = com.sabari.seatsync.entity.SeatStatus.HELD and s.holdExpiresAt < :now))")
    long countAvailable(@Param("showId") Long showId, @Param("now") Instant now);

    @Modifying
    @Query("update Seat s set s.status = com.sabari.seatsync.entity.SeatStatus.AVAILABLE, s.heldBy = null, s.holdExpiresAt = null "
         + "where s.status = com.sabari.seatsync.entity.SeatStatus.HELD and s.holdExpiresAt < :now")
    int releaseExpired(@Param("now") Instant now);

    @Modifying
    @Query("delete from Seat s where s.show.id = :showId")
    void deleteByShowId(@Param("showId") Long showId);

    @Modifying
    @Query("delete from Seat s where s.show.id in (select sh.id from Show sh where sh.event.id = :eventId)")
    void deleteByEventId(@Param("eventId") Long eventId);
}
