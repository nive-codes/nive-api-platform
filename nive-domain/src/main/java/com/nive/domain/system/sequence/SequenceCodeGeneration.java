package com.nive.domain.system.sequence;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author nive
 * @class SequenceCodeGeneration
 * @desc 시퀀스를 생성하는 도메인
 * @since 2025-06-25
 */
//@EntityListeners(AuditingEntityListener.class) //필요없는 경우 주석(자동 created, updated 처리)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sequence_code_generation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_name_date", columnNames = {"name_key", "sequence_date"})
        })
public class SequenceCodeGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 관리용 ID

    @Column(name = "name_key", nullable = false, length = 50)
    private String nameKey;

    @Column(name = "sequence_date", nullable = false, length = 8)
    private String sequenceDate;

    @Column(name = "sequence_value", nullable = false)
    private Integer sequenceValue;

    public static SequenceCodeGeneration create(String nameKey, String sequenceDate, Integer sequenceValue) {
        return SequenceCodeGeneration.builder()
                .nameKey(nameKey)
                .sequenceDate(sequenceDate)
                .sequenceValue(sequenceValue)
                .build();
    }

    public void increment() {
        this.sequenceValue += 1;
    }
}
