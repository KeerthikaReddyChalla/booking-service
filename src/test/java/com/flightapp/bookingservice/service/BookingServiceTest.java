package com.flightapp.bookingservice.service;

import com.flightapp.bookingservice.dto.BookingRequest;
import com.flightapp.bookingservice.dto.InventoryDTO;
import com.flightapp.bookingservice.dto.CancelResponse;
import com.flightapp.bookingservice.dto.BookingResponse;
import com.flightapp.bookingservice.exception.ResourceNotFoundException;
import com.flightapp.bookingservice.feign.FlightClient;
import com.flightapp.bookingservice.model.Booking;
import com.flightapp.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

class BookingServiceTest {

    private BookingRepository repo;
    private KafkaTemplate<String, String> kafka;
    private FlightClient flightClient;
    private R2dbcEntityTemplate r2dbcTemplate;
    private ReactiveCircuitBreakerFactory<?, ?> cbFactory;
    private BookingService service;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(BookingRepository.class);
        kafka = Mockito.mock(KafkaTemplate.class);
        flightClient = Mockito.mock(FlightClient.class);
        r2dbcTemplate = Mockito.mock(R2dbcEntityTemplate.class);

        service = new BookingService(repo, kafka, flightClient, r2dbcTemplate);
    }

    @Test
    void testBookSuccess() {

        // Inventory returned by Flight-Service
        InventoryDTO inv = InventoryDTO.builder()
                .id(1L)
                .airlineId(10L)
                .fromPlace("HYD")
                .toPlace("DEL")
                .flightDate(LocalDateTime.now().plusDays(5))
                .tripType("ONE_WAY")
                .totalSeats(100)
                .availableSeats(100)
                .price(5000.0)
                .build();

        // Booking that will be saved
        Booking savedBooking = Booking.builder()
                .pnr("PNR-123AAA")
                .flightId("1")
                .userName("K")
                .email("k@a.com")
                .bookingDate(LocalDateTime.now())
                .journeyDate(inv.getFlightDate())
                .numberOfSeats(1)
                .passengers(List.of())
                .totalPrice(5000.0)
                .cancelled(false)
                .build();

        // Mock flightClient.getInventory()
        Mockito.when(flightClient.getInventory(1L))
                .thenReturn(Mono.just(inv));

        // Mock circuit breaker to just pass through the Mono
        @SuppressWarnings("unchecked")
        ReactiveCircuitBreaker cb = Mockito.mock(ReactiveCircuitBreaker.class);

        Mockito.when(cbFactory.create("flightService"))
                .thenReturn(cb);

        Mockito.when(cb.run(any(Mono.class), any()))
                .thenAnswer(invocation -> invocation.getArgument(0)); // just return the original Mono

        // Mock repo.save()
        Mockito.when(repo.save(any()))
                .thenReturn(Mono.just(savedBooking));

        BookingRequest req = BookingRequest.builder()
                .userName("K")
                .email("k@a.com")
                .numberOfSeats(1)
                .passengers(List.of())
                .build();

        StepVerifier.create(service.book("1", req))
                .expectNextMatches(r -> r.getFlightId().equals("1"))
                .verifyComplete();
    }

    @Test
    void testGetByPnrNotFound() {

        Mockito.when(repo.findByPnr("PNR000"))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.getByPnr("PNR000"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void testCancelWithin24HoursFails() {

        Booking b = Booking.builder()
                .pnr("P1")
                .journeyDate(LocalDateTime.now().plusHours(10))
                .build();

        Mockito.when(repo.findByPnr("P1"))
                .thenReturn(Mono.just(b));

        StepVerifier.create(service.cancel("P1"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
