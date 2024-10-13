package com.springcloud.demo.roomsmicroservice.exceptions.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErrorResponseDTO {
    String message;
    int status;
    List<String> errors;
}
