package com.sabari.seatsync.repository;
import com.sabari.seatsync.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByOrderByIdAsc();
    List<Event> findByTitleContainingIgnoreCaseOrderByIdAsc(String title);
}
