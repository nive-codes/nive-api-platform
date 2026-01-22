package com.nive.application.login.query;



import com.nive.domain.authentication.jwt.QOutstandingToken;
import com.nive.domain.authentication.jwt.QOutstandingTokenMaster;

import com.nive.domain.authentication.jwt.enums.TokenType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author nive
 * @class BatchOutstandingTokenQueryRepository
 * @desc 블랙리스트 만료 처리 조회 등 처리할 repository
 * @since 2025-06-18
 */
@Repository
@RequiredArgsConstructor
public class BatchOutstandingTokenQueryRepositoryImpl implements BatchOutstandingTokenQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QOutstandingToken token = QOutstandingToken.outstandingToken;
    private final QOutstandingTokenMaster tokenMaster = QOutstandingTokenMaster.outstandingTokenMaster;

    /**
     * 정상적으로 만료된 토큰 기준 master 조회
     * @return
     */
    @Override
    public List<Long> expiredRefreshMasterIds() {

        return queryFactory
                .select(token.tokenMasterId)
                .from(token)
                .where(
                        token.tokenType.eq(TokenType.REFRESH),
                        token.tokenExpiresAt.loe(LocalDateTime.now()),
                        token.tokenMasterId.isNotNull()
                )
                .distinct()
                .fetch();
    }


    @Override
    public List<BatchTokenBlacklistTargetDto> tokensByMasterIds(List<Long> masterIds) {

        return queryFactory
                .select(new QBatchTokenBlacklistTargetDto(
                        token.tokenMasterId,
                        token.id,
                        tokenMaster.tokenInfo,
                        token.tokenType,
                        token.userId,
                        tokenMaster.deviceInfo,
                        token.jti,
                        token.tokenExpiresAt,
                        token.token
                ))
                .from(token)
                .leftJoin(tokenMaster).on(tokenMaster.id.eq(token.tokenMasterId))
                .where(token.tokenMasterId.in(masterIds))
                .fetch();
    }

    /**
     * master만 남은 token 조회
     * @return
     */
    @Override
    public List<BatchTokenMasterOrphanDto> orphanMasters() {

        // Step 2) token이 하나도 없는 "고아 master" 조회
        // - left join 후 token.id가 null인 경우 = 매핑된 토큰이 없음
        return queryFactory
                .select(new QBatchTokenMasterOrphanDto(
                        tokenMaster.id,
                        tokenMaster.userId,
                        tokenMaster.tokenInfo,
                        tokenMaster.deviceInfo,
                        tokenMaster.createdAt
                ))
                .from(tokenMaster)
                .leftJoin(token).on(token.tokenMasterId.eq(tokenMaster.id))
                .where(token.id.isNull())
                .fetch();
    }



}
