package com.sabari.seatsync.dto;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowResponse(Long id, Long eventId, String eventTitle, String venue, LocalDate showDate,
                           LocalTime startTime, LocalTime endTime, long totalSeats, long availableSeats) {}
