package com.sabari.seatsync.repository;
import com.sabari.seatsync.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> lockById(@Param("id") Long id);

    @Query("select b from Booking b join fetch b.show sh join fetch sh.event join fetch b.user where b.id = :id")
    Optional<Booking> findDetailedById(@Param("id") Long id);

    @Query("select b from Booking b join fetch b.show sh join fetch sh.event join fetch b.user "
         + "where b.user.id = :userId order by b.createdAt desc")
    List<Booking> findByUserDetailed(@Param("userId") Long userId);

    @Query("select b from Booking b join fetch b.show sh join fetch sh.event join fetch b.user order by b.createdAt desc")
    List<Booking> findAllDetailed();

    boolean existsByShowId(Long showId);
    boolean existsByShowEventId(Long eventId);
}
