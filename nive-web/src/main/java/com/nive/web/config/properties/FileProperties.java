package com.nive.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author nive
 * @class FileProperties
 * @desc 파일 yaml 설정 값을 파싱하는 properties
 *  * 파일 업로드 전략에 필요한 설정값을 외부 YAML에서 주입받는 프로퍼티 클래스
 *  * - 기본 전략 (defaultStorage)
 *  * - 로컬 저장소 설정 (Local)
 *  * - S3 저장소 설정 (S3: 다중 버킷 지원)
 *  * - 썸네일 옵션 설정 (Thumbnail)
 * @since 2025-04-24
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    private String defaultStorage;
    private Local local;
    private S3 s3;
    private Thumbnail thumbnail;


    @Data
    public static class Local {
        private String path;
        private String pathPrefix;  //uploads 고정으로 db에 저장될 경로 처리
    }

    @Data
    public static class S3 {
        private String defaultBucket;
        private Map<String, Bucket> buckets;

        @Data
        public static class Bucket {
            private String bucketName;
            private String region;
            private String accessKey;
            private String secretKey;
            private String cloudFrontUrl;
            private String pathPrefix;
        }
    }

    @Data
    public static class Thumbnail {
        private String suffix;
        private int width;
        private int height;
    }
}
