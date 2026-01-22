package com.nive.application.initsettings.dto;

import com.nive.domain.system.initSettings.InitSettings;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author nive
 * @class InitSettingResponseDto
 * @desc initSetting default Dto return 객체입니다.
 * @since 2025-04-08
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PACKAGE)
@Schema(description = "초기 설정 응답 DTO")
public class OpenInitSettingsResponseDto {

    @Schema(description = "설정 키", example = "default_currency")
    private String settingKey;

    @Schema(description = "설정 값", example = "USD")
    private String settingValue;

    @Schema(description = "설명", example = "기본 통화 설정")
    private String description;

    public static OpenInitSettingsResponseDto from(InitSettings initSettings) {
        return OpenInitSettingsResponseDto.builder()
                .settingKey(initSettings.getSettingKey())
                .settingValue(initSettings.getSettingValue())
                .description(initSettings.getDescription())
                .build();
    }
}
