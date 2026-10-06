package com.sabari.seatsync.integration;

import com.sabari.seatsync.dto.BookingResponse;
import com.sabari.seatsync.dto.ShowResponse;
import com.sabari.seatsync.entity.BookingStatus;
import com.sabari.seatsync.entity.Seat;
import com.sabari.seatsync.entity.SeatStatus;
import com.sabari.seatsync.entity.User;
import com.sabari.seatsync.exception.SeatHoldExpiredException;
import com.sabari.seatsync.exception.SeatUnavailableException;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Real threads, real PostgreSQL, real row locks. Nothing about the locking is mocked. */
class BookingConcurrencyTest extends AbstractIntegrationTest {

    @RepeatedTest(5)
    void manyUsersRaceForTheSameSeatAndExactlyOneWins() throws Exception {
        ShowResponse show = newShow();
        Long seatId = seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(show.id()).get(0).getId();
        int contenders = 8;
        List<User> people = new ArrayList<>();
        for (int i = 0; i < contenders; i++) people.add(newUser("Racer" + i));

        ExecutorService pool = Executors.newFixedThreadPool(contenders);
        CountDownLatch ready = new CountDownLatch(contenders);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        for (User u : people) {
            results.add(pool.submit(() -> {
                ready.countDown();
                go.await();                                           // everyone starts at the same instant
                try {
                    seatService.hold(u.getId(), show.id(), List.of(seatId));
                    bookingService.confirm(u.getId(), show.id(), List.of(seatId));
                    return true;
                } catch (SeatUnavailableException | SeatHoldExpiredException e) {
                    return false;                                      // the expected "you lost" business error
                }
            }));
        }
        ready.await();
        go.countDown();

        int winners = 0;
        for (Future<Boolean> f : results) if (f.get(30, TimeUnit.SECONDS)) winners++;
        pool.shutdown();

        assertEquals(1, winners, "exactly one user must win the seat");
        assertEquals(1, bookings.count());
        assertEquals(1, bookingSeats.count());
        Seat finalSeat = seats.findById(seatId).orElseThrow();
        assertEquals(SeatStatus.BOOKED, finalSeat.getStatus());
        assertEquals(BookingStatus.CONFIRMED, bookings.findAll().get(0).getStatus());
    }

    @Test
    void doubleSubmitOfTheSameConfirmCreatesOnlyOneBooking() throws Exception {
        ShowResponse show = newShow();
        User ann = newUser("Ann");
        Long seatId = seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(show.id()).get(0).getId();
        seatService.hold(ann.getId(), show.id(), List.of(seatId));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        List<Future<?>> fs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            fs.add(pool.submit(() -> {
                go.await();
                try {
                    BookingResponse r = bookingService.confirm(ann.getId(), show.id(), List.of(seatId));
                    if (r != null) success.incrementAndGet();
                } catch (SeatUnavailableException | SeatHoldExpiredException ignored) { }
                return null;
            }));
        }
        go.countDown();
        for (Future<?> f : fs) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(1, success.get());
        assertEquals(1, bookings.count());
        assertEquals(1, bookingSeats.count());
    }
}
