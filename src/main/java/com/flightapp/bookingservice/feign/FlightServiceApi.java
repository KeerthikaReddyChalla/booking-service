package com.flightapp.bookingservice.feign;

import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

public interface FlightServiceApi {

    @GetMapping("/api/flight/inventory/check")
    Mono<String> checkService(); // sample call
}
