package com.springcloud.demo.roomsmicroservice.rooms.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UUID;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterRoomsDTO {

    @PositiveOrZero
    private Integer simpleBeds;

    @PositiveOrZero
    private Integer mediumBeds;

    @PositiveOrZero
    private Integer doubleBeds;

    @PositiveOrZero
    private Integer maxCapacity;

    @Positive
    @Builder.Default
    private Integer page = 1;

    @Positive
    @Builder.Default
    private Integer limit = 25;

    @UUID
    private String ownerId;
}
