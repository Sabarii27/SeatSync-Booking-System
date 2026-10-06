package com.sabari.seatsync.repository;
import com.sabari.seatsync.entity.BookingSeat;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    @Query("select bs from BookingSeat bs join fetch bs.seat where bs.booking.id in :ids")
    List<BookingSeat> findByBookingIdIn(@Param("ids") Collection<Long> ids);
}
