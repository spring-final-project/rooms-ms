package com.springcloud.demo.roomsmicroservice.rooms.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.springcloud.demo.roomsmicroservice.images.model.Image;
import com.springcloud.demo.roomsmicroservice.rooms.dto.CreateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.ResponseRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.UpdateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;

public class RoomMapper {

    public static Room createRoomDtoToRoom(CreateRoomDTO createRoomDTO){
        return Room
                .builder()
                .num(createRoomDTO.getNum())
                .floor(createRoomDTO.getFloor())
                .name(createRoomDTO.getName())
                .maxCapacity(createRoomDTO.getMaxCapacity())
                .description(createRoomDTO.getDescription())
                .simpleBeds(createRoomDTO.getSimpleBeds())
                .mediumBeds(createRoomDTO.getMediumBeds())
                .doubleBeds(createRoomDTO.getDoubleBeds())
                .build();
    }

    public static Room roomToRoomUpdated(Room room, UpdateRoomDTO updateRoomDTO) {
        Room updatedRoom;
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        try {
            updatedRoom = objectMapper.readValue(objectMapper.writeValueAsString(room), Room.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if(updateRoomDTO.getNum() != null){
            updatedRoom.setNum(updateRoomDTO.getNum());
        }
        if(updateRoomDTO.getName() != null){
            updatedRoom.setName(updateRoomDTO.getName());
        }
        if(updateRoomDTO.getFloor() != null){
            updatedRoom.setFloor(updateRoomDTO.getFloor());
        }
        if(updateRoomDTO.getDescription() != null){
            updatedRoom.setDescription(updateRoomDTO.getDescription());
        }
        if(updateRoomDTO.getMaxCapacity() != null){
            updatedRoom.setMaxCapacity(updateRoomDTO.getMaxCapacity());
        }
        if(updateRoomDTO.getSimpleBeds() != null){
            updatedRoom.setSimpleBeds(updateRoomDTO.getSimpleBeds());
        }
        if(updateRoomDTO.getMediumBeds() != null){
            updatedRoom.setMediumBeds(updateRoomDTO.getMediumBeds());
        }
        if(updateRoomDTO.getDoubleBeds() != null){
            updatedRoom.setDoubleBeds(updateRoomDTO.getDoubleBeds());
        }

        return updatedRoom;
    }

    public static ResponseRoomDTO roomToResponseRoomDto(Room room){
        return ResponseRoomDTO
                .builder()
                .id(room.getId())
                .num(room.getNum())
                .name(room.getName())
                .floor(room.getFloor())
                .maxCapacity(room.getMaxCapacity())
                .description(room.getDescription())
                .ownerId(room.getOwnerId())
                .images(room
                        .getImages()
                        .stream()
                        .map(Image::getUrl
                        )
                        .toList())
                .simpleBeds(room.getSimpleBeds())
                .mediumBeds(room.getMediumBeds())
                .doubleBeds(room.getDoubleBeds())
                .ratingAverage(room.getRatingAverage())
                .totalReviews(room.getTotalReviews())
                .build();
    }
}
