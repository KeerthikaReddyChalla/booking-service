package com.flightapp.bookingservice.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Passenger {

    private String name;
    private Gender gender;
    private int age;
    private String seatNumber;
    private MealType mealType;
}
