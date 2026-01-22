package com.nive.application.filetemp.strategy;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author nive
 * @class FileTempStorageStrategy
 * @desc 임시 파일 저장 전략 인터페이스 (Local / S3 등 확장 가능)
 * @since 2025-04-24
 */
public interface FileStorageStrategy {


    /**
     * 파일 저장 처리
     * @param bucketKey
     * @param file 업로드된 파일
     * @param filePath 저장 경로 (디렉토리)
     * @param fileName 저장 파일명
     * @return 저장된 전체 경로 (예: /2025/04/24/uuid.png)
     */
    String store(String bucketKey, MultipartFile file, String filePath, String fileName);

    /**
     * 파일 저장 확인
     * @param fullPath
     * @return
     */
    boolean exists(String bucketKey, String fullPath);

    /**
     * 저장된 파일 삭제 처리
     * @param bucketKey
     * @param fullPath 전체 경로 (예: /2025/04/24/uuid.png)
     */
    void delete(String bucketKey, String fullPath);

    /**
     * 썸네일 생성 처리
     * @param bucketKey
     * @param file
     * @param filePath
     * @param fileName
     * @return
     */
    String createThumbnail(String bucketKey, MultipartFile file, String filePath, String fileName);




    /**
     * 파일 URL 조회용 (S3일 경우 public URL, Local일 경우 /static 경로 등)
     * @param bucketKey
     * @param filePath
     * @param fileName
     * @return
     */
    String getAccessUrl(String bucketKey, String filePath, String fileName);

    /**
     * 저장소 타입명 (예: LOCAL, S3 등) -> FileStorageStrategyConfig에서  map으로 관리됩니다.
     */
    String getRepositoryCode();


    /**
     * 저장소 별 접근 경로 prefix 반환 (예 : 로컬인 경우 /uploads)
     * 저장 및 조회에 쓰이며, 삭제 등은 이미 db에 저장이 되어 있으므로 활용되지 않는다 [주의]
     * @param bucketKey
     * @return
     */
    String getStoragePathPrefix(String bucketKey);

    /**
     * 파일 스트리밍용(개인정보 파일 등 경로 유출 방지용)
     * @param bucketKey
     * @param filePath
     * @return
     */
    Resource getResource(String bucketKey, String filePath, String fileUploadName);
}
