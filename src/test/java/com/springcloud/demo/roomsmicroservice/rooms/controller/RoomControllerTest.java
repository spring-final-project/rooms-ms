package com.springcloud.demo.roomsmicroservice.rooms.controller;

import com.springcloud.demo.roomsmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.roomsmicroservice.monitoring.TracingExceptions;
import com.springcloud.demo.roomsmicroservice.rooms.dto.CreateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.FilterRoomsDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.ResponseRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.UpdateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.service.RoomService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

@WebMvcTest
class
RoomControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RoomService roomService;

    @MockBean
    private TracingExceptions tracingExceptions;

    @Nested
    class Create {

        @Test
        void createRoom() throws Exception {
            String ownerId = UUID.randomUUID().toString();
            given(roomService.create(any(CreateRoomDTO.class), anyString())).willReturn(new ResponseRoomDTO());

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/rooms")
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .header("X-UserId", ownerId)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
            verify(roomService).create(any(CreateRoomDTO.class), anyString());
        }

        @Test
        void errorWhenNotExistOwnerId() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/rooms")
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString());
            verify(roomService, never()).create(any(CreateRoomDTO.class),anyString());
        }

        @Test
        void errorWhenAmountOfBedsIsNotValid() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/rooms")
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .param("doubleBeds", "-1")
                            .param("simpleBeds", "-1")
                            .param("mediumBeds", "-1")
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(3));
            verify(roomService, never()).create(any(CreateRoomDTO.class), anyString());
        }
    }

    @Nested
    class FindAll {

        @Test
        void findAllRooms() throws Exception {
            given(roomService.findAll(any(FilterRoomsDTO.class))).willReturn(List.of(new ResponseRoomDTO()));

            mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/rooms"))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
            verify(roomService).findAll(any(FilterRoomsDTO.class));
        }

        @Test
        void findAllRoomsBasedInNumOfBeds() throws Exception {
            given(roomService.findAll(any(FilterRoomsDTO.class))).willReturn(List.of(new ResponseRoomDTO()));

            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/rooms")
                            .param("doubleBeds", "2")
                            .param("simpleBeds", "1")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
            verify(roomService).findAll(argThat(dto -> dto.getDoubleBeds() == 2 && dto.getSimpleBeds() == 1));
        }

        @Test
        void findAllRoomsWithPagination() throws Exception {
            given(roomService.findAll(any(FilterRoomsDTO.class))).willReturn(List.of(new ResponseRoomDTO()));

            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/rooms")
                            .param("page", "2")
                            .param("limit", "10")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1));
            verify(roomService).findAll(argThat(dto -> dto.getPage() == 2 && dto.getLimit() == 10));
        }

        @Test
        void errorWhenParametersToFindAreNotValid() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/rooms")
                            .param("page", "-1")
                            .param("limit", "-10")
                            .param("doubleBeds", "-2")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(3));
            verify(roomService, never()).findAll(any(FilterRoomsDTO.class));
        }
    }

    @Nested
    class FindById {

        @Test
        void findRoomById() throws Exception {
            String idToFind = UUID.randomUUID().toString();
            given(roomService.findById(anyString())).willReturn(new ResponseRoomDTO());

            mockMvc.perform(MockMvcRequestBuilders.get("/api/rooms/" + idToFind))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
            verify(roomService).findById(idToFind);
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/rooms/abcde"))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(roomService, never()).findById(anyString());
        }
    }

    @Nested
    class Update {

        @Test
        void updateRoom() throws Exception {
            String idToUpdate = UUID.randomUUID().toString();
            given(roomService.update(anyString(), any(UpdateRoomDTO.class), anyString())).willReturn(new ResponseRoomDTO());

            mockMvc.perform(MockMvcRequestBuilders
                            .patch("/api/rooms/" + idToUpdate)
                            .param("doubleBeds", "3")
                            .param("maxCapacity", "6")
                            .header("X-UserId", UUID.randomUUID())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
            verify(roomService).update(eq(idToUpdate), argThat(dto -> dto.getDoubleBeds() == 3 && dto.getMaxCapacity() == 6), anyString());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .patch("/api/rooms/abcde")
                            .param("doubleBeds", "3")
                            .param("maxCapacity", "6")
                            .header("X-UserId", UUID.randomUUID())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(roomService, never()).update(anyString(), any(UpdateRoomDTO.class), anyString());
        }

        @Test
        void errorWhenParametersAreNotValid() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .patch("/api/rooms/" + UUID.randomUUID())
                            .param("doubleBeds", "-3")
                            .param("maxCapacity", "-1")
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(roomService, never()).update(anyString(), any(UpdateRoomDTO.class), anyString());
        }
    }

    @Nested
    class Delete {

        @Test
        void deleteRoom() throws Exception {
            String idToDelete = UUID.randomUUID().toString();
            given(roomService.delete(anyString(), anyString())).willReturn(new SimpleResponseDTO(true));

            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/rooms/" + idToDelete)
                            .header("X-UserId", UUID.randomUUID())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ok").value(true));
            verify(roomService).delete(eq(idToDelete), anyString());
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/rooms/abcde")
                            .header("X-UserId", UUID.randomUUID())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
            verify(roomService, never()).delete(anyString(), anyString());
        }
    }
}