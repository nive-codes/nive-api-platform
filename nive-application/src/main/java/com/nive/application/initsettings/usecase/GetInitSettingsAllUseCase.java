package com.nive.application.initsettings.usecase;

import com.nive.domain.system.initSettings.InitSettings;
import com.nive.application.initsettings.dto.OpenInitSettingsResponseDto;
import com.nive.domain.system.initSettings.repository.InitSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nive
 * @class GetInitSettingsAllUseCase
 * @desc initSettings를 controller에서 받아 repository에 위임하는 usecase
 * @since 2025-04-08
 */

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetInitSettingsAllUseCase {

    private final InitSettingsRepository initSettingsRepository;

    public List<OpenInitSettingsResponseDto> findAll(){
        List<InitSettings> all = initSettingsRepository.findAll();
        return all.stream()
                .map(o -> OpenInitSettingsResponseDto.from(o))
                .collect(Collectors.toList());
    }


}
