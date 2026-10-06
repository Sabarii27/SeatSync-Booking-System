package com.sabari.seatsync.controller;

import com.sabari.seatsync.dto.*;
import com.sabari.seatsync.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final EventService eventService;
    private final ShowService showService;
    private final BookingService bookingService;
    private final UserService userService;

    public AdminController(EventService eventService, ShowService showService, BookingService bookingService, UserService userService) {
        this.eventService = eventService; this.showService = showService;
        this.bookingService = bookingService; this.userService = userService;
    }

    @PostMapping("/events") @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody EventRequest r) { return eventService.create(r); }

    @PutMapping("/events/{id}")
    public EventResponse updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest r) { return eventService.update(id, r); }

    @DeleteMapping("/events/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) { eventService.delete(id); }

    @PostMapping("/shows") @ResponseStatus(HttpStatus.CREATED)
    public ShowResponse createShow(@Valid @RequestBody ShowRequest r) { return showService.create(r); }

    @PutMapping("/shows/{id}")
    public ShowResponse updateShow(@PathVariable Long id, @Valid @RequestBody ShowUpdateRequest r) { return showService.update(id, r); }

    @DeleteMapping("/shows/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShow(@PathVariable Long id) { showService.delete(id); }

    @GetMapping("/shows/{id}/seats")
    public ShowResponse showAvailability(@PathVariable Long id) { return showService.get(id); }

    @GetMapping("/users")
    public List<UserDto> users() { return userService.listAll(); }

    @GetMapping("/bookings")
    public List<BookingResponse> bookings() { return bookingService.listAll(); }

    @PatchMapping("/bookings/{id}/status")
    public BookingResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest r) {
        return bookingService.adminChangeStatus(id, r.status());
    }
}
