package com.sabari.seatsync.exception;

import org.springframework.http.HttpStatus;

public class SeatUnavailableException extends ApiException {
    public SeatUnavailableException(String message) { super(HttpStatus.CONFLICT, message); }
}
