package com.nive.web.config.file;

import com.nive.web.config.properties.FileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nive
 * @class S3ClientConfig
 * @desc 버킷 설정마다 맞는 s3client를 Map의 형태로 가지고 bean을 만드는 형태.
 * @since 2025-04-24
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class S3ClientConfig {

    private final FileProperties fileProperties;

    @Bean
    public Map<String, S3Client> s3ClientMap() {
        Map<String, S3Client> map = new HashMap<>();
        Map<String, FileProperties.S3.Bucket> buckets = fileProperties.getS3().getBuckets();

        for (Map.Entry<String, FileProperties.S3.Bucket> entry : buckets.entrySet()) {
            String bucketKey = entry.getKey();
            FileProperties.S3.Bucket bucket = entry.getValue();

            S3Client s3Client;
            if (bucket.getAccessKey() != null && bucket.getSecretKey() != null) {
                log.info("[S3ClientConfig] [{}] accessKey 기반 인증 사용", bucketKey);
                s3Client = S3Client.builder()
                        .region(Region.of(bucket.getRegion()))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(bucket.getAccessKey(), bucket.getSecretKey())
                                )
                        )
                        .build();
            } else {
                log.info("[S3ClientConfig] [{}] IAM Role 기반 인증 사용", bucketKey);
                s3Client = S3Client.builder()
                        .region(Region.of(bucket.getRegion()))
                        .credentialsProvider(DefaultCredentialsProvider.create()) // EC2 Role, 환경 변수 등 순차 탐색
                        .build();
            }

            map.put(bucketKey, s3Client);
        }

        return map;
    }

}