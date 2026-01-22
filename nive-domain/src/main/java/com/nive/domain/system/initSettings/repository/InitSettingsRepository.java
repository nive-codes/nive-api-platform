package com.nive.domain.system.initSettings.repository;

import com.nive.domain.system.initSettings.InitSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author nive
 * @class InitSettingsRepository
 * @desc initSettings의 데이터를 관리하는 repository
 * @since 2025-04-08
 */
@Repository
public interface InitSettingsRepository extends JpaRepository<InitSettings, Long> {

    Optional<InitSettings> findBySettingKey(String settingKey);
}
