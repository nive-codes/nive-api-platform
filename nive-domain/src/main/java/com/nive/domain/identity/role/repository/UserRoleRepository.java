package com.nive.domain.identity.role.repository;

import com.nive.domain.identity.role.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nive
 * @class UserRoleRepository
 * @desc 관리자의 역할(role, level등)을 관리하는 repository
 * @since 2025-06-10
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    void deleteByUserId(Long userId);
    List<UserRole> findAllByUserId(Long userId);

}
