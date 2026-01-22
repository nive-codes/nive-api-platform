package com.nive.domain.system.initSettings;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.io.Serializable;

/**
 * @author nive
 * @class InitSettings
 * @desc InitSettings 도메인 - 시스템 전체의 관리할 객체 (헥사고날 아키텍처 참조)
 * @since 2025-04-08
 */
@Builder(access = AccessLevel.PACKAGE)
@Getter
@Entity
@AllArgsConstructor                                 // Builder와 함께 사용 시 선택
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InitSettings implements Serializable { //[NOTE] redis를 통한 캐시화 되므로 직렬화/역직렬화 충돌 방지

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long settingId; //관리용 pk

  private String settingKey; //세팅 key

  private String settingValue; //세팅 value

  private String description; // 세팅 설명


  public void updateValue(String settingValue, String description) {
    this.settingValue = settingValue;
    this.description = description;
  }


}
