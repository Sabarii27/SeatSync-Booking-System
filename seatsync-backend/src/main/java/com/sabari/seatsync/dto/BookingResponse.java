package com.sabari.seatsync.dto;
import com.sabari.seatsync.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record BookingResponse(Long id, String eventTitle, String venue, Long showId, LocalDate showDate,
                              LocalTime startTime, List<String> seats, BigDecimal totalAmount,
                              BookingStatus status, Instant createdAt, String userEmail) {}
