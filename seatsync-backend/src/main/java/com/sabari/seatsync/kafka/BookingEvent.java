package com.sabari.seatsync.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingEvent(String eventType, Long bookingId, Long userId, Long showId,
                           List<Long> seatIds, BigDecimal totalAmount, Instant timestamp) {}
