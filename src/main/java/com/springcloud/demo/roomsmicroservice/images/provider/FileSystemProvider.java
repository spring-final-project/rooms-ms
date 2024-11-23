package com.springcloud.demo.roomsmicroservice.images.provider;

import com.springcloud.demo.roomsmicroservice.exceptions.BadRequestException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@ConditionalOnExpression("'${cloud.aws.s3.bucket.name}'.equals('filesystem')")
public class FileSystemProvider implements ImageProviderService {
    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) throws BadRequestException {
        List<String> urlImagesSaved = new ArrayList<>();
        Path folderPath = Paths.get("images").resolve(folder);

        try {
            Files.createDirectories(folderPath);
            for (MultipartFile file : files) {
                String extension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
                String key = UUID.randomUUID() + extension;
                Path targetPath = folderPath.resolve(key);
                Files.write(targetPath, file.getBytes());
                urlImagesSaved.add("http://localhost:8080/" + folder + "/" + key);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Error uploading files");
        }
        return urlImagesSaved;
    }

    @Override
    public boolean deleteImages(List<String> images) {
        for (String imageUrl : images) {
            String imagePath = imageUrl.substring(imageUrl.indexOf("/", 10) + 1);
            try {
                Path fullPath = Paths.get("images").resolve(imagePath);
                Files.deleteIfExists(fullPath);
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean deleteFolder(String folder) {
        Path folderPath = Paths.get("images").resolve(folder);
        try {
            Files.walk(folderPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
