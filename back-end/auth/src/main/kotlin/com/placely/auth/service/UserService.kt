package com.placely.auth.service

import com.placely.auth.dto.AuthUserDTO
import com.placely.auth.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    fun updateUserInfo(authUserDTO: AuthUserDTO): Long {
        val result = userRepository.updateUserInfo(authUserDTO)

        if (result > 0) {
            return result
        } else {
            throw RuntimeException("유저 정보 변경 실패")
        }
    }
}