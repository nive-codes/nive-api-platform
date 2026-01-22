package com.nive.application.admininfo.query;

import com.nive.application.admininfo.dto.AdminAuthSearchDto;
import com.nive.domain.identity.role.QUserRole;
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
 * @class AdminAuthQueryRepository
 * @desc queryDsl 관리자 조회용 repository
 * @since 2025-06-09
 */
@Repository
@RequiredArgsConstructor
public class AdminAuthQueryRepositoryImpl implements AdminAuthQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QUser user = QUser.user;
    private final QUserRole userRole = QUserRole.userRole;


    public Page<AdminAuthListQueryDto> searchPage(AdminAuthSearchDto dto){

        Pageable pageable = dto.toPageable();


        List<AdminAuthListQueryDto> content = queryFactory.select(new QAdminAuthListQueryDto(
                        user.id,
                        user.loginId,
                        user.firstName,
                        user.lastName,
                        user.email,
                        user.joinedAt
                )).from(user)
                .where(
                        user.isAdmin.isTrue(),
                        eqSearchKeyword(dto.getSearchKeyword()),
                        user.loginId.notIn("admin123"),
                        eqNotDeleted()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.joinedAt.desc())
                .fetch();

        Long total = queryFactory.select(user.count())
                .from(user)
                .where(
                        user.isAdmin.isTrue(),
                        eqSearchKeyword(dto.getSearchKeyword()),
                        user.loginId.notIn("admin123"),
                        eqNotDeleted()
                )
                .fetchOne();


        return new PageImpl<>(content, pageable, Optional.ofNullable(total).orElse(0L));

    }

    //목록에서의 조회 시 admin123 방지
    @Override
    public AdminAuthDetailQueryDto searchDetail(Long userId) {
        return queryFactory.select(new QAdminAuthDetailQueryDto(
                        user.id,
                        user.loginId,
                        user.firstName,
                        user.lastName,
                        user.email,
                        user.status,
                        userRole.role,
                        user.joinedAt
                )).from(user)
                .leftJoin(userRole).on(user.id.eq(userRole.userId))
                .where(
                        user.id.eq(userId),
                        eqNotDeleted(),
                        user.loginId.notIn("admin123"),
                        user.isAdmin.isTrue()
                )

                .orderBy(user.joinedAt.desc())
                .fetchOne();
//        return null;
    }


    /**
     * 본인 정보 조회 처리
     * @param userId
     * @return
     */
    @Override
    public AdminAuthDetailQueryDto getMyDetail(Long userId) {
        return queryFactory.select(new QAdminAuthDetailQueryDto(
                        user.id,
                        user.loginId,
                        user.firstName,
                        user.lastName,
                        user.email,
                        user.status,
                        userRole.role,
                        user.joinedAt
                )).from(user)
                .leftJoin(userRole).on(user.id.eq(userRole.userId))
                .where(
                        user.id.eq(userId),
                        eqNotDeleted(),
                        user.isAdmin.isTrue()
                )

                .orderBy(user.joinedAt.desc())
                .fetchOne();
//        return null;
    }

    private BooleanExpression eqSearchKeyword(String searchKeyword) {
        if(!StringUtils.hasText(searchKeyword)){
            return null;
        }
        return user.loginId.containsIgnoreCase(searchKeyword)
              .or(user.email.equalsIgnoreCase(searchKeyword))       /*개인정보 보안상 완전 일치 시 조회*/
                .or(user.lastName.equalsIgnoreCase(searchKeyword))
                .or(user.firstName.equalsIgnoreCase(searchKeyword));
    }

    private BooleanExpression eqNotDeleted() {
        return user.status.notIn(UserStatus.SUSPENDED, UserStatus.WITHDRAWN);
    }

}
