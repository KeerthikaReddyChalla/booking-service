package com.flightapp.bookingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("booking")
public class Booking {

    @Id
    private Long id;

    private String pnr;
    private String flightId;

    private String userName;
    private String email;

    private LocalDateTime bookingDate;
    private LocalDateTime journeyDate;

    private int numberOfSeats;
    private double totalPrice;

    private Boolean cancelled;

    private List<Passenger> passengers;
}
