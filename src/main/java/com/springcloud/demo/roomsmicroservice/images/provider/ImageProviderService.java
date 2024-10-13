package com.springcloud.demo.roomsmicroservice.images.provider;

import com.springcloud.demo.roomsmicroservice.exceptions.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageProviderService {
    public List<String> uploadImages(List<MultipartFile> files, String folder) throws BadRequestException;
    public boolean deleteImages(List<String> images);
    public boolean deleteFolder(String folder);
}
