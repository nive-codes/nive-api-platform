package com.nive.application.port;

import java.util.Map;

/**
 * @author hosikchoi
 * @class FilePropertiesPolicy
 * @desc [클래스 설명]
 * @since 2026-01-06
 */
public interface FilePropertiesPolicy {
    String getDefaultStorage();
    Local getLocal();
    S3 getS3();
    Thumbnail getThumbnail();

    interface Local {
        String getPath();
        String getPathPrefix();
    }

    interface S3 {
        String getDefaultBucket();
        Map<String, Bucket> getBuckets();

        interface Bucket {
            String getBucketName();
            String getRegion();
            String getAccessKey();
            String getSecretKey();
            String getCloudFrontUrl();
            String getPathPrefix();
        }
    }

    interface Thumbnail {
        String getSuffix();
        int getWidth();
        int getHeight();
    }
}
