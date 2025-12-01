package com.flightapp.bookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, Object>> handle(ResourceNotFoundException ex) {
        return Mono.just(Map.of(
                "status", 404,
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
