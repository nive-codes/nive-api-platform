package com.nive.domain.identity.role.repository;

import com.nive.domain.identity.role.AdminApiRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nive
 * @class AdminApiRoleRepository
 * @desc 관리자 api 권한 관리 repository
 * @since 2025-06-09
 */
@Repository
public interface AdminApiRoleRepository extends JpaRepository<AdminApiRole,Long> {
    void deleteAllByUserId(Long userId);

    List<AdminApiRole> findAllByUserId(Long userId);
}
