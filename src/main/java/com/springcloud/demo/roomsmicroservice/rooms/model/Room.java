package com.springcloud.demo.roomsmicroservice.rooms.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.springcloud.demo.roomsmicroservice.images.model.Image;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer num;

    private String name;

    private Integer floor;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    private String description;

    @Column(name = "simple_beds")
    private Integer simpleBeds;

    @Column(name = "medium_beds")
    private Integer mediumBeds;

    @Column(name = "double_beds")
    private Integer doubleBeds;

    @Column(name = "rating_average")
    private Integer ratingAverage;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    // Relation with user microservice
    @Column(name = "owner_id")
    private UUID ownerId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "room", targetEntity = Image.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Image> images;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "last_updated")
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

}
