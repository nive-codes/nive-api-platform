package com.nive.application.filetemp.usecase;

import com.nive.application.filetemp.strategy.FileStorageStrategy;
import com.nive.domain.system.filetemp.enums.FileRepositoryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author nive
 * @class GetFileAccessUrlResolverUseCase
 * @desc 파일 경로를 전략패턴에 맞도록 체크 후 파일 url 내려주는 usecase 클래스
 * @since 2025-07-02
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetFileAccessUrlResolverUseCase {
    private final Map<String, FileStorageStrategy> strategyMap;

    /**
     * 정적 파일 리졸버 처리
     * @param repositoryType
     * @param bucketKey
     * @param filePath
     * @param fileUploadName
     * @return
     */
    public String resolve(FileRepositoryType repositoryType, String bucketKey, String filePath, String fileUploadName) {

      if (repositoryType == null) {
          log.warn("[파일 리졸버] [레포지토리 타입 없음] : bucketKey: {}, filePath: {}, fileUploadName: {}", bucketKey, filePath, fileUploadName);
          return null; // 또는 기본 이미지 URL 반환 가능
      }

      FileStorageStrategy strategy = strategyMap.get(repositoryType.name());

      if (strategy == null) {
          log.warn("[파일 리졸버] [전략 없음] : respositoryType : {}, bucketKey : {}, filePath : {}, fileUploadName : {}",repositoryType.name(),bucketKey,filePath,fileUploadName);
          return null;
//        throw new IllegalArgumentException("지원하지 않는 저장소 타입입니다: " + repositoryType);
      }

      return strategy.getAccessUrl(bucketKey, filePath, fileUploadName);
    }

}
