package com.springcloud.demo.roomsmicroservice.images.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.SA_EAST_1)
//                Toma las credenciales de la maquina o la instancia de EC2 en la que se ejecuta la aplicación
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
