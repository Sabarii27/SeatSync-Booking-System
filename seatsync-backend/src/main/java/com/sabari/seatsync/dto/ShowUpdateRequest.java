package com.sabari.seatsync.dto;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowUpdateRequest(@NotNull LocalDate showDate, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {}
