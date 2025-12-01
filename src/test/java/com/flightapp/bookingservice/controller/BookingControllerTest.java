package com.flightapp.bookingservice.controller;

import com.flightapp.bookingservice.dto.*;
import com.flightapp.bookingservice.exception.GlobalExceptionHandler;
import com.flightapp.bookingservice.model.*;
import com.flightapp.bookingservice.service.BookingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(BookingController.class)
@Import(GlobalExceptionHandler.class)
class BookingControllerTest {

    @MockBean
    private BookingService service;

    private final WebTestClient client = WebTestClient.bindToController(new BookingController(service))
            .controllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void testBookTicketSuccess() {

        BookingResponse resp = BookingResponse.builder()
                .pnr("PNR12345")
                .flightId("F001")
                .userName("Keerthika")
                .email("k@example.com")
                .bookingDate(LocalDateTime.now())
                .journeyDate(LocalDateTime.now().plusDays(5))
                .numberOfSeats(1)
                .cancelled(false)
                .build();

        Mockito.when(service.book(any(), any()))
                .thenReturn(Mono.just(resp));

        String json = """
                {
                  "userName": "Keerthika",
                  "email": "k@example.com",
                  "numberOfSeats": 1,
                  "passengers": [
                    {
                      "name": "AAA",
                      "gender": "FEMALE",
                      "age": 22,
                      "seatNumber": "1A",
                      "mealType": "VEG"
                    }
                  ]
                }
                """;

        client.post()
                .uri("/api/booking/F001")
                .contentType(APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo("PNR12345");
    }

    @Test
    void testGetByPnr() {

        BookingResponse resp = BookingResponse.builder()
                .pnr("PNR99999")
                .flightId("F100")
                .email("q@example.com")
                .userName("K")
                .bookingDate(LocalDateTime.now())
                .journeyDate(LocalDateTime.now().plusDays(7))
                .build();

        Mockito.when(service.getByPnr("PNR99999"))
                .thenReturn(Mono.just(resp));

        client.get()
                .uri("/api/booking/PNR99999")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.flightId").isEqualTo("F100");
    }

    @Test
    void testCancelTicket() {

        CancelResponse resp = CancelResponse.builder()
                .pnr("PNR22")
                .message("Cancelled successfully")
                .build();

        Mockito.when(service.cancel("PNR22"))
                .thenReturn(Mono.just(resp));

        client.delete()
                .uri("/api/booking/cancel/PNR22")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cancelled successfully");
    }
}
