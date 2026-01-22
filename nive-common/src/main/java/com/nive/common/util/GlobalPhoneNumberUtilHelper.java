package com.nive.common.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nive
 * @class GlobalPhoneNumberUtilHelper
 * @desc 국가 번호 + 전화번호 관련 util
 * @since 2025-05-28
 */
@Slf4j
public class GlobalPhoneNumberUtilHelper {

  private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

  /**
   * 번호 파싱 시도
   */
  public static Phonenumber.PhoneNumber parse(String phoneNumber) {
    try {
      return phoneUtil.parse(phoneNumber, null); // null = 지역 무시, 국제번호 우선
    } catch (NumberParseException e) {
      log.warn("[전화번호 파싱 실패] 입력 값: {}, 이유: {}", phoneNumber, e.getMessage());
      return null;
    }
  }

  /**
   * 번호가 유효한 국제 전화번호인지 검사
   */
  public static boolean isValid(String phoneNumber) {
    Phonenumber.PhoneNumber parsed = parse(phoneNumber);
    return parsed != null && phoneUtil.isValidNumber(parsed);
  }

  /**
   * 국가 코드 반환 (예: +82 → 82)
   */
  public static Integer getCountryCode(String phoneNumber) {
    Phonenumber.PhoneNumber parsed = parse(phoneNumber);
    return parsed != null ? parsed.getCountryCode() : null;
  }

  /**
   * 내선 번호(국가 코드 제외한 번호) 반환 (예: 010-XXXX → 10XXXX)
   */
  public static Long getNationalNumber(String phoneNumber) {
    Phonenumber.PhoneNumber parsed = parse(phoneNumber);
    return parsed != null ? parsed.getNationalNumber() : null;
  }

  /**
   * 해당 전화번호의 ISO 지역코드 반환 (예: KR, US 등)
   */
  public static String getRegionCode(String phoneNumber) {
    Phonenumber.PhoneNumber parsed = parse(phoneNumber);
    return parsed != null ? phoneUtil.getRegionCodeForNumber(parsed) : null;
  }

  /**
   * 국제표준 포맷(E.164)로 포맷 (예: +821012345678)
   */
  public static String formatE164(String phoneNumber) {
    Phonenumber.PhoneNumber parsed = parse(phoneNumber);
    return parsed != null ? phoneUtil.format(parsed, PhoneNumberFormat.E164) : null;
  }

  /**
   * 지역표준 포맷으로 포맷 (국가 번호 제외하고 적절한 지역 형태로 변환)
   */
  public static String formatNational(String phoneNumber) {
    Phonenumber.PhoneNumber parsed = parse(phoneNumber);
    return parsed != null ? phoneUtil.format(parsed, PhoneNumberFormat.NATIONAL) : null;
  }

  /**
   * 국제표준 포맷으로 포맷 (국가 코드 포함, 공백 포함)
   */
  public static String formatInternational(String phoneNumber) {
    Phonenumber.PhoneNumber parsed = parse(phoneNumber);
    return parsed != null ? phoneUtil.format(parsed, PhoneNumberFormat.INTERNATIONAL) : null;
  }
}
