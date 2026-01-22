package com.nive.domain.identity.role.repository;

import com.nive.domain.identity.role.UserRoleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author nive
 * @class UserRoleTemplateRepository
 * @desc 관리자 유저 롤을 관리하는 repository
 * @since 2025-06-10
 */
@Repository
public interface UserRoleTemplateRepository extends JpaRepository<UserRoleTemplate, Long> {
    List<UserRoleTemplate> findAllByIsAdminTrueOrderByRoleOrderDesc();

}
