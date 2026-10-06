package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.EventRequest;
import com.sabari.seatsync.dto.EventResponse;
import com.sabari.seatsync.entity.Event;
import com.sabari.seatsync.exception.ConflictException;
import com.sabari.seatsync.exception.ResourceNotFoundException;
import com.sabari.seatsync.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {
    private final EventRepository events;
    private final ShowRepository shows;
    private final SeatRepository seats;
    private final BookingRepository bookings;

    public EventService(EventRepository events, ShowRepository shows, SeatRepository seats, BookingRepository bookings) {
        this.events = events; this.shows = shows; this.seats = seats; this.bookings = bookings;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list(String query) {
        List<Event> found = (query == null || query.isBlank()) ? events.findAllByOrderByIdAsc()
                : events.findByTitleContainingIgnoreCaseOrderByIdAsc(query.trim());
        return found.stream().map(EventService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse get(Long id) { return toResponse(find(id)); }

    @Transactional
    public EventResponse create(EventRequest r) {
        return toResponse(events.save(new Event(r.title().trim(), r.description(), r.venue().trim(), r.duration())));
    }

    @Transactional
    public EventResponse update(Long id, EventRequest r) {
        Event e = find(id);
        e.update(r.title().trim(), r.description(), r.venue().trim(), r.duration());
        return toResponse(e);
    }

    @Transactional
    public void delete(Long id) {
        find(id);
        if (bookings.existsByShowEventId(id)) throw new ConflictException("This event has bookings and cannot be deleted");
        seats.deleteByEventId(id);
        shows.deleteByEventId(id);
        events.deleteById(id);
    }

    private Event find(Long id) { return events.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event not found")); }

    static EventResponse toResponse(Event e) {
        return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getVenue(), e.getDuration());
    }
}
