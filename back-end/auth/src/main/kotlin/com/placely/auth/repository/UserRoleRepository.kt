package com.placely.auth.repository

import com.placely.auth.entity.UserRoleEntity
import com.placely.auth.entity.UserRoleId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 사용자 역할 Repository
 */
@Repository
interface UserRoleRepository : JpaRepository<UserRoleEntity, UserRoleId> {

    /**
     * 사용자 ID로 역할 조회 (첫 번째 역할만)
     * @param userId 사용자 ID
     * @return 역할명 (ADMIN, OWNER, MANAGER, CASHIER, USER)
     */
    @Query(
        """
        SELECT r.roleName 
        FROM UserRoleEntity ur 
        JOIN RoleEntity r ON ur.roleId = r.roleId 
        WHERE ur.userId = :userId 
        AND r.isDeleted = 'N'
        ORDER BY r.roleId
    """
    )
    fun findFirstRoleNameByUserId(@Param("userId") userId: Long): String?

    /**
     * 사용자 ID로 모든 역할 조회
     * @param userId 사용자 ID
     * @return 역할명 리스트
     */
    @Query(
        """
        SELECT r.roleName 
        FROM UserRoleEntity ur 
        JOIN RoleEntity r ON ur.roleId = r.roleId 
        WHERE ur.userId = :userId 
        AND r.isDeleted = 'N'
        ORDER BY r.roleId
    """
    )
    fun findRoleNamesByUserId(@Param("userId") userId: Long): List<String>
} 