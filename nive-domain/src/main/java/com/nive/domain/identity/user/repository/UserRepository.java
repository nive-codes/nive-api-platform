package com.nive.domain.identity.user.repository;

import com.nive.domain.identity.user.User;
import com.nive.domain.identity.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author nive
 * @class UserRepository
 * @desc 관리자 / 사용자 통합 관리용 Repository
 * @since 2025-04-11
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumberAndPhoneCountryCode(String phoneNumber,String phoneCountryCode);

    boolean existsByLoginId(String loginId);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<User> findAllByStatusNotIn(Collection<UserStatus> statuses);

    List<User> findAllByIsAdminTrueAndStatusNotIn( Collection<UserStatus> statuses);


    boolean existsByPhoneCountryCodeAndPhoneNumber(String phoneCountryCode, String phoneNumber);

    boolean existsByPhoneCountryCodeAndPhoneNumberAndIdNot(String phoneCountryCode, String phoneNumber, Long id);

    @Query("SELECT u FROM User u " +
            "WHERE u.status IN :statuses " +
            "AND u.deletedAt IS NOT NULL " +
            "AND u.deletedAt <= :expiredDate")
    List<User> findWithdrawnUsersOverRetentionPeriod(
            @Param("statuses") List<UserStatus> statuses,
            @Param("expiredDate") LocalDateTime expiredDate
    );
}
