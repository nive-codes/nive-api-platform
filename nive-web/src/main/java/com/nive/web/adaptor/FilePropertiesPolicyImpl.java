package com.nive.web.adaptor;

import com.nive.application.port.FilePropertiesPolicy;
import com.nive.web.config.properties.FileProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hosikchoi
 * @class FilePropertiesPolicyImpl
 * @desc application의 파일 업로드에 쓰기 위한 adaptor
 * @since 2026-01-06
 */
@Component
public class FilePropertiesPolicyImpl implements FilePropertiesPolicy {
    private final FileProperties properties;

    public FilePropertiesPolicyImpl(FileProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getDefaultStorage() {
        return properties.getDefaultStorage();
    }

    @Override
    public Local getLocal() {
        return new LocalImpl(properties.getLocal());
    }

    @Override
    public S3 getS3() {
        return new S3Impl(properties.getS3());
    }

    @Override
    public Thumbnail getThumbnail() {
        return new ThumbnailImpl(properties.getThumbnail());
    }

    /* ===================== Local ===================== */

    private static class LocalImpl implements Local {
        private final FileProperties.Local local;

        private LocalImpl(FileProperties.Local local) {
            this.local = local;
        }

        @Override
        public String getPath() {
            return local.getPath();
        }

        @Override
        public String getPathPrefix() {
            return local.getPathPrefix();
        }
    }

    /* ===================== S3 ===================== */

    private static class S3Impl implements S3 {
        private final FileProperties.S3 s3;

        private S3Impl(FileProperties.S3 s3) {
            this.s3 = s3;
        }

        @Override
        public String getDefaultBucket() {
            return s3.getDefaultBucket();
        }

        @Override
        public Map<String, Bucket> getBuckets() {
            Map<String, Bucket> result = new HashMap<>();
            if (s3.getBuckets() == null) {
                return result;
            }

            for (Map.Entry<String, FileProperties.S3.Bucket> entry : s3.getBuckets().entrySet()) {
                result.put(entry.getKey(), new BucketImpl(entry.getValue()));
            }
            return result;
        }
    }

    private static class BucketImpl implements S3.Bucket {
        private final FileProperties.S3.Bucket bucket;

        private BucketImpl(FileProperties.S3.Bucket bucket) {
            this.bucket = bucket;
        }

        @Override
        public String getBucketName() {
            return bucket.getBucketName();
        }

        @Override
        public String getRegion() {
            return bucket.getRegion();
        }

        @Override
        public String getAccessKey() {
            return bucket.getAccessKey();
        }

        @Override
        public String getSecretKey() {
            return bucket.getSecretKey();
        }

        @Override
        public String getCloudFrontUrl() {
            return bucket.getCloudFrontUrl();
        }

        @Override
        public String getPathPrefix() {
            return bucket.getPathPrefix();
        }
    }

    /* ===================== Thumbnail ===================== */

    private static class ThumbnailImpl implements Thumbnail {
        private final FileProperties.Thumbnail thumbnail;

        private ThumbnailImpl(FileProperties.Thumbnail thumbnail) {
            this.thumbnail = thumbnail;
        }

        @Override
        public String getSuffix() {
            return thumbnail.getSuffix();
        }

        @Override
        public int getWidth() {
            return thumbnail.getWidth();
        }

        @Override
        public int getHeight() {
            return thumbnail.getHeight();
        }
    }
}
