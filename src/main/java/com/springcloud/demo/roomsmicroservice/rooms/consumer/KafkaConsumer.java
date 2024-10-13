package com.springcloud.demo.roomsmicroservice.rooms.consumer;

import com.springcloud.demo.roomsmicroservice.client.bookings.dto.BookingDTO;
import com.springcloud.demo.roomsmicroservice.rooms.service.RoomService;
import com.springcloud.demo.roomsmicroservice.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final RoomService roomService;

    @KafkaListener(topics = "${spring.kafka.topics.REVIEW_CREATED_TOPIC}")
    public void updateRatingEvent(String message) {
        BookingDTO bookingDTO = JsonUtils.fromJson(message, BookingDTO.class);
        roomService.updateRating(bookingDTO.getRoomId(), bookingDTO.getRating());
    }
}
