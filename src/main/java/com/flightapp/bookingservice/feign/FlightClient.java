package com.flightapp.bookingservice.feign;

import com.flightapp.bookingservice.config.FeignConfig;
import com.flightapp.bookingservice.dto.InventoryDTO;
import com.flightapp.bookingservice.service.FlightClientFallback;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import reactor.core.publisher.Mono;

@FeignClient(
        name = "Flight-service",
        configuration = FeignConfig.class,
        fallback = FlightClientFallback.class
)
public interface FlightClient {

    @GetMapping("/api/flight/inventory/check")
    Mono<String> checkInventory();
    
    @GetMapping("/api/flight/inventory/{id}")
    Mono<InventoryDTO> getInventory(@PathVariable("id") Long id);
}
