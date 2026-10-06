package com.sabari.seatsync.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowRequest(@NotNull Long eventId, @NotNull LocalDate showDate, @NotNull LocalTime startTime,
                          @NotNull LocalTime endTime, @NotNull @Min(1) @Max(26) Integer rows,
                          @NotNull @Min(1) @Max(30) Integer seatsPerRow, @NotNull @DecimalMin("0.01") BigDecimal price) {}
