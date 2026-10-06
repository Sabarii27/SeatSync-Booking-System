package com.sabari.seatsync.dto;
import java.time.Instant;

public record UserDto(Long id, String name, String email, String role, Instant createdAt) {}
