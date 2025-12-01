package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.*;
import com.flightapp.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService service;

    @PostMapping("/{flightId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookingResponse> book(
            @PathVariable String flightId,
            @RequestBody BookingRequest req
    ) {
        return service.book(flightId, req);
    }

    @GetMapping("/{pnr}")
    public Mono<BookingResponse> getByPnr(@PathVariable String pnr) {
        return service.getByPnr(pnr);
    }

    @DeleteMapping("/cancel/{pnr}")
    public Mono<CancelResponse> cancel(@PathVariable String pnr) {
        return service.cancel(pnr);
    }
}
