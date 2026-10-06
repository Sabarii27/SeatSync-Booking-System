package com.sabari.seatsync.dto;
import java.time.Instant;
import java.util.List;

public record HoldResponse(List<Long> seatIds, Instant holdExpiresAt) {}
