package com.sabari.seatsync.controller;

import com.sabari.seatsync.dto.BookingResponse;
import com.sabari.seatsync.security.AuthUser;
import com.sabari.seatsync.service.BookingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @GetMapping
    public List<BookingResponse> mine(@AuthenticationPrincipal AuthUser user) { return bookingService.listForUser(user.id()); }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) { return bookingService.get(user.id(), id); }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) { return bookingService.cancel(user.id(), id); }
}
