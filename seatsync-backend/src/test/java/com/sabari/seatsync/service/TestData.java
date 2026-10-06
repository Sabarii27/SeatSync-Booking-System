package com.sabari.seatsync.service;

import com.sabari.seatsync.entity.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/** Small helpers for unit tests (entities have no public id setters). */
final class TestData {
    private TestData() {}

    static <T> T withId(T entity, Long id) { ReflectionTestUtils.setField(entity, "id", id); return entity; }

    static Show show(Long id) {
        Event event = withId(new Event("Movie", "d", "Venue", 120), 1L);
        return withId(new Show(event, LocalDate.now().plusDays(1), LocalTime.of(19, 0), LocalTime.of(21, 0)), id);
    }

    static Seat seat(Long id, Show show, SeatStatus status, Long heldBy, Instant expires) {
        Seat s = withId(new Seat(show, "A", id.intValue(), new BigDecimal("250")), id);
        ReflectionTestUtils.setField(s, "status", status);
        ReflectionTestUtils.setField(s, "heldBy", heldBy);
        s.setHoldExpiresAt(expires);
        return s;
    }

    static User user(Long id) { return withId(new User("U" + id, "u" + id + "@t.com", "x", UserRole.USER), id); }
}
