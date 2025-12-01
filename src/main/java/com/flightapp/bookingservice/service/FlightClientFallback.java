package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.InventoryDTO;
import com.flightapp.bookingservice.feign.FlightClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FlightClientFallback implements FlightClient {

    @Override
    public Mono<String> checkInventory() {
        log.error("Fallback: checkInventory() called because Flight-Service is DOWN");
        return Mono.just("Flight-Service unavailable");
    }

    @Override
    public Mono<InventoryDTO> getInventory(Long id) {
        log.error("Fallback: getInventory({}) called because Flight-Service is DOWN", id);
        return Mono.error(new RuntimeException("Flight-Service unavailable — please try again later."));
    }
}
