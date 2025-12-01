package com.flightapp.bookingservice.dto;

import com.flightapp.bookingservice.model.Passenger;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequest {
    private String userName;
    private String email;
    private int numberOfSeats;
    private List<Passenger> passengers;
}
