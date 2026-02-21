package com.example.vertexspace_server.exception;

public class JwtAuthenticationException extends RuntimeException {
    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    public JwtAuthenticationException(String message) {
        super(message);
    }
}

