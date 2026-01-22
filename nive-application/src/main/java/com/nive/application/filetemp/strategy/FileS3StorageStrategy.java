package com.nive.application.filetemp.strategy;

import com.nive.common.response.ErrorCode;
import com.nive.common.response.LogLevel;
import com.nive.application.port.FilePropertiesPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.nive.common.exception.BusinessRestException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * @author nive
 * @class FileS3StorageStrategy
 * @desc S3 환경 파일 저장 전략 구현체
 * @since 2025-04-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileS3StorageStrategy implements FileStorageStrategy {
    private final FilePropertiesPolicy filePropertiesPolicy;
    private final Map<String, S3Client> s3ClientMap;

    //s3 버킷 자체의 값을 가지고 온다.
    private FilePropertiesPolicy.S3.Bucket getBucket(String bucketKey) {
        return Optional.ofNullable(filePropertiesPolicy.getS3().getBuckets().get(bucketKey))
                .orElseThrow(() -> {
                    throw BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).message("S3 설정에 정의되지 않은 버킷 키: " + bucketKey).build();
                });
    };

//    private S3Client getS3Client(FileProperties.S3.Bucket bucket) {
//        return S3Client.builder()
//                .region(Region.of(bucket.getRegion()))
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create(bucket.getAccessKey(), bucket.getSecretKey())
//                ))
//                .build();
//    }

    @Override
    public String store(String bucketKey, MultipartFile file, String filePath, String fileName) {
        FilePropertiesPolicy.S3.Bucket bucket = getBucket(bucketKey);
//        String key = getStoragePathPrefix(bucketKey)+"/"+filePath + "/" + fileName;
        String key = Paths.get(getStoragePathPrefix(bucketKey), filePath, fileName)
                .toString()
                .replace("\\", "/"); // 윈도우 환경 대응

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            S3Client s3Client = s3ClientMap.get(bucketKey); //bean으로 등록된 s3client를 Map으로 등록했던걸 key로 가져와서 활용

//            getS3Client(bucket).putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return key;
        } catch (IOException e) {
            log.error("[파일 업로드] S3 저장 중 에러 발생", e);
            throw BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).message("파일 S3 저장 실패").data(e).build();
        }
    }

    @Override
    public boolean exists(String bucketKey, String fullPath) {
        FilePropertiesPolicy.S3.Bucket bucket = getBucket(bucketKey);
        String key = fullPath.startsWith("/") ? fullPath.substring(1) : fullPath;

        try {
            S3Client s3Client = s3ClientMap.get(bucketKey); //bean으로 등록된 s3client를 Map으로 등록했던걸 key로 가져와서 활용
//            getS3Client(bucket).headObject(b -> b.bucket(bucket.getBucketName()).key(key));
            s3Client.headObject(b -> b.bucket(bucket.getBucketName()).key(key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void delete(String bucketKey, String fullPath) {
        FilePropertiesPolicy.S3.Bucket bucket = getBucket(bucketKey);
        String key = fullPath.startsWith("/") ? fullPath.substring(1) : fullPath;

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket.getBucketName())
                    .key(key)
                    .build();
            S3Client s3Client = s3ClientMap.get(bucketKey); //bean으로 등록된 s3client를 Map으로 등록했던걸 key로 가져와서 활용
//            getS3Client(bucket).deleteObject(request);
            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).message("파일 S3 삭제 실패").data(e).build();
        }
    }

    @Override
    public String createThumbnail(String bucketKey, MultipartFile file, String filePath, String fileName) {
        FilePropertiesPolicy.S3.Bucket bucket = getBucket(bucketKey);
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage thumbnail = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            thumbnail.getGraphics().drawImage(originalImage, 0, 0, 300, 300, null);

            String thumbName = fileName.replace(".", "_thumb.");

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", os);
            byte[] imageBytes = os.toByteArray();

//            String key = getStoragePathPrefix(bucketKey)+"/"+filePath + "/" + thumbName;
            String key = Paths.get(getStoragePathPrefix(bucketKey), filePath, fileName)
                    .toString()
                    .replace("\\", "/"); // 윈도우 환경 대응

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket.getBucketName())
                    .key(key)
                    .contentType("image/jpeg")
                    .build();
            S3Client s3Client = s3ClientMap.get(bucketKey); //bean으로 등록된 s3client를 Map으로 등록했던걸 key로 가져와서 활용
            s3Client.putObject(request, RequestBody.fromBytes(imageBytes));
//            getS3Client(bucket).putObject(request, RequestBody.fromBytes(imageBytes));
            return "/" + key;

        } catch (IOException e) {
            log.error("[썸네일 생성] S3 저장 중 에러 발생: {}", fileName, e);
            throw BusinessRestException.builder(ErrorCode.INTERNAL_SERVER_ERROR).message("S3 썸네일 생성 실패").data(e).build();
        }
    }

    /**
     * S3 Presigned URL 발급
     *
     * @param bucket           대상 버킷 정보
     * @param filePath         파일 저장 디렉토리 (예: kyc/2025/07/10)
     * @param fileUploadName   저장된 파일명 (예: image123.jpg)
     * @param expirationMinutes Presigned URL 유효 시간 (분)
     * @return Presigned URL
     */
    public String getPresignedUrl(FilePropertiesPolicy.S3.Bucket bucket, String filePath, String fileUploadName, int expirationMinutes) {
        try {
            String region = bucket.getRegion();
            String bucketName = bucket.getBucketName();

            // 파일 경로 및 파일명 조합
            String basePath = Optional.ofNullable(filePath)
                    .map(p -> p.startsWith("/") ? p.substring(1) : p)
                    .orElseThrow(() -> {
                        log.error("[S3] [파일 업로드] 경로 오류");
                        throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, "파일 경로가 유효하지 않습니다.");
                    });

            if (fileUploadName == null || fileUploadName.isBlank()) {
                log.error("[S3] [파일 업로드] 파일 명 없음");
                throw new BusinessRestException(ErrorCode.VALIDATION_FAILED, "파일명이 유효하지 않습니다.");
            }

            String key = basePath.endsWith("/") ? basePath + fileUploadName : basePath + "/" + fileUploadName;

            // credentialsProvider 분기 처리
            AwsCredentialsProvider credentialsProvider = (
                    bucket.getAccessKey() != null && bucket.getSecretKey() != null
            )
                    ? StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(bucket.getAccessKey(), bucket.getSecretKey())
            )
                    : DefaultCredentialsProvider.create();

            S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

            log.info("[Presigned URL 발급] key={}, duration={}초, expiresAt={}", key, expirationMinutes, LocalDateTime.now().plusMinutes(expirationMinutes));
            log.info("[Presigned URL 발급] pregisgnedRequest={}", presignedRequest.url().toString());

            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("[Presigned URL 발급 실패] filePath={}, fileName={}, duration={}분", filePath, fileUploadName, expirationMinutes, e);
            throw new BusinessRestException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Presigned URL 생성 실패했습니다.",
                    LogLevel.ERROR
            );
        }
    }

    @Override
    public String getAccessUrl(String bucketKey, String filePath, String fileUploadName) {
        FilePropertiesPolicy.S3.Bucket bucket = getBucket(bucketKey);
        if("personal".equals(bucketKey)){
//            log.info("[개인정보] [파일 정적 접근] [금지]");
//            throw new BusinessRestException(ErrorCode.UNAUTHORIZED,"error.not_found.file", LogLevel.INFO);
            return getPresignedUrl(bucket,filePath,fileUploadName,1);
        }else{
            // filePath가 null이거나 "/"로 끝나지 않는 경우를 고려

            String normalizedPath = filePath == null ? "" : filePath.endsWith("/") ? filePath : filePath + "/";
            if(StringUtils.hasText(bucket.getCloudFrontUrl())){
                return bucket.getCloudFrontUrl()+"/"+normalizedPath + fileUploadName;
            }

            // 전체 객체 키 구성
            String fullKey = normalizedPath + fileUploadName;

            // S3 퍼블릭 URL 구성
            return "https://" + bucket.getBucketName() + ".s3." + bucket.getRegion() + ".amazonaws.com/" + fullKey;
        }

    }

    @Override
    public String getRepositoryCode() {
        return "S3";
    }

    @Override
    public String getStoragePathPrefix(String bucketKey) {
        return filePropertiesPolicy.getS3().getBuckets().get(bucketKey).getPathPrefix();
    }


    @Override
    public Resource getResource(String bucketKey, String filePath, String fileUploadName) {
        FilePropertiesPolicy.S3.Bucket bucket = getBucket(bucketKey);
        S3Client client = s3ClientMap.get(bucketKey);
        try {
            String bucketName = bucket.getBucketName();

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            ResponseInputStream<GetObjectResponse> inputStream = client.getObject(request);
            return new InputStreamResource(inputStream);
        } catch (NoSuchKeyException e) {
            throw new BusinessRestException(ErrorCode.NOT_FOUND, "S3 파일 없음");
        } catch (Exception e) {
            throw new BusinessRestException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 파일 스트리밍 오류");
        }
    }
}
