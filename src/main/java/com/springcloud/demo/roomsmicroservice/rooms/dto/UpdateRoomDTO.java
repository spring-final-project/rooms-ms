package com.springcloud.demo.roomsmicroservice.rooms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UUID;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoomDTO {

    @PositiveOrZero
    private Integer num;

    private String name;

    private Integer floor;

    @Length(max = 255)
    private String description;

    @Size(max = 10)
    @Builder.Default
    private List<MultipartFile> newImages = new ArrayList<>();

    @Size(max = 10)
    @Builder.Default
    private List<String> imagesToDelete = new ArrayList<>();

    @PositiveOrZero
    private Integer maxCapacity;

    @PositiveOrZero
    private Integer simpleBeds;

    @PositiveOrZero
    private Integer mediumBeds;

    @PositiveOrZero
    private Integer doubleBeds;
}
