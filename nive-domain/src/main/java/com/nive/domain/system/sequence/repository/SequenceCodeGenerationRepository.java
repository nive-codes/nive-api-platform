package com.nive.domain.system.sequence.repository;

import com.nive.domain.system.sequence.SequenceCodeGeneration;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author nive
 * @class SequenceCodeGenerationRepository
 * @desc 시퀀스 생성하는 repository
 * @since 2025-06-25
 */
@Repository
public interface SequenceCodeGenerationRepository extends JpaRepository<SequenceCodeGeneration, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cs FROM SequenceCodeGeneration cs WHERE cs.nameKey = :nameKey AND cs.sequenceDate = :seqDate")
    Optional<SequenceCodeGeneration> findWithLock(@Param("nameKey") String nameKey, @Param("seqDate") String seqDate);
}
