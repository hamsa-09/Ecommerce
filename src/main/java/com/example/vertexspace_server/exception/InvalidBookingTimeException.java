package com.example.vertexspace_server.exception;

public class InvalidBookingTimeException extends RuntimeException {
    public InvalidBookingTimeException(String message) {
        super(message);
    }
}
