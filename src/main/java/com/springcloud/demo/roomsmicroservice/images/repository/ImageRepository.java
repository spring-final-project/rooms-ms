package com.springcloud.demo.roomsmicroservice.images.repository;

import com.springcloud.demo.roomsmicroservice.images.model.Image;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    List<Image> findByRoom(Room room);
}
