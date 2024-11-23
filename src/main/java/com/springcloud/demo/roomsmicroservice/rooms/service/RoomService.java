package com.springcloud.demo.roomsmicroservice.rooms.service;

import com.springcloud.demo.roomsmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.roomsmicroservice.exceptions.BadRequestException;
import com.springcloud.demo.roomsmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.roomsmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.roomsmicroservice.images.model.Image;
import com.springcloud.demo.roomsmicroservice.images.service.ImageService;
import com.springcloud.demo.roomsmicroservice.rooms.dto.CreateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.FilterRoomsDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.ResponseRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.dto.UpdateRoomDTO;
import com.springcloud.demo.roomsmicroservice.rooms.mapper.RoomMapper;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import com.springcloud.demo.roomsmicroservice.rooms.repository.RoomRepository;
import com.springcloud.demo.roomsmicroservice.rooms.repository.RoomSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomSpecifications roomSpecifications;
    private final ImageService imageService;


    public ResponseRoomDTO create(CreateRoomDTO createRoomDTO, String idUserLogged) {
        if (createRoomDTO.getName() == null && createRoomDTO.getNum() == null) {
            throw new BadRequestException("Number or name must be specify");
        }

        Room room = RoomMapper.createRoomDtoToRoom(createRoomDTO);
        room.setOwnerId(UUID.fromString(idUserLogged));
        room.setImages(new ArrayList<>());

        room = roomRepository.save(room);

        if (!createRoomDTO.getImages().isEmpty()) {
            List<Image> imagesSaved = imageService.createImages(createRoomDTO.getImages(), room);
            imageService.saveImages(imagesSaved);
            room.setImages(imagesSaved);
        }

        return RoomMapper.roomToResponseRoomDto(room);
    }

    public List<ResponseRoomDTO> findAll(FilterRoomsDTO filters) {

        int page = filters.getPage() - 1;
        int limit = Math.min(filters.getLimit(), 50);
        Pageable pageable = PageRequest.of(page, limit);

        List<Room> rooms = roomRepository.findAll(roomSpecifications.withFilters(filters), pageable).getContent();

        return rooms.stream().map(RoomMapper::roomToResponseRoomDto).toList();
    }

    public ResponseRoomDTO findById(String id) {
        Room room = roomRepository
                .findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Not found room with id: " + id));

        return RoomMapper.roomToResponseRoomDto(room);
    }

    public ResponseRoomDTO update(String id, UpdateRoomDTO updateRoomDTO, String idUserLogged) {
        Room room = roomRepository
                .findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Not found room by id: " + id));

        if(!room.getOwnerId().toString().equals(idUserLogged)){
            throw new ForbiddenException("Not have permission to update a room that belong to another user");
        }

        Room updatedRoom = RoomMapper.roomToRoomUpdated(room, updateRoomDTO);

//        Delete images
        if (updateRoomDTO.getImagesToDelete() != null && !updateRoomDTO.getImagesToDelete().isEmpty()) {
            imageService.deleteImages(updateRoomDTO.getImagesToDelete());
            updatedRoom.setImages(
                    updatedRoom
                            .getImages()
                            .stream()
                            .filter(image -> !updateRoomDTO.getImagesToDelete().contains(image.getUrl()))
                            .toList()
            );
        }

//        Add images
        if (updateRoomDTO.getNewImages() != null && !updateRoomDTO.getNewImages().isEmpty()) {
            List<Image> newImages = imageService.createImages(updateRoomDTO.getNewImages(), updatedRoom);
            List<Image> concatImages = new ArrayList<>();
            concatImages.addAll(updatedRoom.getImages());
            concatImages.addAll(newImages);
            updatedRoom.setImages(concatImages);
        }

        updatedRoom = roomRepository.save(updatedRoom);

        return RoomMapper.roomToResponseRoomDto(updatedRoom);
    }

    public void updateRating(String id, int rating) {
        Optional<Room> roomFound = roomRepository
                .findById(UUID.fromString(id));

        roomFound.ifPresent(room -> {
            if(room.getTotalReviews() == 0){
                room.setTotalReviews(1);
                room.setRatingAverage(rating);
            } else {
                room.setRatingAverage((room.getRatingAverage() * room.getTotalReviews() + rating) / (room.getTotalReviews() + 1));
                room.setTotalReviews(room.getTotalReviews() + 1);
            }
                roomRepository.save(room);
        });
    }

    public SimpleResponseDTO delete(String id, String idUserLogged) {
        Room room = roomRepository
                .findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Not found room by id: " + id));

        System.out.println("ownerId = " + room.getOwnerId());
        if(!room.getOwnerId().toString().equals(idUserLogged)){
            throw new ForbiddenException("Not have permission to delete room that belong to another user");
        }

        imageService.deleteFolder("rooms/" + room.getId() + "/");

        roomRepository.delete(room);

        return new SimpleResponseDTO(true);
    }
}
