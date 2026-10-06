package com.sabari.seatsync.dto;
import jakarta.validation.constraints.*;

public record RegisterRequest(@NotBlank @Size(max = 100) String name, @NotBlank @Email String email,
                              @NotBlank @Size(min = 6, max = 100) String password) {}
