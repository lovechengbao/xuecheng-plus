package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConfigurationProperties("minio")
public class MinioConfig {
    @Value("${spring.minio.endpoint}")
    private String endpoint;
    @Value("${spring.minio.accessKey}")
    private String accessKey;
    @Value("${spring.minio.secretKey}")
    private String secretKey;


    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
