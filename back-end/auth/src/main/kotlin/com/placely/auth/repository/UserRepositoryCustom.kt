package com.placely.auth.repository

import com.placely.auth.dto.AuthUserDTO

/**
 * 사용자 Repository 커스텀 인터페이스 (QueryDSL 사용)
 */
interface UserRepositoryCustom {
    
    /**
     * 유저 정보 변경 (QueryDSL 사용)
     */
    fun updateUserInfo(authUserDTO: AuthUserDTO): Long
} 