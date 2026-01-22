package com.nive.application.filetemp.policy;

import com.nive.application.filetemp.dto.FilePolicyDto;
import com.nive.application.filetemp.dto.CoreFileTransferResult;
import com.nive.domain.system.filetemp.FileTemp;
import com.nive.domain.system.filetemp.enums.BaseFileGroup;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * @author nive
 * @class CoreFilePolicy
 * @desc 각 도메인/모듈에서 파일을 어떻게 다뤄야 하는지에 대한 공통 규칙(Policy)을 강제하는 계약(Contract)
 * @since 2025-04-25
 */
public interface CoreFilePolicy<T> {
    /**
     * 파일 정책을 가져오는 모듈
     * 예)
     * FilePolicyDto policy = FilePolicyDto.builder()
     *         .fileModule("banner")
     *         .fileRepositoryType(FileRepositoryType.S3)
     *         .bucketKey("main")
     *         .fileGroup(BannerFileGroup.MAIN.getCode())
     *         .filePolicyType(BannerFilePolicyType.IMAGE)
     *         .maxFileSize(10 * 1024 * 1024L)
     *         .maxFileCount(1)
     *         .requiredMinFileCount(1)
     *         .build();
     * return List.of(policy);
     * @return
     */
    public List<FilePolicyDto> getPolicy();

    /**
     * 파일 id 내 같은 그룹(fileGroup) 전체 조회
     *
     * 예)
     * bannerId 기준 전체 파일 조회
     * return bannerFileRepository.findByBannerIdAndFileGroupAndDeletedFalseOrderBySortOrderAsc(id, bannerFileGroup.getCode());
     * @param id
     * @param baseFileGroup
     * @return
     */
    public List<?> selectFiles(Long id, BaseFileGroup baseFileGroup);

    /**
     * 특정 도메인 엔티티(id)에 속한 모든 파일 조회
     * 예)
     * return bannerFileRepository.findAllByBannerIdAndDeletedFalseOrderByFileGroupAscSortOrderAscIdDesc(id);
     * @param id
     * @return
     */
    public List<?> selectFiles(Long id);

    /**
     * 파일 transfer 처리
     * 기본 흐름 예시:
     *  1. FileTempValidate() 호출 (검통 검증)
     *  2. 도메인 파일 entity 생성
     *  3. 도메인 파일 저장
     *  4. tempFile Valid 처리(이관 통과)
     * 예)
     * FileTemp fileTemp = fileTempValidate(tempId, moduleId, ipAddr);
     *
     *    BannerFile transfer = BannerFile.transfer(fileTemp.getFileGroup(), fileTemp.getFileRepositoryType(), fileTemp.getBucketKey(),
     *            fileTemp.getFileUploadName(), fileTemp.getFileOriginName(), fileTemp.getSortOrder(), fileTemp.getFileSize(), fileTemp.getFilePath(),
     *            ipAddr, moduleId);
     *
     *    BannerFile saved = bannerFileRepository.save(transfer);
     *
     *    fileTemp.markValid(ipAddr,saved.getId());
     *
     *    return new CoreFileTransferResult(saved.getId(), fileTemp.getFileGroup());
     * @param tempId
     * @param moduleId
     * @param ipAddr
     * @return
     */
    public CoreFileTransferResult fileTransfer(Long tempId, Long moduleId, String ipAddr);

    /**
     * temp 파일 검증(정책 검증)
     * @param tempId
     * @param moduleId
     * @param ipAddress
     */
    public FileTemp fileTempValidate(Long tempId, Long moduleId,  String ipAddress);

    /**
     * 이관 시 파일 업로드 개수 체크
     * 예)
     *    int currentCount = bannerFileRepository.countByBannerIdAndFileGroupAndDeletedFalse(productId, fileGroup);
     *    if (isOverMaxFileCount(currentCount, maxFileCount)) {
     *        log.warn("[배너 파일] [이전] [파일 개수 초과] productId: {}, fileGroup: {}, currentCount: {}, maxAllowed: {}", productId, fileGroup, currentCount, maxFileCount);
     *        throw BusinessRestException.builder(ErrorCode.VALIDATION_FAILED).key("error.validation.over.file").data(fileGroup).build();
     *    }
     * @param id
     * @param fileGroup
     * @param maxFileCount
     */
    public void validateMaxFileCount(Long id, String fileGroup, int maxFileCount);

    /**
     * 이관 후 필수 파일 개수 체크
     * 예)
     *    int currentCount = bannerFileRepository.countByBannerIdAndFileGroupAndDeletedFalse(id, fileGroup);
     *
     *    // 필수로 올려야되는 개수가 더 큰 경우(같으면 통과 되어야함)
     *    if(requiredFileCount > currentCount){
     *        log.info("[배너 파일] [이전] [최소 등록 개수 미만] id : {}, fileGroup : {}, requiredFileCount : {}, currentCount : {}", id, fileGroup, requiredFileCount, currentCount);
     *        throw BusinessRestException.builder(ErrorCode.VALIDATION_FAILED).key("error.validation.required.file.count").data(fileGroup).logLevel(LogLevel.INFO).build();
     *    }
     * @param id
     * @param fileGroup
     * @param requiredFileCount
     */
    public void validateRequiredFileCount(Long id, String fileGroup, int requiredFileCount);

    /**
     * 기존 파일 물리 삭제 처리
     * 예)
     *    BannerFile byId = bannerFileRepository.findById(id).orElseThrow(() -> {
     *                log.warn("[배너 파일] [물리 삭제] [파일 없음] id : {},deletedBy : {} ", id, deletedBy);
     *                throw BusinessRestException.builder(ErrorCode.NOT_FOUND).key("error.not_found.file").data(id).logLevel(LogLevel.WARN).build();
     *            }
     *    );
     *    log.debug("[배너 파일] [물리 삭제] [파일 확인 완료] id : {}, productId : {}, deletedBy : {} ", byId.getId(), byId.getBannerId(), deletedBy);
     *    bannerFileRepository.delete(byId);
     *    log.debug("[배너 파일] [물리 삭제] [완료] id : {}, productId : {}, deletedBy : {} ", byId.getId(), byId.getBannerId(), deletedBy);
     * @param id
     * @param deletedBy
     */
    public void hardDelete(Long id, Long deletedBy);

    /**
     * 파일 정렬 변경
     * 예)
     *    BannerFile byId = bannerFileRepository.findById(id).orElseThrow(
     *            () ->{
     *                log.warn("[배너 파일] [정렬 변경] [파일 없음] id : {}", id);
     *                throw new BusinessRestException(ErrorCode.NOT_FOUND,"error.not_found.file");
     *            });
     *    byId.updateSortOrder(sortOrder, updatedIp);
     * @param id
     * @param sortOrder
     * @param updatedIp
     */
    public void updateSortOrder(Long id, int sortOrder, String updatedIp);


        /**
         * 업로드된 파일 메타 데이터 확장자 검증
         * @param originName
         * @return
         */
    default String extractExtension(String originName) {
        if (originName == null || !originName.contains(".")) {
            return null;
        }
        return originName.substring(originName.lastIndexOf('.') + 1).toLowerCase();
    }



    /**
     * 그룹 별 정책 찾기
     * @param policies
     * @param fileGroup
     * @return
     */
    default FilePolicyDto getPolicyValidate(List<FilePolicyDto> policies, String fileGroup) {
        return policies.stream()
                .filter(policy -> policy.getFileGroup().equals(fileGroup))
                .findFirst()
                .orElse(null);
    }

    /**
     * 파일 업로드 개수 체크 조건
     * @param currentCount
     * @param maxFileCount
     * @return
     */
    default boolean isOverMaxFileCount(int currentCount, int maxFileCount) {
        return currentCount >= maxFileCount;
    }


    /**
     * 그룹별로 파일 정책의 필수 count 처리
     * @param id
     */
    default void validateRequiredFileGroups(Long id){
        // 필수 업로드 파일 검증(그룹 별)
        List<FilePolicyDto> policies = getPolicy();
        for(FilePolicyDto dto :policies){
            if(dto.getRequiredMinFileCount() > 0 ){
                validateRequiredFileCount(id, dto.getFileGroup(), dto.getRequiredMinFileCount());
            }
        }
    }

    /**
     * 각 모듈 파일 스트리밍 경로
     * @param entity
     * @return
     */
    public String getStreamingUrl(T entity);

    /**
     * 각 파일 스트리밍 resource 생성
     * @param entity
     * @return
     */
    public Resource getFileResource(T entity) ;


}
