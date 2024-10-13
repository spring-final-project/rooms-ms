package com.springcloud.demo.roomsmicroservice.rooms.repository;

import com.springcloud.demo.roomsmicroservice.rooms.dto.FilterRoomsDTO;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class RoomSpecifications {

    public Specification<Room> withFilters(FilterRoomsDTO filters) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.getSimpleBeds() != null) {
                predicates.add(builder.equal(root.get("simpleBeds"), filters.getSimpleBeds()));
            }
            if(filters.getMediumBeds() != null){
                predicates.add(builder.equal(root.get("mediumBeds"), filters.getMediumBeds()));
            }
            if(filters.getDoubleBeds() != null){
                predicates.add(builder.equal(root.get("doubleBeds"), filters.getDoubleBeds()));
            }
            if(filters.getMaxCapacity() != null){
                predicates.add(builder.equal(root.get("maxCapacity"), filters.getMaxCapacity()));
            }
            if(filters.getOwnerId() != null){
                predicates.add(builder.equal(root.get("ownerId"), UUID.fromString(filters.getOwnerId())));
            }

            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);

            return builder.and(predicatesArray);
        };
    }
}
