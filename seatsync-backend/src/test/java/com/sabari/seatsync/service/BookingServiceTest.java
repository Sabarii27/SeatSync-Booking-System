package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.BookingResponse;
import com.sabari.seatsync.entity.*;
import com.sabari.seatsync.exception.*;
import com.sabari.seatsync.kafka.BookingEvent;
import com.sabari.seatsync.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.sabari.seatsync.service.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock BookingRepository bookings;
    @Mock BookingSeatRepository bookingSeats;
    @Mock SeatRepository seats;
    @Mock ShowRepository shows;
    @Mock UserRepository users;
    @Mock ApplicationEventPublisher publisher;
    @InjectMocks BookingService service;

    Show show = show(10L);
    User user = user(7L);

    private void stubBasics(List<Seat> locked) {
        when(shows.findWithEventById(10L)).thenReturn(Optional.of(show));
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(seats.lockByIds(anyCollection())).thenReturn(locked);
    }

    @Test
    void confirmBooksHeldSeatsAndCalculatesTotalOnServer() {
        Instant later = Instant.now().plusSeconds(200);
        Seat a = seat(1L, show, SeatStatus.HELD, 7L, later);
        Seat b = seat(2L, show, SeatStatus.HELD, 7L, later);
        stubBasics(List.of(a, b));
        when(bookings.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse res = service.confirm(7L, 10L, List.of(2L, 1L));

        assertEquals(0, new BigDecimal("500").compareTo(res.totalAmount()));
        assertEquals(BookingStatus.CONFIRMED, res.status());
        assertEquals(SeatStatus.BOOKED, a.getStatus());
        assertEquals(SeatStatus.BOOKED, b.getStatus());
        verify(publisher).publishEvent(any(BookingEvent.class));
    }

    @Test
    void confirmFailsWhenHoldExpired() {
        Seat a = seat(1L, show, SeatStatus.HELD, 7L, Instant.now().minusSeconds(1));
        stubBasics(List.of(a));
        assertThrows(SeatHoldExpiredException.class, () -> service.confirm(7L, 10L, List.of(1L)));
        verify(bookings, never()).save(any());
    }

    @Test
    void confirmFailsWhenSeatWasNeverHeld() {
        stubBasics(List.of(seat(1L, show, SeatStatus.AVAILABLE, null, null)));
        assertThrows(SeatHoldExpiredException.class, () -> service.confirm(7L, 10L, List.of(1L)));
    }

    @Test
    void confirmFailsWhenAnotherUserHoldsTheSeat() {
        stubBasics(List.of(seat(1L, show, SeatStatus.HELD, 99L, Instant.now().plusSeconds(100))));
        assertThrows(SeatUnavailableException.class, () -> service.confirm(7L, 10L, List.of(1L)));
    }

    @Test
    void confirmFailsWhenSeatBelongsToDifferentShow() {
        Seat other = seat(1L, show(11L), SeatStatus.HELD, 7L, Instant.now().plusSeconds(100));
        stubBasics(List.of(other));
        assertThrows(InvalidBookingException.class, () -> service.confirm(7L, 10L, List.of(1L)));
    }

    @Test
    void cancelRejectsBookingThatIsNotConfirmed() {
        Booking b = withId(new Booking(user, show, BookingStatus.CANCELLED, BigDecimal.TEN), 5L);
        when(bookings.lockById(5L)).thenReturn(Optional.of(b));
        assertThrows(InvalidBookingException.class, () -> service.cancel(7L, 5L));
    }

    @Test
    void cancelRejectsOtherUsersBooking() {
        Booking b = withId(new Booking(user, show, BookingStatus.CONFIRMED, BigDecimal.TEN), 5L);
        when(bookings.lockById(5L)).thenReturn(Optional.of(b));
        assertThrows(ForbiddenException.class, () -> service.cancel(8L, 5L));
    }
}
