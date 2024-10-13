package com.springcloud.demo.roomsmicroservice.rooms.repository;

import com.springcloud.demo.roomsmicroservice.rooms.dto.FilterRoomsDTO;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.*;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class RoomSpecificationsTest {

    @InjectMocks
    private RoomSpecifications roomSpecifications;

    private Root<Room> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder builder;
    private FilterRoomsDTO filters;

    @BeforeEach
    public void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        builder = mock(CriteriaBuilder.class);
        filters = new FilterRoomsDTO();
    }

    @Test
    public void withNoFilters() {
        Specification<Room> spec = roomSpecifications.withFilters(filters);
        Predicate predicate = spec.toPredicate(root, query, builder);

        assertNull(predicate);
    }

    @Test
    public void withSimpleBedsFilter() {
        filters.setSimpleBeds(2);
        Predicate mockPredicate = mock(Predicate.class);
        given(builder.equal(root.get("simpleBeds"), 2)).willReturn(mockPredicate);

        roomSpecifications.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("simpleBeds"), 2);
    }

    @Test
    public void withMediumBedsFilter() {
        filters.setMediumBeds(1);
        Predicate mockPredicate = mock(Predicate.class);
        given(builder.equal(root.get("mediumBeds"), 1)).willReturn(mockPredicate);

        roomSpecifications.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("mediumBeds"), 1);
    }

    @Test
    public void withDoubleBedsFilter() {
        filters.setDoubleBeds(3);
        Predicate mockPredicate = mock(Predicate.class);
        given(builder.equal(root.get("doubleBeds"), 3)).willReturn(mockPredicate);

        roomSpecifications.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("doubleBeds"), 3);
    }

    @Test
    public void withMaxCapacityFilter() {
        filters.setMaxCapacity(5);
        Predicate mockPredicate = mock(Predicate.class);
        when(builder.equal(root.get("maxCapacity"), 5)).thenReturn(mockPredicate);

        roomSpecifications.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("maxCapacity"), 5);
    }

    @Test
    public void withOwnerIdFilter() {
        String ownerId = UUID.randomUUID().toString();
        filters.setOwnerId(ownerId);
        Predicate mockPredicate = mock(Predicate.class);
        when(builder.equal(root.get("ownerId"), UUID.fromString(ownerId))).thenReturn(mockPredicate);

        roomSpecifications.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("ownerId"), UUID.fromString(ownerId));
    }
}