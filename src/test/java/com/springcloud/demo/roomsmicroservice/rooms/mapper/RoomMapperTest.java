package com.springcloud.demo.roomsmicroservice.rooms.mapper;

import com.springcloud.demo.roomsmicroservice.images.model.Image;
import com.springcloud.demo.roomsmicroservice.rooms.dto.CreateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.ResponseRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.UpdateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

class RoomMapperTest {

    @Test
    void createRoomDtoToRoom(){
        MockMultipartFile image1 = new MockMultipartFile("test","image1.png","image/png", new byte[0]);
        CreateRoomDTO createRoomDTO = CreateRoomDTO
                .builder()
                .num(1)
                .floor(1)
                .images(List.of(image1))
                .doubleBeds(2)
                .maxCapacity(4)
                .description("Habitacion doble")
                .build();

        Room room = RoomMapper.createRoomDtoToRoom(createRoomDTO);

        assertThat(room.getNum()).isEqualTo(createRoomDTO.getNum());
        assertThat(room.getFloor()).isEqualTo(createRoomDTO.getFloor());
        assertThat(room.getImages()).isNull();
        assertThat(room.getDoubleBeds()).isEqualTo(createRoomDTO.getDoubleBeds());
        assertThat(room.getMaxCapacity()).isEqualTo(createRoomDTO.getMaxCapacity());
        assertThat(room.getDescription()).isEqualTo(createRoomDTO.getDescription());
    }

    @Test
    void roomToRoomUpdated(){
        UpdateRoomDTO updateRoomDTO = UpdateRoomDTO
                .builder()
                .num(15)
                .name("new name")
                .description("new description")
                .floor(2)
                .simpleBeds(1)
                .mediumBeds(2)
                .doubleBeds(1)
                .maxCapacity(2)
                .build();
        Image image1 = Image.builder().id(UUID.randomUUID()).url("http://localhost/image1.png").build();
        Room room = Room
                .builder()
                .id(UUID.randomUUID())
                .num(1)
                .floor(1)
                .images(List.of(image1))
                .doubleBeds(2)
                .maxCapacity(4)
                .ownerId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();

        Room updatedRoom = RoomMapper.roomToRoomUpdated(room,updateRoomDTO);

        assertThat(updatedRoom.getId()).isEqualTo(room.getId());
        assertThat(updatedRoom.getNum()).isEqualTo(updateRoomDTO.getNum());
        assertThat(updatedRoom.getFloor()).isEqualTo(updateRoomDTO.getFloor());
        assertThat(updatedRoom.getImages()).hasSize(room.getImages().size());
        assertThat(updatedRoom.getDoubleBeds()).isEqualTo(updateRoomDTO.getDoubleBeds());
        assertThat(updatedRoom.getSimpleBeds()).isEqualTo(updateRoomDTO.getSimpleBeds());
        assertThat(updatedRoom.getMediumBeds()).isEqualTo(updateRoomDTO.getMediumBeds());
        assertThat(updatedRoom.getMaxCapacity()).isEqualTo(updateRoomDTO.getMaxCapacity());
        assertThat(updatedRoom.getDescription()).isEqualTo(updateRoomDTO.getDescription());
        assertThat(updatedRoom.getOwnerId()).isEqualTo(room.getOwnerId());
    }

    @Test
    void roomToResponseRoomDto(){
        Image image1 = Image.builder().id(UUID.randomUUID()).url("http://localhost/image1.png").build();
        Room room = Room
                .builder()
                .id(UUID.randomUUID())
                .num(1)
                .floor(1)
                .images(List.of(image1))
                .doubleBeds(2)
                .maxCapacity(4)
                .description("Habitacion doble")
                .ownerId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();

        ResponseRoomDTO response = RoomMapper.roomToResponseRoomDto(room);

        assertThat(response.getId()).isEqualTo(room.getId());
        assertThat(response.getNum()).isEqualTo(room.getNum());
        assertThat(response.getFloor()).isEqualTo(room.getFloor());
        assertThat(response.getImages().size()).isEqualTo(1);
        assertThat(response.getImages().getFirst()).isEqualTo(room.getImages().getFirst().getUrl());
        assertThat(response.getDoubleBeds()).isEqualTo(room.getDoubleBeds());
        assertThat(response.getMaxCapacity()).isEqualTo(room.getMaxCapacity());
        assertThat(response.getDescription()).isEqualTo(room.getDescription());
        assertThat(response.getOwnerId()).isEqualTo(room.getOwnerId());
    }

}