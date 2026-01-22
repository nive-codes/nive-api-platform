package com.nive.application.sequence.support;

import com.nive.domain.system.sequence.SequenceCodeGeneration;
import com.nive.domain.system.sequence.SequenceNameKey;
import com.nive.domain.system.sequence.repository.SequenceCodeGenerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author nive
 * @class SequenceCodeGenerator
 * @desc 각 모듈 + 날짜 별 시퀀스 생성 Service - egov 참조
 * @since 2025-06-25
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SequenceCodeGenerator {
  private final SequenceCodeGenerationRepository sequenceCodeGenerationRepository;

  /**
   * 코드 생성
   * 예: O-20250625-00001
   * @param nameKey enum 값
   * @return prefix + yyyyMMdd + "-" + 5자리 시퀀스
   */
  @Transactional
  public String generate(SequenceNameKey nameKey) {
      LocalDate date = LocalDate.now();
      String seqDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
      String prefix = nameKey.getStrategyPrefix();

      try {

          SequenceCodeGeneration sequenceCodeGeneration = sequenceCodeGenerationRepository.findWithLock(nameKey.name(), seqDate)
                  .map(seq -> {
                      seq.increment();
                      return seq;
                  })
                  .orElseGet(() -> SequenceCodeGeneration.create(nameKey.name(), seqDate, 1));

          sequenceCodeGenerationRepository.save(sequenceCodeGeneration);

          String formattedSeq = String.format("%05d", sequenceCodeGeneration.getSequenceValue());

          return prefix + seqDate + "-" + formattedSeq;
      }catch (Exception e) {
          log.error("[CodeGen] 코드 생성 중 예외 발생: nameKey={}, message={}", nameKey, e.getMessage(), e);
          // fallback 랜덤코드 예: O-20250625-X4A9TQ
          // 2. 밀리초 기반의 순차적인 값 (밀리초까지 반영하여 고유성 보장)
          String sequence = String.format("%06d", System.currentTimeMillis() % 1000000); // 마지막 6자리

          // 3. UUID 일부를 사용 (첫 8자리 정도를 가져오기)
          String uuidPart = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
          String seq = prefix + seqDate + "-X" + uuidPart;
          log.warn("[CodeGen:FALLBACK] 랜덤코드 생성됨: fallbackCode={}, nameKey={}, cause={}", seq, nameKey, e.toString());
          return seq;
      }
  }
}
