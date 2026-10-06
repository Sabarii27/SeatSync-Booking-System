package com.sabari.seatsync.controller;

import com.sabari.seatsync.dto.*;
import com.sabari.seatsync.security.AuthUser;
import com.sabari.seatsync.service.BookingService;
import com.sabari.seatsync.service.SeatService;
import com.sabari.seatsync.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowController {
    private final ShowService showService;
    private final SeatService seatService;
    private final BookingService bookingService;

    public ShowController(ShowService showService, SeatService seatService, BookingService bookingService) {
        this.showService = showService; this.seatService = seatService; this.bookingService = bookingService;
    }

    @GetMapping("/{id}")
    public ShowResponse get(@PathVariable Long id) { return showService.get(id); }

    /** Public. If a valid token is sent, seats held by that user come back with heldByMe=true. */
    @GetMapping("/{id}/seats")
    public List<SeatResponse> seats(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return seatService.getSeats(id, user == null ? null : user.id());
    }

    @PostMapping("/{id}/holds")
    public HoldResponse hold(@PathVariable Long id, @Valid @RequestBody SeatActionRequest req, @AuthenticationPrincipal AuthUser user) {
        return seatService.hold(user.id(), id, req.seatIds());
    }

    @PostMapping("/{id}/confirm") @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse confirm(@PathVariable Long id, @Valid @RequestBody SeatActionRequest req, @AuthenticationPrincipal AuthUser user) {
        return bookingService.confirm(user.id(), id, req.seatIds());
    }
}
