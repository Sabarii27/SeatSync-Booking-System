package com.sabari.seatsync.exception;

import org.springframework.http.HttpStatus;

public class InvalidBookingException extends ApiException {
    public InvalidBookingException(String message) { super(HttpStatus.BAD_REQUEST, message); }
}
