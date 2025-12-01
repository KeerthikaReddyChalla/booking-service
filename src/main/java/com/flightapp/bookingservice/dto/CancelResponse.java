package com.flightapp.bookingservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelResponse {
    private String pnr;
    private String message;
}
