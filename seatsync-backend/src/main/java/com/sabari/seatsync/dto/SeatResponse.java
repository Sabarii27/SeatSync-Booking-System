package com.sabari.seatsync.dto;
import com.sabari.seatsync.entity.SeatStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record SeatResponse(Long id, String label, String rowLabel, Integer seatNumber, SeatStatus status,
                           BigDecimal price, boolean heldByMe, Instant holdExpiresAt) {}
