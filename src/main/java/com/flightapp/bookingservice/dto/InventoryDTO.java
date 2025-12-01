package com.flightapp.bookingservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryDTO {
    private Long id;
    private Long airlineId;
    private String fromPlace;
    private String toPlace;
    private LocalDateTime flightDate;
    private String tripType;
    private int totalSeats;
    private int availableSeats;
    private double price;
}
