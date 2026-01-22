package com.nive.application.userinfo.query;

import com.nive.application.userinfo.dto.AdminUserListSearchDto;

import com.nive.domain.authentication.mfa.QUserMfaSetting;
import com.nive.domain.identity.user.QUser;
import com.nive.domain.identity.user.enums.UserStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author nive
 * @class AdminUserQueryRepositoryImpl
 * @desc query dsl 관리자 회원 관리 조회용 repository
 * @since 2025-05-29
 */
@Repository
@RequiredArgsConstructor
public class AdminUserQueryRepositoryImpl implements AdminUserQueryRepository {
  private final JPAQueryFactory queryFactory;
  private final QUser user = QUser.user;
  private final QUserMfaSetting userMfaSetting = QUserMfaSetting.userMfaSetting;

  @Override
  public Page<AdminUserListQueryDto> searchByCondition(AdminUserListSearchDto dto) {


    Pageable pageable = dto.toPageable();

    List<AdminUserListQueryDto> content = queryFactory.select(new QAdminUserListQueryDto(
                    user.id,
                    user.loginId,
                    user.firstName,
                    user.lastName,
                    user.email,
                    user.joinedAt
            )).from(user)

            .where(eqSearchKeyword(dto.getSearchKeyword()),
                    user.isAdmin.isFalse(),
                    eqNotSuspendedAndWithdraw()
            )
            .orderBy(user.joinedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total = queryFactory
            .select(user.count())        //총 데이터 개수 조회용 count 쿼리
            .from(user)

            .where(eqSearchKeyword(dto.getSearchKeyword()),
                    user.isAdmin.isFalse(),
                    eqNotSuspendedAndWithdraw()
            )
            .fetchOne();

    return new PageImpl<>(content, pageable, Optional.ofNullable(total).orElse(0L));
//    return null;

  }


  @Override
  public Page<AdminUserWithdrawListQueryDto> searchByConditionWithdraw(AdminUserListSearchDto dto) {


    Pageable pageable = dto.toPageable();

    List<AdminUserWithdrawListQueryDto> content = queryFactory.select(new QAdminUserWithdrawListQueryDto(
                    user.id,
                    user.loginId,
                    user.firstName,
                    user.lastName,
                    user.email,
                    user.joinedAt,
                    user.deletedAt,
                    user.deletedReason
            )).from(user)
            .where(eqSearchKeyword(dto.getSearchKeyword()),
                    user.isAdmin.isFalse(),
                    eqWithdraw()
            )
            .orderBy(user.joinedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total = queryFactory
            .select(user.count())        //총 데이터 개수 조회용 count 쿼리
            .from(user)
            .where(eqSearchKeyword(dto.getSearchKeyword()),
                    user.isAdmin.isFalse(),
                    eqWithdraw()
            )
            .fetchOne();

    return new PageImpl<>(content, pageable, Optional.ofNullable(total).orElse(0L));
//    return null;

  }

  /**
   * 탈퇴 회원도 데이터 볼 수 있도록 수정-25.06.05
   * @param userId
   * @return
   */
  @Override
  public AdminUserInfoQueryDto findByUserId(Long userId) {


    return queryFactory.select(new QAdminUserInfoQueryDto(
                    user.id,
                    user.email,
                    user.loginId,
                    user.firstName,
                    user.lastName,
                    user.phoneNumber,
                    user.phoneCountryCode,
                    userMfaSetting.enabled.coalesce(false), //이게 있다면 boolean으로 판단, 없으면 false
                    user.status
            )).from(user)
            .leftJoin(userMfaSetting).on(userMfaSetting.userId.eq(user.id))
            .where(
                    user.id.eq(userId),
                    user.isAdmin.isFalse()
//                    ,notEqDeleted()
            ).fetchOne();
//    return null;
  }

  private BooleanExpression eqSearchKeyword(String keyword) {
    if (!StringUtils.hasText(keyword)) return null;
    return user.firstName.equalsIgnoreCase(keyword)
            .or(user.lastName.equalsIgnoreCase(keyword))
            .or(user.loginId.containsIgnoreCase(keyword))
            .or(user.email.equalsIgnoreCase(keyword));
  }

  private BooleanExpression eqUserStatus(UserStatus userStatus) {
      return  userStatus!= null ? user.status.eq(userStatus) : null;
  }

  private BooleanExpression eqNotSuspendedAndWithdraw() {
    return user.status.notIn(UserStatus.SUSPENDED, UserStatus.WITHDRAWN);
  }

  private BooleanExpression eqWithdraw() {
    return user.status.in(UserStatus.WITHDRAWN);
  }

}
