package com.placely.auth.service

import com.placely.auth.dto.AuthUserDTO
import com.placely.auth.dto.PasswordUpdateRequest
import com.placely.auth.repository.UserRepository
import com.placely.common.security.crypto.CryptoUtil
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val cryptoUtil: CryptoUtil
) {

    /**
     * 사용자 정보 업데이트
     * @param authUserDTO 사용자 정보 업데이트 요청 정보
     * @return 업데이트 결과 (업데이트된 행 수)
     */
    fun updateUserInfo(authUserDTO: AuthUserDTO): Long {

        // 1. 사용자 정보 업데이트
        val result = userRepository.updateUserInfo(authUserDTO)

        // 2. 사용자 정보 업데이트 결과가 1 이상이면 성공, 아니면 예외 처리
        return result.takeIf { it > 0 }
            ?: throw RuntimeException("유저 정보 변경 실패")
    }

    /**
     * 사용자 비밀번호 변경
     * @param userId 사용자 ID
     * @param passwordUpdateRequest 비밀번호 변경 요청 정보
     */
    fun updateUserPassword(userId: Long, passwordUpdateRequest: PasswordUpdateRequest) {
        // 1. 사용자 정보 조회 (삭제되지 않은 사용자만)
        val userEntity = userRepository.findByUserIdAndIsDeleted(userId)
            .orElseThrow { RuntimeException("유저 정보 조회 실패: userId=$userId") }

        // 2. 기존 비밀번호 검증
        val isOldPasswordCorrect = cryptoUtil.verifyHash(passwordUpdateRequest.oldPassword, userEntity.passwordHash)
        if (!isOldPasswordCorrect) {
            log.warn { "비밀번호가 일치하지 않습니다. 사용자명: ${userEntity.username}" }
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다")
        }

        // 3. 새 비밀번호로 변경
        userEntity.passwordHash = cryptoUtil.hashing(passwordUpdateRequest.newPassword)
    }
}