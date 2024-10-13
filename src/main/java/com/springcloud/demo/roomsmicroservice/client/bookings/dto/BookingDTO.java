package com.springcloud.demo.roomsmicroservice.client.bookings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private String id;
    private String roomId;
    private Integer rating;
}
