package com.springcloud.demo.roomsmicroservice.images.service;

import com.springcloud.demo.roomsmicroservice.images.model.Image;
import com.springcloud.demo.roomsmicroservice.images.provider.ImageProviderService;
import com.springcloud.demo.roomsmicroservice.images.repository.ImageRepository;
import com.springcloud.demo.roomsmicroservice.rooms.model.Room;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageProviderService imageProviderService;

    @InjectMocks
    ImageService imageService;

    @Nested
    class create {
        @Test
        void createImages(){
            MockMultipartFile image1 = new MockMultipartFile("images", "image1.png","image/png", new byte[0]);
            MockMultipartFile image2 = new MockMultipartFile("images", "image2.png","image/png", new byte[0]);
            MockMultipartFile image3 = new MockMultipartFile("images", "image3.png","image/png", new byte[0]);
            Room room = new Room();

            given(imageProviderService.uploadImages(anyList(), anyString())).willReturn(List.of("room/image1.png", "room/image2.png", "room/image3.png"));

            List<Image> imagesCreated =  imageService.createImages(List.of(image1,image2,image3), room);

            verify(imageProviderService).uploadImages(argThat(files -> files.contains(image1) && files.contains(image2) && files.contains(image3)), eq("rooms/" + room.getId()));

            assertThat(imagesCreated).hasSize(3);
            assertThat(imagesCreated.getFirst().getUrl()).isEqualTo("room/" + image1.getOriginalFilename());
            assertThat(imagesCreated.get(1).getUrl()).isEqualTo("room/" + image2.getOriginalFilename());
            assertThat(imagesCreated.get(2).getUrl()).isEqualTo("room/" + image3.getOriginalFilename());
            imagesCreated.forEach(image -> {
                assertThat(image.getRoom()).isEqualTo(room);
            });
        }
    }

    @Nested
    class Save {
        @Test
        void saveImages() {
            Image image1 = Image.builder().url("http://localhost/image1.png").room(new Room()).build();
            Image image2 = Image.builder().url("http://localhost/image2.png").room(new Room()).build();
            List<Image> images = List.of(image1, image2);

            given(imageRepository.saveAll(anyList())).willReturn(images);

            imageService.saveImages(images);
            verify(imageRepository).saveAll(images);
        }
    }

    @Nested
    class delete {
        @Test
        void deleteImages() {
            List<String> imagesToDelete = List.of("room/image1.png", "room/image2.png", "room/image3.png");
            imageService.deleteImages(imagesToDelete);
            verify(imageProviderService).deleteImages(imagesToDelete);
        }
    }

    @Nested
    class DeleteFolder {
        @Test
        void deleteFolder() {
            String roomId = UUID.randomUUID().toString();
            imageService.deleteFolder("rooms/" + roomId + "/");
            verify(imageProviderService).deleteFolder("rooms/" + roomId + "/");
        }
    }
}