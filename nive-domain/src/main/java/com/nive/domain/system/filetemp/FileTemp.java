package com.nive.domain.system.filetemp;

import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import com.nive.domain.system.filetemp.enums.FileStatusCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author nive
 * @class CommonFileTemp
 * @desc 임시 파일 정보 테이블
 * @since 2025-04-24
 */
// 필요한 경우에만 아래 주석 해제
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "common_file_temp")
public class FileTemp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_id")
    private Long id;

    @Column(name = "file_id", columnDefinition = "bigint comment '실제 이관된 파일 ID'")
    private Long fileId;

    @Column(name = "file_group", length = 100, nullable = false, columnDefinition = "varchar(100) comment '파일 그룹'")
    private String fileGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_repository_type", length = 100, nullable = false, columnDefinition = "varchar(100) comment '파일 저장소 (s3, local 등)'")
    private FileRepositoryType fileRepositoryType;

    @Column(name = "bucket_key", columnDefinition = "varchar(50) comment 'yml 명시 bucket key, 로컬인 경우 LOCAL'")
    private String bucketKey;

    @Column(name = "file_upload_name", length = 400, nullable = false, columnDefinition = "varchar(255) comment '업로드된 파일 이름'")
    private String fileUploadName;

    @Column(name = "file_origin_name", length = 255, nullable = false, columnDefinition = "varchar(255) comment '원본 파일 이름'")
    private String fileOriginName;

    @Column(name = "file_path", length = 1000, nullable = false, columnDefinition = "varchar(1000) comment '파일 저장 경로'")
    private String filePath;

    @Column(name = "file_module", length = 50, nullable = false, columnDefinition = "varchar(50) comment '업로드 요청한 모듈 정보'")
    private String fileModule;

    @Column(name = "sort_order", columnDefinition = "int default 1 comment '파일 순서'")
    private int sortOrder;

    @Column(name = "file_size", nullable = false, columnDefinition = "int comment '파일 크기'")
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_status", length = 20, nullable = false, columnDefinition = "varchar(20) default 'PENDING' comment '파일 상태 (PENDING, VALID, INVALID)'")
    private FileStatusCode fileStatus;

    @Column(name = "invalid_reason", length = 300, columnDefinition = "varchar(300) comment 'INVALID 사유'")
    private String invalidReason;

    @Column(name = "transfer_at", columnDefinition = "datetime comment '이관 시간'")
    private LocalDateTime transferAt;

    @Column(name = "expire_at", columnDefinition = "datetime comment '만료 시간'")
    private LocalDateTime expireAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false,nullable = false, columnDefinition = "datetime default current_timestamp comment '생성 일시'")
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 50, nullable = false, columnDefinition = "varchar(50) comment '생성자 ID'")
    private Long createdBy;

    @Column(name = "created_ip", length = 80, nullable = false, columnDefinition = "varchar(100) comment '생성자 IP 주소'")
    private String createdIp;

    @LastModifiedDate
    @Column(name = "updated_dt", columnDefinition = "datetime comment '수정 일시'", insertable = false)
    private LocalDateTime updatedDt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50, columnDefinition = "varchar(50) comment '수정자 ID'")
    private Long updatedBy;

    @Column(name = "updated_ip", length = 80, columnDefinition = "varchar(100) comment '수정자 IP 주소'")
    private String updatedIp;


    /**
     * 정적 생성 메서드
     * @param fileGroup
     * @param fileRepositoryType
     * @param fileUploadName
     * @param fileOriginName
     * @param filePath
     * @param fileModule
     * @param sortOrder
     * @param fileSize
     * @return
     */
    public static FileTemp create(
            String fileGroup,
            FileRepositoryType fileRepositoryType,
            String bucketKey,
            String fileUploadName,
            String fileOriginName,
            String filePath,
            String fileModule,
            int sortOrder,
            long fileSize,
            LocalDateTime expireAt,
            String createdIp
    ) {
        return FileTemp.builder()
                .fileGroup(fileGroup)
                .fileRepositoryType(fileRepositoryType)
                .bucketKey(bucketKey)
                .fileUploadName(fileUploadName)
                .fileOriginName(fileOriginName)
                .filePath(filePath)
                .fileModule(fileModule)
                .sortOrder(sortOrder)
                .fileSize(fileSize)
                .fileStatus(FileStatusCode.PENDING)
                .createdAt(LocalDateTime.now())
                .createdIp(createdIp)
                .expireAt(expireAt)
                .build();
    }


    /**
     * 검증 후 이관 처리
     * @param updatedIp
     */
    public void markValid(String updatedIp, Long transferFileId) {
        this.fileStatus = FileStatusCode.VALID;
        this.fileId = transferFileId;
        this.transferAt = LocalDateTime.now();
        this.updatedIp = updatedIp;

    }

    /**
     * 검증 후 invalid로 표시(이관X)
     * @param reason
     */
    public void markInvalid(String reason, String updatedIp) {
        this.fileStatus = FileStatusCode.INVALID;
        this.invalidReason = reason;
        this.updatedIp = updatedIp;
    }

    /**
     * 순서 변경
     * @param sortOrder
     * @param updatedIp
     */
    public void updateSortOrder(int sortOrder, String updatedIp) {
        this.sortOrder = sortOrder;
        this.updatedIp = updatedIp;
    }
}
