package com.sabari.seatsync.dto;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SeatActionRequest(@NotEmpty List<Long> seatIds) {}
