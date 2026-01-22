package com.nive.domain.system.filetemp.repository;

import com.nive.domain.system.filetemp.FileTemp;
import com.nive.domain.system.filetemp.enums.FileStatusCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author nive
 * @class FileTempRepository
 * @desc 임시 파일을 관리하는 repository
 * @since 2025-04-24
 */
@Repository
public interface FileTempRepository extends JpaRepository<FileTemp, Long> {
    List<FileTemp> findAllByFileStatusNotIn(Collection<FileStatusCode> fileStatuses);


    List<FileTemp> findAllByFileStatusAndExpireAtLessThanEqual(FileStatusCode fileStatus, LocalDateTime expireAtIsLessThan);

    List<FileTemp> findAllByExpireAtBefore(LocalDateTime expireAtBefore);

    List<FileTemp> findAllByFileStatusAndTransferAtIsNotNull(FileStatusCode fileStatus);

    List<FileTemp> findAllByFileStatusAndExpireAtBefore(FileStatusCode fileStatus, LocalDateTime expireAtBefore);
}
