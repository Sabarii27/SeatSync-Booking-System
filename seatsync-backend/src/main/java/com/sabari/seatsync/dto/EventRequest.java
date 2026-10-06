package com.sabari.seatsync.dto;
import jakarta.validation.constraints.*;

public record EventRequest(@NotBlank @Size(max = 200) String title, @Size(max = 2000) String description,
                           @NotBlank @Size(max = 200) String venue, @NotNull @Min(1) Integer duration) {}
