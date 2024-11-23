package com.springcloud.demo.roomsmicroservice.images.provider;

import com.springcloud.demo.roomsmicroservice.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnExpression("!'${cloud.aws.s3.bucket.name}'.equals('filesystem')")
public class S3Provider implements ImageProviderService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket.name}")
    private String bucket;

    @Value("${cloud.aws.s3.region}")
    private String region;

    @Override
    public List<String> uploadImages(List<MultipartFile> files, String folder) {
        List<String> urlImagesSaved = new ArrayList<>();
        List<MultipartFile> filesRejected = new ArrayList<>();

        files.stream().map(file -> CompletableFuture.runAsync(() -> {
            String extension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
            String key = folder + "/" + UUID.randomUUID() + extension;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            try {
                PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

                if (response.sdkHttpResponse().isSuccessful()) {
                    urlImagesSaved.add("https://" + bucket + ".s3." + region + ".amazonaws.com/" + key);
                } else {
                    log.error("Error uploading file: {}", file.getOriginalFilename());
                    filesRejected.add(file);
                }

            } catch (IOException e) {
                log.error("Error uploading file: {}", file.getOriginalFilename());
                filesRejected.add(file);
            }

        })).forEach(CompletableFuture::join);

        if (!filesRejected.isEmpty()) {
            throw new BadRequestException("Error uploading files");
        }

        return urlImagesSaved;

    }

    @Override
    public boolean deleteImages(List<String> imagesKeys) {
        List<ObjectIdentifier> objects = imagesKeys.stream().map(key -> ObjectIdentifier.builder().key(key).build()).toList();

        Delete delete = Delete.builder().objects(objects).build();

        try {
            DeleteObjectsRequest multiObjectDelete = DeleteObjectsRequest.builder().bucket(bucket).delete(delete).build();
            DeleteObjectsResponse deleteObjectsResponse = s3Client.deleteObjects(multiObjectDelete);
            return deleteObjectsResponse.sdkHttpResponse().isSuccessful();

        } catch (Exception e) {
            log.error("Error deleting images");
            return false;
        }
    }

    @Override
    public boolean deleteFolder(String folderName) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(folderName)
                .build();

        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

        if (listObjectsV2Response.sdkHttpResponse().isSuccessful()) {
            this.deleteImages(listObjectsV2Response.contents().stream().map(S3Object::key).toList());
            return true;
        } else {
            log.error("Error deleting folder");
            return false;
        }
    }
}
