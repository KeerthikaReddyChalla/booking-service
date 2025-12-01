package com.flightapp.bookingservice.dto;

import com.flightapp.bookingservice.model.Passenger;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {

    private String pnr;
    private String flightId;

    private String userName;
    private String email;

    private LocalDateTime bookingDate;
    private LocalDateTime journeyDate;

    private int numberOfSeats;
    private double totalPrice;

    private List<Passenger> passengers;
    private boolean cancelled;
}
