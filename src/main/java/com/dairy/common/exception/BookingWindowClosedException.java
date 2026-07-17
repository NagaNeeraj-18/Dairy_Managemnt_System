package com.dairy.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class BookingWindowClosedException extends RuntimeException {
    public BookingWindowClosedException(String message) {
        super(message);
    }
}
