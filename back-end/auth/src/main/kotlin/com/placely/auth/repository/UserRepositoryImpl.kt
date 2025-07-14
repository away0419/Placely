package com.placely.auth.repository

import com.placely.auth.dto.AuthUserDTO
import com.placely.auth.entity.QUserEntity
import com.placely.auth.entity.UserEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository

/**
 * 사용자 Repository 커스텀 구현체 (QueryDSL 사용)
 * 실무에서 권장하는 JPAQueryFactory 방식 사용
 */
@Repository
class UserRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : UserRepositoryCustom {

    private val queryFactory: JPAQueryFactory by lazy { JPAQueryFactory(entityManager) }

    /**
     * 유저 정보 변경 (QueryDSL 사용)
     * 실무에서 자주 사용하는 동적 업데이트 패턴 적용
     *
     * @param authUserDTO 업데이트할 사용자 정보
     * @return 업데이트된 레코드 수
     */
    override fun updateUserInfo(authUserDTO: AuthUserDTO): Long {
        // userId가 null인 경우 업데이트 불가
        val userId = authUserDTO.userId ?: return 0L

        // Q클래스 동적 생성 (안전한 방식)
        val qUser = QUserEntity.userEntity

        val updateQuery = queryFactory
            .update(qUser)
            .where(qUser.userId.eq(userId))

        // 동적으로 null이 아닌 필드만 업데이트 적용
        authUserDTO.email?.let { updateQuery.set(qUser.email, it) }
        authUserDTO.phone?.let { updateQuery.set(qUser.phone, it) }
        authUserDTO.fullName?.let { updateQuery.set(qUser.fullName, it) }
        authUserDTO.birthDate?.let { updateQuery.set(qUser.birthDate, it) }
        authUserDTO.gender?.let { updateQuery.set(qUser.gender, it) }
        authUserDTO.updatedAt?.let { updateQuery.set(qUser.updatedAt, it) }
        authUserDTO.updatedBy?.let { updateQuery.set(qUser.updatedBy, it) }

        return updateQuery.execute()
    }
}