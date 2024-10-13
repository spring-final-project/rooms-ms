package com.springcloud.demo.roomsmicroservice.rooms.service;

import com.springcloud.demo.roomsmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.roomsmicroservice.exceptions.BadRequestException;
import com.springcloud.demo.roomsmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.roomsmicroservice.images.model.Image;
import com.springcloud.demo.roomsmicroservice.images.service.ImageService;
import com.springcloud.demo.roomsmicroservice.rooms.dto.CreateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.FilterRoomsDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.ResponseRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.UpdateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import com.springcloud.demo.roomsmicroservice.rooms.repository.RoomRepository;

import static org.assertj.core.api.Assertions.*;

import com.springcloud.demo.roomsmicroservice.rooms.repository.RoomSpecifications;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    RoomRepository roomRepository;

    @Mock
    RoomSpecifications roomSpecifications;

    @Mock
    ImageService imageService;

    @InjectMocks
    RoomService roomService;

    Room mockedRoom;

    @BeforeEach
    void setup() {
        mockedRoom = Room.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .num(1)
                .images(List.of(Image.builder().url("image1.png").build()))
                .build();
    }

    @Nested
    class Create {

        CreateRoomDTO createRoomDTO;

        @BeforeEach
        void setup() {
            createRoomDTO = new CreateRoomDTO();
        }

        @Test
        void createRoom() {
            createRoomDTO.setNum(1);

            given(roomRepository.save(any(Room.class))).willReturn(mockedRoom);

            ResponseRoomDTO response = roomService.create(createRoomDTO, mockedRoom.getOwnerId().toString());

            verify(imageService, never()).createImages(anyList(), any(Room.class));
            verify(roomRepository).save(any(Room.class));
            assertThat(response.getId()).isEqualTo(mockedRoom.getId());
            assertThat(response.getNum()).isEqualTo(mockedRoom.getNum());
            assertThat(response.getOwnerId()).isEqualTo(mockedRoom.getOwnerId());
            assertThat(response.getImages().size()).isEqualTo(mockedRoom.getImages().size());
        }

        @Test
        void createRoomWithImages() {
            MultipartFile file = new MockMultipartFile("test", new byte[0]);
            createRoomDTO.setNum(1);
            createRoomDTO.setImages(List.of(file));

            given(imageService.createImages(anyList(), any(Room.class))).willReturn(List.of(new Image()));
            given(roomRepository.save(any(Room.class))).willReturn(mockedRoom);
            willDoNothing().given(imageService).saveImages(anyList());

            ResponseRoomDTO response = roomService.create(createRoomDTO, mockedRoom.getOwnerId().toString());

            verify(imageService, times(1)).createImages(eq(createRoomDTO.getImages()), any(Room.class));
            verify(roomRepository).save(argThat(room -> room.getImages().isEmpty()));
            verify(imageService).saveImages(argThat(images -> images.size() == 1));
            assertThat(response).isInstanceOf(ResponseRoomDTO.class);
        }

        @Test
        void errorWhenNumAndNameAreEmpty() {
            BadRequestException e = Assertions.assertThrows(BadRequestException.class, () -> {
                roomService.create(createRoomDTO, mockedRoom.getOwnerId().toString());
            });

            verify(roomRepository, never()).save(any(Room.class));
            assertThat(e.getMessage()).contains("Number or name must be specify");
        }
    }

    @Nested
    class FindAll {

        FilterRoomsDTO filters;

        @BeforeEach
        void setup() {
            filters = new FilterRoomsDTO();
        }

        @Test
        void findAll() {
            given(roomRepository.findAll((Specification<Room>) any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(mockedRoom)));
            given(roomSpecifications.withFilters(any(FilterRoomsDTO.class))).willReturn((root, query, builder) -> builder.and());

            List<ResponseRoomDTO> response = roomService.findAll(filters);

            verify(roomSpecifications).withFilters(filters);
            verify(roomRepository).findAll(
                    eq(roomSpecifications.withFilters(filters)),
                    eq(PageRequest.of(0, 25))
            );
            assertThat(response.size()).isEqualTo(1);
        }

        @Test
        void findAllWithFilters() {
            filters.setPage(2);
            filters.setLimit(10);
            filters.setDoubleBeds(2);
            filters.setMaxCapacity(4);

            given(roomRepository.findAll((Specification<Room>) any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(mockedRoom)));
            given(roomSpecifications.withFilters(any(FilterRoomsDTO.class))).willReturn((root, query, builder) -> builder.and());

            List<ResponseRoomDTO> response = roomService.findAll(filters);

            verify(roomSpecifications).withFilters(filters);
            verify(roomRepository).findAll(
                    eq(roomSpecifications.withFilters(filters)),
                    eq(PageRequest.of(1, 10))
            );
            assertThat(response.size()).isEqualTo(1);
        }
    }

    @Nested
    class FindById {

        UUID idToFind;

        @BeforeEach
        void setup() {
            idToFind = mockedRoom.getId();
        }

        @Test
        void findById() {
            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.of(mockedRoom));

            ResponseRoomDTO response = roomService.findById(idToFind.toString());

            verify(roomRepository).findById(idToFind);
            assertThat(response).isInstanceOf(ResponseRoomDTO.class);
            assertThat(response.getId()).isEqualTo(idToFind);
        }

        @Test
        void errorWhenNotFoundRoomById() {
            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> {
                roomService.findById(idToFind.toString());
            });

            verify(roomRepository).findById(idToFind);
            assertThat(e.getMessage()).contains("Not found room with id");

        }
    }

    @Nested
    class Update {

        UpdateRoomDTO updateRoomDTO;
        UUID idRoomToUpdate;
        Room updatedRoom;

        @BeforeEach
        void setup() {
            updateRoomDTO = new UpdateRoomDTO();
            idRoomToUpdate = mockedRoom.getId();
            updatedRoom = Room.builder()
                    .id(mockedRoom.getId())
                    .ownerId(mockedRoom.getOwnerId())
                    .images(mockedRoom.getImages())
                    .num(2)
                    .doubleBeds(2)
                    .build();
        }

        @Test
        void updateRoom() {
            updateRoomDTO.setNum(2);
            updateRoomDTO.setDoubleBeds(2);

            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.of(mockedRoom));
            given(roomRepository.save(any(Room.class))).willReturn(updatedRoom);

            ResponseRoomDTO response = roomService.update(idRoomToUpdate.toString(), updateRoomDTO, mockedRoom.getOwnerId().toString());

            System.out.println(mockedRoom.getNum());
            System.out.println(updateRoomDTO.getNum());

            verify(roomRepository).findById(idRoomToUpdate);
            verify(imageService, never()).createImages(anyList(), any(Room.class));
            verify(imageService, never()).deleteImages(anyList());
            verify(roomRepository).save(argThat(room ->
                    Objects.equals(room.getNum(), updateRoomDTO.getNum()) &&
                            Objects.equals(room.getDoubleBeds(), updateRoomDTO.getDoubleBeds()))
            );
            assertThat(response.getId()).isEqualTo(idRoomToUpdate);
            assertThat(response.getNum()).isEqualTo(updatedRoom.getNum());
            assertThat(response.getDoubleBeds()).isEqualTo(updatedRoom.getDoubleBeds());
        }

        @Test
        void addNewImages() {
            updatedRoom.setImages(List.of(
                            Image.builder().url("image1.png").build(),
                            Image.builder().url("image2.png").build())
            );

            MultipartFile file = new MockMultipartFile("test", new byte[0]);
            updateRoomDTO.setNewImages(List.of(file));

            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.of(mockedRoom));
            given(imageService.createImages(anyList(), any(Room.class))).willReturn(List.of(new Image()));
            given(roomRepository.save(any(Room.class))).willReturn(updatedRoom);

            ResponseRoomDTO response = roomService.update(idRoomToUpdate.toString(), updateRoomDTO, mockedRoom.getOwnerId().toString());

            verify(roomRepository).findById(idRoomToUpdate);
            verify(imageService).createImages(eq(updateRoomDTO.getNewImages()), argThat(room -> room.getId().equals(idRoomToUpdate)));
            verify(imageService,never()).deleteImages(anyList());
            verify(roomRepository).save(argThat(room -> room.getId().equals(idRoomToUpdate)));
            assertThat(response.getImages()).hasSameSizeAs(updatedRoom.getImages());
        }

        @Test
        void deleteImages() {
            updateRoomDTO.setImagesToDelete(List.of("image1.png"));
            updatedRoom.setImages(List.of());

            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.of(mockedRoom));
            willDoNothing().given(imageService).deleteImages(anyList());
            given(roomRepository.save(any(Room.class))).willReturn(updatedRoom);

            ResponseRoomDTO response = roomService.update(idRoomToUpdate.toString(), updateRoomDTO, mockedRoom.getOwnerId().toString());

            verify(roomRepository).findById(idRoomToUpdate);
            verify(imageService,never()).createImages(anyList(), any(Room.class));
            verify(imageService).deleteImages(updateRoomDTO.getImagesToDelete());
            verify(roomRepository).save(argThat(room -> room.getImages().isEmpty()));
            assertThat(response.getId()).isEqualTo(idRoomToUpdate);
            assertThat(response.getImages()).isEmpty();
        }
    }

    @Nested
    class UpdateRating {

        UUID idRoomToUpdate;
        Room updatedRoom;

        @BeforeEach
        void setup() {
            idRoomToUpdate = mockedRoom.getId();
            updatedRoom = Room.builder()
                    .id(mockedRoom.getId())
                    .ownerId(mockedRoom.getOwnerId())
                    .images(mockedRoom.getImages())
                    .num(mockedRoom.getNum())
                    .doubleBeds(mockedRoom.getDoubleBeds())
                    .ratingAverage(5)
                    .totalReviews(1)
                    .build();
        }

        @Test
        void setRating() {
            int rating = 5;

            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.of(mockedRoom));
            given(roomRepository.save(any(Room.class))).willReturn(updatedRoom);

            roomService.updateRating(idRoomToUpdate.toString(), rating);

            verify(roomRepository).findById(idRoomToUpdate);
            verify(roomRepository).save(argThat(room -> room.getRatingAverage() == rating));
        }

        @Test
        void updateRating() {
            mockedRoom.setTotalReviews(1);
            mockedRoom.setRatingAverage(5);
            int rating = 9;

            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.of(mockedRoom));
            given(roomRepository.save(any(Room.class))).willReturn(updatedRoom);

            roomService.updateRating(idRoomToUpdate.toString(), rating);

            verify(roomRepository).findById(idRoomToUpdate);
            verify(roomRepository).save(argThat(room -> room.getRatingAverage() == 7));
        }
    }

    @Nested
    class Delete {

        UUID idToFind;

        @BeforeEach
        void setup(){
            idToFind = UUID.randomUUID();
        }

        @Test
        void deleteRoom(){
            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.of(mockedRoom));
            willDoNothing().given(roomRepository).delete(any(Room.class));

            SimpleResponseDTO response = roomService.delete(idToFind.toString(), mockedRoom.getOwnerId().toString());

            verify(roomRepository).findById(idToFind);
            verify(roomRepository).delete(mockedRoom);
            assertThat(response.isOk()).isTrue();
        }

        @Test
        void errorWhenNotFoundRoomById(){
            given(roomRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, ()-> {
                roomService.delete(idToFind.toString(), mockedRoom.getOwnerId().toString());
            });

            verify(roomRepository).findById(idToFind);
            verify(roomRepository,never()).delete(any(Room.class));
            assertThat(e.getMessage()).contains("Not found room by id");
        }
    }
}