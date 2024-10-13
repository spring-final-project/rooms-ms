package com.springcloud.demo.roomsmicroservice.images.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "images")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String url;

    @ManyToOne
    @JoinColumn(name = "room_id")
    @JsonBackReference
    private Room room;
}
