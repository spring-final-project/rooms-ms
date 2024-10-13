package com.springcloud.demo.roomsmicroservice.images.service;

import com.springcloud.demo.roomsmicroservice.images.model.Image;
import com.springcloud.demo.roomsmicroservice.images.provider.ImageProviderService;
import com.springcloud.demo.roomsmicroservice.images.repository.ImageRepository;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageProviderService imageProviderService;

    public List<Image> createImages(List<MultipartFile> images, Room room) {
        List<String> urlImagesSaved = imageProviderService.uploadImages(images, "rooms/" + room.getId());

        return urlImagesSaved
                .stream()
                .map(url -> Image
                        .builder()
                        .url(url)
                        .room(room)
                        .build()
                )
                .toList();
    }

    public void saveImages(List<Image> images) {
        imageRepository.saveAll(images);
    }

    public void deleteImages(List<String> imagesToDelete) {
        imageProviderService.deleteImages(imagesToDelete);
    }

    public  void deleteFolder(String folder) {
        imageProviderService.deleteFolder(folder);
    }
}
