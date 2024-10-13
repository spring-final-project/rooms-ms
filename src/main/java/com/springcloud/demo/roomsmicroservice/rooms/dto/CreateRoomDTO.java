package com.springcloud.demo.roomsmicroservice.rooms.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomDTO {

    @PositiveOrZero
    private Integer num;

    private String name;

    private Integer floor;

    @Length(max = 255)
    private String description;

    @Size(max = 10)
    @Builder.Default
    private List<MultipartFile> images = new ArrayList<>();

    @PositiveOrZero
    private Integer maxCapacity;

    @PositiveOrZero
    @Builder.Default
    private Integer simpleBeds = 0;

    @PositiveOrZero
    @Builder.Default
    private Integer mediumBeds = 0;

    @PositiveOrZero
    @Builder.Default
    private Integer doubleBeds = 0;
}
