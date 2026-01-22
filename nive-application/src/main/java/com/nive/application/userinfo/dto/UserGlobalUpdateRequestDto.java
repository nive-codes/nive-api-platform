package com.nive.application.userinfo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author nive
 * @class UserGlobalUpdateRequestDto
 * @desc [클래스 설명]
 * @since 2025-04-17
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "로그인 후 정보 조회 시 최초 1회 정보 수정용(나라, 언어, 통화) DTO")
public class UserGlobalUpdateRequestDto {

    @NotBlank(message = "통화 코드는 꼭 선택해주셔야 됩니다.")
    @Schema(description = "통화 코드 - CountryCurrency Domain enabled 참조")
    private String currencyCode;

    @NotBlank(message = "언어 코드는 꼭 선택해주셔야 됩니다.")
    @Schema(description = "언어 코드 - CountryLanguage enabled 참조")
    private String languageCode;

    @NotBlank(message = "국가 코드는 꼭 선택해주셔야 됩니다.")
    @Schema(description = "국가 코드 - Country Domain enabled 참조")
    private String countryCode;



}
