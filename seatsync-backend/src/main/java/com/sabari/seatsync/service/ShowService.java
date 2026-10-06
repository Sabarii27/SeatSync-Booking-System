package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.ShowRequest;
import com.sabari.seatsync.dto.ShowResponse;
import com.sabari.seatsync.dto.ShowUpdateRequest;
import com.sabari.seatsync.entity.Event;
import com.sabari.seatsync.entity.Seat;
import com.sabari.seatsync.entity.Show;
import com.sabari.seatsync.exception.BadRequestException;
import com.sabari.seatsync.exception.ConflictException;
import com.sabari.seatsync.exception.ResourceNotFoundException;
import com.sabari.seatsync.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShowService {
    private final ShowRepository shows;
    private final EventRepository events;
    private final SeatRepository seats;
    private final BookingRepository bookings;

    public ShowService(ShowRepository shows, EventRepository events, SeatRepository seats, BookingRepository bookings) {
        this.shows = shows; this.events = events; this.seats = seats; this.bookings = bookings;
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> forEvent(Long eventId) {
        if (!events.existsById(eventId)) throw new ResourceNotFoundException("Event not found");
        return shows.findByEventDetailed(eventId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ShowResponse get(Long id) {
        return toResponse(shows.findWithEventById(id).orElseThrow(() -> new ResourceNotFoundException("Show not found")));
    }

    /** Creates the show and its whole seat grid (A1..) in one transaction. */
    @Transactional
    public ShowResponse create(ShowRequest r) {
        Event event = events.findById(r.eventId()).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        checkTimes(r.startTime(), r.endTime());
        Show show = shows.save(new Show(event, r.showDate(), r.startTime(), r.endTime()));
        List<Seat> grid = new ArrayList<>();
        for (int row = 0; row < r.rows(); row++) {
            String label = String.valueOf((char) ('A' + row));
            for (int n = 1; n <= r.seatsPerRow(); n++) grid.add(new Seat(show, label, n, r.price()));
        }
        seats.saveAll(grid);
        return toResponse(show);
    }

    @Transactional
    public ShowResponse update(Long id, ShowUpdateRequest r) {
        Show show = shows.findWithEventById(id).orElseThrow(() -> new ResourceNotFoundException("Show not found"));
        checkTimes(r.startTime(), r.endTime());
        show.reschedule(r.showDate(), r.startTime(), r.endTime());
        return toResponse(show);
    }

    @Transactional
    public void delete(Long id) {
        if (!shows.existsById(id)) throw new ResourceNotFoundException("Show not found");
        if (bookings.existsByShowId(id)) throw new ConflictException("This show has bookings and cannot be deleted");
        seats.deleteByShowId(id);
        shows.deleteById(id);
    }

    private void checkTimes(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) throw new BadRequestException("End time must be after start time");
    }

    private ShowResponse toResponse(Show s) {
        long total = seats.countByShowId(s.getId());
        long available = seats.countAvailable(s.getId(), Instant.now());
        Event e = s.getEvent();
        return new ShowResponse(s.getId(), e.getId(), e.getTitle(), e.getVenue(), s.getShowDate(),
                s.getStartTime(), s.getEndTime(), total, available);
    }
}
