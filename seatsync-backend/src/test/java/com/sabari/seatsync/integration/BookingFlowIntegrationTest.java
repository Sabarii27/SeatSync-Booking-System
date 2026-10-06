package com.sabari.seatsync.integration;

import com.sabari.seatsync.dto.BookingResponse;
import com.sabari.seatsync.dto.ShowResponse;
import com.sabari.seatsync.entity.*;
import com.sabari.seatsync.exception.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void showCreationGeneratesSeatGrid() {
        ShowResponse show = newShow();
        assertEquals(10, seats.countByShowId(show.id()));
        assertEquals(10, show.availableSeats());
    }

    @Test
    void holdThenConfirmCreatesBookingAndBooksSeats() {
        ShowResponse show = newShow();
        User ann = newUser("Ann");
        List<Seat> grid = seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(show.id());
        List<Long> ids = List.of(grid.get(0).getId(), grid.get(1).getId());

        seatService.hold(ann.getId(), show.id(), ids);
        BookingResponse booking = bookingService.confirm(ann.getId(), show.id(), ids);

        assertEquals(BookingStatus.CONFIRMED, booking.status());
        assertEquals(0, new java.math.BigDecimal("500").compareTo(booking.totalAmount()));
        assertEquals(List.of("A1", "A2"), booking.seats());
        assertEquals(SeatStatus.BOOKED, seats.findById(ids.get(0)).orElseThrow().getStatus());
        assertEquals(1, bookingService.listForUser(ann.getId()).size());
    }

    @Test
    void confirmWithoutHoldIsRejected() {
        ShowResponse show = newShow();
        User ann = newUser("Ann");
        Long seatId = seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(show.id()).get(0).getId();
        assertThrows(SeatHoldExpiredException.class, () -> bookingService.confirm(ann.getId(), show.id(), List.of(seatId)));
    }

    @Test
    void otherUserCannotHoldAlreadyHeldSeat() {
        ShowResponse show = newShow();
        User ann = newUser("Ann"), bob = newUser("Bob");
        Long seatId = seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(show.id()).get(0).getId();
        seatService.hold(ann.getId(), show.id(), List.of(seatId));
        assertThrows(SeatUnavailableException.class, () -> seatService.hold(bob.getId(), show.id(), List.of(seatId)));
    }

    @Test
    void expiredHoldIsReleasedAndCanBeTakenByAnotherUser() {
        ShowResponse show = newShow();
        User ann = newUser("Ann"), bob = newUser("Bob");
        Long seatId = seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(show.id()).get(0).getId();
        seatService.hold(ann.getId(), show.id(), List.of(seatId));

        Seat seat = seats.findById(seatId).orElseThrow();
        seat.setHoldExpiresAt(Instant.now().minusSeconds(1));      // simulate the 5 minutes passing
        seats.save(seat);

        assertEquals(1, seatService.releaseExpiredHolds());
        assertEquals(SeatStatus.AVAILABLE, seats.findById(seatId).orElseThrow().getStatus());
        assertThrows(SeatHoldExpiredException.class, () -> bookingService.confirm(ann.getId(), show.id(), List.of(seatId)));
        assertDoesNotThrow(() -> seatService.hold(bob.getId(), show.id(), List.of(seatId)));
    }

    @Test
    void cancellationReleasesSeatsAndCannotBeRepeated() {
        ShowResponse show = newShow();
        User ann = newUser("Ann");
        Long seatId = seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(show.id()).get(0).getId();
        seatService.hold(ann.getId(), show.id(), List.of(seatId));
        BookingResponse booking = bookingService.confirm(ann.getId(), show.id(), List.of(seatId));

        BookingResponse cancelled = bookingService.cancel(ann.getId(), booking.id());

        assertEquals(BookingStatus.CANCELLED, cancelled.status());
        assertEquals(SeatStatus.AVAILABLE, seats.findById(seatId).orElseThrow().getStatus());
        assertThrows(InvalidBookingException.class, () -> bookingService.cancel(ann.getId(), booking.id()));
    }
}
