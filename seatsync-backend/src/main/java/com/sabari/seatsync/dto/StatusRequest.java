package com.sabari.seatsync.dto;
import com.sabari.seatsync.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull BookingStatus status) {}
