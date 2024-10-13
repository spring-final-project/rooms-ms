package com.springcloud.demo.roomsmicroservice.rooms.integration;

import com.jayway.jsonpath.JsonPath;
import com.springcloud.demo.roomsmicroservice.images.model.Image;
import com.springcloud.demo.roomsmicroservice.images.provider.ImageProviderService;
import com.springcloud.demo.roomsmicroservice.images.repository.ImageRepository;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import com.springcloud.demo.roomsmicroservice.rooms.repository.RoomRepository;
import static org.assertj.core.api.Assertions.*;

import org.hamcrest.Matchers;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RoomTestIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ImageRepository imageRepository;

    @MockBean
    private ImageProviderService imageProviderService;

    List<Room> rooms = new ArrayList<>();

    @BeforeEach
    void setup() {
        roomRepository.deleteAll();

        Room room1 = Room
                .builder()
                .num(1)
                .floor(1)
                .description("Habitación matrimonial")
                .doubleBeds(1)
                .simpleBeds(0)
                .mediumBeds(0)
                .name("Matrimonial")
                .maxCapacity(2)
                .ownerId(UUID.randomUUID())
                .build();
        Image image1 = Image.builder().url("http://localhost/image1.png").room(room1).build();
        Image image2 = Image.builder().url("http://localhost/image2.png").room(room1).build();
        room1.setImages(List.of(image1,image2));

        Room room2 = Room
                .builder()
                .num(2)
                .floor(1)
                .description("Habitación doble")
                .doubleBeds(2)
                .simpleBeds(0)
                .mediumBeds(0)
                .name("Doble")
                .maxCapacity(4)
                .ownerId(UUID.randomUUID())
                .build();
        Image image3 = Image.builder().url("http://localhost/image3.png").room(room2).build();
        room2.setImages(List.of(image3));
        rooms = roomRepository.saveAll(List.of(room1,room2));
    }

    @Nested
    class Create {

        @Test
        @Transactional
        void createRoom() throws Exception {
            String ownerId = UUID.randomUUID().toString();

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/api/rooms")
                            .param("num", "1")
                            .param("doubleBeds", "2")
                            .param("maxCapacity", "4")
                            .header("X-UserId", ownerId)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.num").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ownerId").value(ownerId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.doubleBeds").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.maxCapacity").value(4))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.simpleBeds").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mediumBeds").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.images.size()").value(0))
                    .andReturn();

            String idRoomCreated = JsonPath.parse(result.getResponse().getContentAsString()).read("$.id");
            Room roomCreated = roomRepository.findById(UUID.fromString(idRoomCreated)).orElseThrow(()-> new AssertionFailure("Not found room in database"));
            assertThat(roomCreated.getImages()).hasSize(0);
        }

        @Test
        @Transactional
        void createRoomWithImages() throws Exception {
            String ownerId = UUID.randomUUID().toString();
            MockMultipartFile image1 = new MockMultipartFile("images", "image1.png","image/png", new byte[0]);
            MockMultipartFile image2 = new MockMultipartFile("images", "image2.png","image/png", new byte[0]);

            given(imageProviderService.uploadImages(anyList(), anyString())).willReturn(List.of("room/image1.png", "room/image2.png"));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders
                            .multipart("/api/rooms")
                            .file(image1)
                            .file(image2)
                            .param("num", "1")
                            .param("doubleBeds", "2")
                            .param("maxCapacity", "4")
                            .header("X-UserId", ownerId)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.num").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ownerId").value(ownerId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.doubleBeds").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.maxCapacity").value(4))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.simpleBeds").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mediumBeds").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.images.size()").value(2))
                    .andReturn();

            String idRoomCreated = JsonPath.parse(result.getResponse().getContentAsString()).read("$.id");
            Room roomCreated = roomRepository.findById(UUID.fromString(idRoomCreated)).orElseThrow(()-> new AssertionFailure("Not found room in database"));
            assertThat(roomCreated.getImages()).hasSize(2);
            roomCreated.getImages().forEach(image -> {
                assertThat(image.getRoom().getId().toString()).isEqualTo(idRoomCreated);
            });
        }
    }

    @Nested
    class FindAll {

        @Test
        void findAllRooms() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/rooms"))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2));
        }

        @Test
        void findRoomsWithPagination() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/rooms")
                            .queryParam("page","2")
                            .queryParam("limit","1")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(rooms.get(1).getId().toString()));
        }

        @Test
        void findRoomsByBedsFilters() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/rooms")
                            .param("doubleBeds","2")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(rooms.get(1).getId().toString()));
        }

        @Test
        void findRoomsByMaxCapacity() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/rooms")
                            .param("maxCapacity","2")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(rooms.getFirst().getId().toString()));
        }

        @Test
        void findRoomsByOwnerId() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/rooms")
                            .param("ownerId",rooms.get(1).getOwnerId().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(rooms.get(1).getId().toString()));
        }
    }

    @Nested
    class FindById {
        @Test
        void findById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/rooms/"+ rooms.getFirst().getId().toString()))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(rooms.getFirst().getId().toString()));
        }

        @Test
        void errorWhenNotFoundRoomById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/rooms/"+ UUID.randomUUID()))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found room with id")));
        }
    }

    @Nested
    class Update {
        @Test
        void updateRoom() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .patch("/api/rooms/"+rooms.get(1).getId())
                    .param("simpleBeds", "1")
                    .param("description","Habitación matrinomial + un hijo")
                    .param("name","Matrimonial xl")
                    .param("maxCapacity", "3")
                    .header("X-UserId", rooms.get(1).getOwnerId().toString())
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(rooms.get(1).getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Matrimonial xl"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Habitación matrinomial + un hijo"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.simpleBeds").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.maxCapacity").value(3));
        }

        @Test
        void updateImagesOfRoom() throws Exception {
            MockMultipartFile newImage1 = new MockMultipartFile("newImages", "newImage1.png", "image/png", new byte[0]);
            MockMultipartFile newImage2 = new MockMultipartFile("newImages", "newImage2.png", "image/png", new byte[0]);

            String imageToDelete = rooms.getFirst().getImages().getFirst().getUrl();

            given(imageProviderService.uploadImages(anyList(), anyString())).willReturn(List.of("rooms/" + rooms.getFirst().getId() + "/newImage1.png", "rooms/" + rooms.getFirst().getId() + "/newImage2.png"));
            given(imageProviderService.deleteImages(anyList())).willReturn(true);

            mockMvc.perform(MockMvcRequestBuilders
                            .multipart("/api/rooms/" + rooms.getFirst().getId())
                            .file(newImage1)
                            .file(newImage2)
                            .param("imagesToDelete", imageToDelete)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                            .header("X-UserId", rooms.getFirst().getOwnerId().toString())
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(rooms.getFirst().getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.images.size()").value(3));

            List<Image> images = imageRepository.findByRoom(rooms.getFirst());
            assertThat(images).hasSize(3);
            assertThat(images.getFirst().getUrl()).isEqualTo(rooms.getFirst().getImages().get(1).getUrl());
            assertThat(images.get(1).getUrl()).isEqualTo("rooms/" + rooms.getFirst().getId() + "/" + newImage1.getOriginalFilename());
            assertThat(images.get(2).getUrl()).isEqualTo("rooms/" + rooms.getFirst().getId() + "/" + newImage2.getOriginalFilename());

            imageRepository.findById(rooms.getFirst().getImages().getFirst().getId()).ifPresent(image-> {
                throw new AssertionFailure("Image should not be in DB");
            });
        }
    }

    @Nested
    class Delete {
        @Test
        void deleteRoom() throws Exception {
            given(imageProviderService.deleteFolder(anyString())).willReturn(true);

            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/rooms/" + rooms.getFirst().getId())
                            .header("X-UserId", rooms.getFirst().getOwnerId().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ok").value(true));

            roomRepository.findById(rooms.getFirst().getId()).ifPresent((room)->{
                throw new AssertionFailure("Room not deleted from DB");
            });
        }

        @Test
        void errorWhenNotFoundRoomById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/rooms/" + UUID.randomUUID())
                            .header("X-UserId", rooms.getFirst().getOwnerId().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message",Matchers.containsString("Not found room by id")));

            roomRepository.findById(rooms.getFirst().getId()).orElseThrow(()-> new AssertionFailure("Room deleted on error"));
        }
    }
}
