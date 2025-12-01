package com.flightapp.bookingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.bookingservice.dto.*;
import com.flightapp.bookingservice.exception.ResourceNotFoundException;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FlightClient flightClient;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private String generatePNR() {
        return "PNR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @CircuitBreaker(name = "flightService", fallbackMethod = "flightFallback")
    public Mono<BookingResponse> book(String flightId, BookingRequest req) {

        log.info("Booking request received for Flight ID: {}", flightId);

        return flightClient.getInventory(Long.valueOf(flightId))
                .flatMap(inventory -> {

                    if (inventory.getAvailableSeats() < req.getNumberOfSeats()) {
                        return Mono.error(new ResourceNotFoundException("Not enough seats available"));
                    }

                    double totalCost = req.getNumberOfSeats() * inventory.getPrice();

                    Booking booking = Booking.builder()
                            .pnr(generatePNR())
                            .flightId(flightId)
                            .userName(req.getUserName())
                            .email(req.getEmail())
                            .numberOfSeats(req.getNumberOfSeats())
                            .passengers(req.getPassengers())
                            .bookingDate(LocalDateTime.now())
                            .journeyDate(inventory.getFlightDate())
                            .totalPrice(totalCost)
                            .cancelled(false)
                            .build();

                    return repo.save(booking)
                            .flatMap(saved ->
                                    sendKafkaEvent(saved)
                                            .thenReturn(toResponse(saved))
                            );
                });
    }

    // 🔹 Circuit Breaker Fallback Method
    private Mono<BookingResponse> flightFallback(String flightId, BookingRequest req, Throwable ex) {
        log.error("Fallback triggered for flight {} : {}", flightId, ex.getMessage());
        return Mono.error(new RuntimeException("Flight service unavailable, please try again later."));
    }

    private BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .pnr(b.getPnr())
                .flightId(b.getFlightId())
                .userName(b.getUserName())
                .email(b.getEmail())
                .bookingDate(b.getBookingDate())
                .journeyDate(b.getJourneyDate())
                .numberOfSeats(b.getNumberOfSeats())
                .totalPrice(b.getTotalPrice())
                .passengers(b.getPassengers())
                .cancelled(b.getCancelled())
                .build();
    }

    private Mono<Void> sendKafkaEvent(Booking booking) {
        try {
            String json = mapper.writeValueAsString(
                    BookingEvent.builder()
                            .pnr(booking.getPnr())
                            .email(booking.getEmail())
                            .userName(booking.getUserName())
                            .numberOfSeats(booking.getNumberOfSeats())
                            .totalPrice(booking.getTotalPrice())
                            .build()
            );
            kafkaTemplate.send("booking-events", json);
        } catch (Exception e) {
            log.error("Failed to send Kafka event!", e);
        }
        return Mono.empty();
    }

    public Mono<BookingResponse> getByPnr(String pnr) {
        return repo.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Invalid PNR")))
                .map(this::toResponse);
    }

    public Mono<CancelResponse> cancel(String pnr) {
        return repo.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Invalid PNR")))
                .flatMap(b -> {

                    if (b.getJourneyDate().minusHours(24).isBefore(LocalDateTime.now())) {
                        return Mono.error(new ResourceNotFoundException("Cannot cancel within 24 hours"));
                    }

                    b.setCancelled(true);

                    return repo.save(b)
                            .thenReturn(CancelResponse.builder()
                                    .pnr(pnr)
                                    .message("Cancelled successfully")
                                    .build());
                });
    }
}
