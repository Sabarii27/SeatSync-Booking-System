package com.sabari.seatsync.exception;

import org.springframework.http.HttpStatus;

public class SeatHoldExpiredException extends ApiException {
    public SeatHoldExpiredException(String message) { super(HttpStatus.CONFLICT, message); }
}
