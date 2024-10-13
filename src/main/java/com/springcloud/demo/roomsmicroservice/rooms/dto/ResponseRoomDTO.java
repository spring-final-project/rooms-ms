package com.springcloud.demo.roomsmicroservice.rooms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseRoomDTO {
    private UUID id;
    private Integer num;
    private String name;
    private Integer floor;
    private Integer maxCapacity;
    private String description;
    private UUID ownerId;
    private List<String> images;
    private Integer simpleBeds;
    private Integer mediumBeds;
    private Integer doubleBeds;
    private Integer ratingAverage;
    private Integer totalReviews;
}
