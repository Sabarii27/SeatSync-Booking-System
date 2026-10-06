package com.sabari.seatsync.service;

import com.sabari.seatsync.entity.*;
import com.sabari.seatsync.exception.SeatUnavailableException;
import com.sabari.seatsync.repository.SeatRepository;
import com.sabari.seatsync.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static com.sabari.seatsync.service.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {
    @Mock SeatRepository seats;
    @Mock ShowRepository shows;
    SeatService service;
    Show show = show(10L);

    @BeforeEach
    void setUp() { service = new SeatService(seats, shows, 5); }

    @Test
    void holdMovesAvailableSeatToHeldForFiveMinutes() {
        Seat s = seat(1L, show, SeatStatus.AVAILABLE, null, null);
        when(shows.existsById(10L)).thenReturn(true);
        when(seats.lockByIds(List.of(1L))).thenReturn(List.of(s));

        var res = service.hold(7L, 10L, List.of(1L));

        assertEquals(SeatStatus.HELD, s.getStatus());
        assertEquals(7L, s.getHeldBy());
        assertTrue(res.holdExpiresAt().isAfter(Instant.now().plusSeconds(290)));
    }

    @Test
    void holdFailsWhenSomeoneElseHoldsItAndHoldIsStillValid() {
        Seat s = seat(1L, show, SeatStatus.HELD, 99L, Instant.now().plusSeconds(120));
        when(shows.existsById(10L)).thenReturn(true);
        when(seats.lockByIds(List.of(1L))).thenReturn(List.of(s));
        assertThrows(SeatUnavailableException.class, () -> service.hold(7L, 10L, List.of(1L)));
    }

    @Test
    void holdSucceedsWhenOtherUsersHoldAlreadyExpired() {
        Seat s = seat(1L, show, SeatStatus.HELD, 99L, Instant.now().minusSeconds(5));
        when(shows.existsById(10L)).thenReturn(true);
        when(seats.lockByIds(List.of(1L))).thenReturn(List.of(s));
        service.hold(7L, 10L, List.of(1L));
        assertEquals(7L, s.getHeldBy());
    }

    @Test
    void holdFailsOnBookedSeat() {
        Seat s = seat(1L, show, SeatStatus.BOOKED, null, null);
        when(shows.existsById(10L)).thenReturn(true);
        when(seats.lockByIds(List.of(1L))).thenReturn(List.of(s));
        assertThrows(SeatUnavailableException.class, () -> service.hold(7L, 10L, List.of(1L)));
    }

    @Test
    void seatListShowsExpiredHoldAsAvailable() {
        Seat s = seat(1L, show, SeatStatus.HELD, 99L, Instant.now().minusSeconds(5));
        when(shows.existsById(10L)).thenReturn(true);
        when(seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(10L)).thenReturn(List.of(s));
        assertEquals(SeatStatus.AVAILABLE, service.getSeats(10L, 7L).get(0).status());
    }
}
