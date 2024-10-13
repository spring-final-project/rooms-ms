package com.springcloud.demo.roomsmicroservice.rooms.controller;

import com.springcloud.demo.roomsmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.roomsmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.roomsmicroservice.rooms.dto.CreateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.FilterRoomsDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.ResponseRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.UpdateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseRoomDTO create(@Valid @ModelAttribute CreateRoomDTO createRoomDTO, @RequestHeader("X-UserId") String idUserLogged) {
        return roomService.create(createRoomDTO, idUserLogged);
    }

    @GetMapping
    List<ResponseRoomDTO> findAll(@Valid @ModelAttribute FilterRoomsDTO filters) {
        return roomService.findAll(filters);
    }

    @GetMapping("/{id}")
    ResponseRoomDTO findById(@PathVariable @UUID String id) {
        return roomService.findById(id);
    }

    @PatchMapping("/{id}")
    ResponseRoomDTO update(
            @PathVariable @UUID String id,
            @Valid @ModelAttribute UpdateRoomDTO updateRoomDTO,
            @RequestHeader("X-UserId") String idUserLogged
    ){
        return roomService.update(id, updateRoomDTO, idUserLogged);
    }

    @DeleteMapping("/{id}")
    SimpleResponseDTO delete(@PathVariable @UUID String id, @RequestHeader("X-UserId") String idUserLogged) {
        return roomService.delete(id, idUserLogged);
    }
}
