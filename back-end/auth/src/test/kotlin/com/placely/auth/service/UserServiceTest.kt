package com.placely.auth.service

import com.placely.auth.dto.AuthUserDTO
import com.placely.auth.dto.PasswordUpdateRequest
import com.placely.auth.entity.Gender
import com.placely.auth.entity.UserEntity
import com.placely.auth.entity.UserStatus
import com.placely.auth.repository.UserRepository
import com.placely.common.security.crypto.CryptoUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import java.time.LocalDateTime
import java.util.*

class UserServiceTest : FunSpec({

    // 테스트용 목객체 생성 (relaxed = true로 설정하여 기본 동작 제공)
    val userRepository = mockk<UserRepository>(relaxed = true)
    val cryptoUtil = mockk<CryptoUtil>(relaxed = true)
    val userService = UserService(userRepository, cryptoUtil)

    // 각 테스트 실행 전에 mock 객체 상태 초기화
    beforeTest {
        clearMocks(userRepository, cryptoUtil)
    }

    context("updateUserInfo 메서드") {
        test("사용자 정보 업데이트 성공 시 업데이트된 행 수를 반환해야 한다") {
            // Given: 테스트 데이터 준비
            val testUserDTO = AuthUserDTO(
                userId = 1L,
                username = "testuser",
                email = "test@example.com",
                phone = "010-1234-5678",
                fullName = "테스트 사용자",
                birthDate = LocalDateTime.of(1990, 1, 1, 0, 0),
                gender = Gender.M,
                updatedAt = LocalDateTime.now(),
                updatedBy = 1L
            )

            // Repository가 성공적으로 1행을 업데이트했다고 가정
            every { userRepository.updateUserInfo(testUserDTO) } returns 1

            // When: 사용자 정보 업데이트 실행
            val result = userService.updateUserInfo(testUserDTO)

            // Then: 결과 검증
            result shouldBe 1
            
            // Repository 메서드가 올바른 파라미터로 호출되었는지 확인
            verify(exactly = 1) { userRepository.updateUserInfo(testUserDTO) }
        }

        test("Repository에서 0을 반환하면 RuntimeException을 던져야 한다") {
            // Given: 테스트 데이터 준비
            val testUserDTO = AuthUserDTO(
                userId = 1L,
                username = "testuser",
                email = "test@example.com",
                phone = "010-1234-5678",
                fullName = "테스트 사용자",
                updatedAt = LocalDateTime.now(),
                updatedBy = 1L
            )

            // Repository가 업데이트 실패로 0을 반환한다고 가정
            every { userRepository.updateUserInfo(testUserDTO) } returns 0

            // When & Then: 예외 발생 검증
            val exception = shouldThrow<RuntimeException> {
                userService.updateUserInfo(testUserDTO)
            }

            // 예외 메시지 확인
            exception.message shouldContain "유저 정보 변경 실패"
            
            // Repository 메서드가 호출되었는지 확인
            verify(exactly = 1) { userRepository.updateUserInfo(testUserDTO) }
        }

        test("Repository에서 음수를 반환하면 RuntimeException을 던져야 한다") {
            // Given: 테스트 데이터 준비
            val testUserDTO = AuthUserDTO(
                userId = 999L,
                email = "invalid@example.com"
            )

            // Repository가 비정상적으로 음수를 반환한다고 가정
            every { userRepository.updateUserInfo(testUserDTO) } returns -1

            // When & Then: 예외 발생 검증
            val exception = shouldThrow<RuntimeException> {
                userService.updateUserInfo(testUserDTO)
            }

            // 예외 메시지 확인
            exception.message shouldBe "유저 정보 변경 실패"
            
            // Repository 메서드가 호출되었는지 확인
            verify(exactly = 1) { userRepository.updateUserInfo(testUserDTO) }
        }

        test("여러 행이 업데이트되어도 정상적으로 처리되어야 한다") {
            // Given: 테스트 데이터 준비
            val testUserDTO = AuthUserDTO(
                userId = 1L,
                email = "updated@example.com",
                fullName = "업데이트된 이름",
                phone = "010-9876-5432"
            )

            // Repository가 2행을 업데이트했다고 가정 (일반적이지 않지만 가능한 경우)
            every { userRepository.updateUserInfo(testUserDTO) } returns 2

            // When: 사용자 정보 업데이트 실행
            val result = userService.updateUserInfo(testUserDTO)

            // Then: 결과 검증
            result shouldBe 2
            
            // Repository 메서드가 올바른 파라미터로 호출되었는지 확인
            verify(exactly = 1) { userRepository.updateUserInfo(testUserDTO) }
        }

        test("빈 DTO로도 Repository 호출이 정상적으로 이루어져야 한다") {
            // Given: 최소한의 데이터만 포함된 DTO
            val emptyUserDTO = AuthUserDTO()

            // Repository가 성공적으로 처리했다고 가정
            every { userRepository.updateUserInfo(emptyUserDTO) } returns 1

            // When: 사용자 정보 업데이트 실행
            val result = userService.updateUserInfo(emptyUserDTO)

            // Then: 결과 검증
            result shouldBe 1
            
            // Repository 메서드가 호출되었는지 확인
            verify(exactly = 1) { userRepository.updateUserInfo(emptyUserDTO) }
        }
    }

    context("updateUserPassword 메서드") {
        test("올바른 기존 비밀번호로 새 비밀번호 변경이 성공해야 한다") {
            // Given: 테스트 데이터 준비
            val userId = 1L
            val oldPassword = "oldPassword123"
            val newPassword = "newPassword456"
            val passwordUpdateRequest = PasswordUpdateRequest(oldPassword, newPassword)
            
            val testUserEntity = UserEntity(
                userId = userId,
                username = "testuser",
                email = "test@example.com",
                passwordHash = "hashedOldPassword",
                fullName = "테스트 사용자",
                status = UserStatus.ACTIVE,
                isDeleted = "N"
            )

            val hashedNewPassword = "hashedNewPassword"
            val passwordHashSlot = slot<String>()

            // Repository가 사용자를 정상 조회한다고 가정
            every { userRepository.findByUserIdAndIsDeleted(userId) } returns Optional.of(testUserEntity)
            
            // 기존 비밀번호 검증이 성공한다고 가정
            every { cryptoUtil.verifyHash(oldPassword, "hashedOldPassword") } returns true
            
            // 새 비밀번호 해싱 결과 설정
            every { cryptoUtil.hashing(newPassword) } returns hashedNewPassword

            // When: 비밀번호 변경 실행
            userService.updateUserPassword(userId, passwordUpdateRequest)

            // Then: 검증
            // Repository 메서드가 올바른 파라미터로 호출되었는지 확인
            verify(exactly = 1) { userRepository.findByUserIdAndIsDeleted(userId) }
            verify(exactly = 1) { cryptoUtil.verifyHash(oldPassword, "hashedOldPassword") }
            verify(exactly = 1) { cryptoUtil.hashing(newPassword) }
            
            // 사용자 엔티티의 비밀번호가 새 해시값으로 변경되었는지 확인
            testUserEntity.passwordHash shouldBe hashedNewPassword
        }

        test("존재하지 않는 사용자에 대해 RuntimeException을 던져야 한다") {
            // Given: 테스트 데이터 준비
            val userId = 999L
            val passwordUpdateRequest = PasswordUpdateRequest("oldPassword", "newPassword")

            // Repository가 사용자를 찾지 못한다고 가정
            every { userRepository.findByUserIdAndIsDeleted(userId) } returns Optional.empty()
            
            // When & Then: 예외 발생 검증
            val exception = shouldThrow<RuntimeException> {
                userService.updateUserPassword(userId, passwordUpdateRequest)
            }

            // 예외 메시지 확인
            exception.message shouldContain "유저 정보 조회 실패: userId=$userId"
            
            // Repository 메서드만 호출되고 CryptoUtil은 호출되지 않았는지 확인
            verify(exactly = 1) { userRepository.findByUserIdAndIsDeleted(userId) }
            verify(exactly = 0) { cryptoUtil.verifyHash(any(), any()) }
            verify(exactly = 0) { cryptoUtil.hashing(any()) }
        }

        test("기존 비밀번호가 일치하지 않으면 IllegalArgumentException을 던져야 한다") {
            // Given: 테스트 데이터 준비
            val userId = 1L
            val wrongOldPassword = "wrongPassword"
            val newPassword = "newPassword456"
            val passwordUpdateRequest = PasswordUpdateRequest(wrongOldPassword, newPassword)
            
            val testUserEntity = UserEntity(
                userId = userId,
                username = "testuser",
                email = "test@example.com",
                passwordHash = "hashedOldPassword",
                fullName = "테스트 사용자",
                status = UserStatus.ACTIVE,
                isDeleted = "N"
            )

            // Repository가 사용자를 정상 조회한다고 가정
            every { userRepository.findByUserIdAndIsDeleted(userId) } returns Optional.of(testUserEntity)
            
            // 기존 비밀번호 검증이 실패한다고 가정
            every { cryptoUtil.verifyHash(wrongOldPassword, "hashedOldPassword") } returns false

            // When & Then: 예외 발생 검증
            val exception = shouldThrow<IllegalArgumentException> {
                userService.updateUserPassword(userId, passwordUpdateRequest)
            }

            // 예외 메시지 확인
            exception.message shouldBe "비밀번호가 일치하지 않습니다"
            
            // Repository와 verifyHash만 호출되고 hashing은 호출되지 않았는지 확인
            verify(exactly = 1) { userRepository.findByUserIdAndIsDeleted(userId) }
            verify(exactly = 1) { cryptoUtil.verifyHash(wrongOldPassword, "hashedOldPassword") }
            verify(exactly = 0) { cryptoUtil.hashing(any()) }
            
            // 비밀번호가 변경되지 않았는지 확인
            testUserEntity.passwordHash shouldBe "hashedOldPassword"
        }

        test("삭제된 사용자에 대해서는 조회되지 않아야 한다") {
            // Given: 테스트 데이터 준비
            val userId = 1L
            val passwordUpdateRequest = PasswordUpdateRequest("oldPassword", "newPassword")

            // Repository가 삭제된 사용자로 인해 조회되지 않는다고 가정 
            // (findByUserIdAndIsDeleted는 삭제되지 않은 사용자만 조회)
            every { userRepository.findByUserIdAndIsDeleted(userId) } returns Optional.empty()

            // When & Then: 예외 발생 검증
            val exception = shouldThrow<RuntimeException> {
                userService.updateUserPassword(userId, passwordUpdateRequest)
            }

            // 예외 메시지 확인
            exception.message shouldContain "유저 정보 조회 실패: userId=$userId"
            
            // Repository 메서드만 호출되었는지 확인
            verify(exactly = 1) { userRepository.findByUserIdAndIsDeleted(userId) }
            verify(exactly = 0) { cryptoUtil.verifyHash(any(), any()) }
            verify(exactly = 0) { cryptoUtil.hashing(any()) }
        }

        test("빈 문자열 비밀번호로도 변경이 가능해야 한다") {
            // Given: 테스트 데이터 준비 (빈 문자열 새 비밀번호)
            val userId = 1L
            val oldPassword = "oldPassword123"
            val newPassword = ""
            val passwordUpdateRequest = PasswordUpdateRequest(oldPassword, newPassword)
            
            val testUserEntity = UserEntity(
                userId = userId,
                username = "testuser",
                email = "test@example.com",
                passwordHash = "hashedOldPassword",
                fullName = "테스트 사용자",
                status = UserStatus.ACTIVE,
                isDeleted = "N"
            )

            val hashedEmptyPassword = "hashedEmptyPassword"

            // Repository가 사용자를 정상 조회한다고 가정
            every { userRepository.findByUserIdAndIsDeleted(userId) } returns Optional.of(testUserEntity)
            
            // 기존 비밀번호 검증이 성공한다고 가정
            every { cryptoUtil.verifyHash(oldPassword, "hashedOldPassword") } returns true
            
            // 빈 문자열도 해싱 가능하다고 가정
            every { cryptoUtil.hashing(newPassword) } returns hashedEmptyPassword

            // When: 비밀번호 변경 실행
            userService.updateUserPassword(userId, passwordUpdateRequest)

            // Then: 검증
            verify(exactly = 1) { userRepository.findByUserIdAndIsDeleted(userId) }
            verify(exactly = 1) { cryptoUtil.verifyHash(oldPassword, "hashedOldPassword") }
            verify(exactly = 1) { cryptoUtil.hashing(newPassword) }
            
            // 사용자 엔티티의 비밀번호가 새 해시값으로 변경되었는지 확인
            testUserEntity.passwordHash shouldBe hashedEmptyPassword
        }

        test("동일한 비밀번호로 변경해도 정상 처리되어야 한다") {
            // Given: 테스트 데이터 준비 (기존과 같은 비밀번호)
            val userId = 1L
            val samePassword = "samePassword123"
            val passwordUpdateRequest = PasswordUpdateRequest(samePassword, samePassword)
            
            val testUserEntity = UserEntity(
                userId = userId,
                username = "testuser",
                email = "test@example.com",
                passwordHash = "hashedSamePassword",
                fullName = "테스트 사용자",
                status = UserStatus.ACTIVE,
                isDeleted = "N"
            )

            val hashedSamePasswordAgain = "hashedSamePasswordAgain"

            // Repository가 사용자를 정상 조회한다고 가정
            every { userRepository.findByUserIdAndIsDeleted(userId) } returns Optional.of(testUserEntity)
            
            // 기존 비밀번호 검증이 성공한다고 가정
            every { cryptoUtil.verifyHash(samePassword, "hashedSamePassword") } returns true
            
            // 동일한 비밀번호도 새로 해싱된다고 가정 (Salt 때문에 해시값이 다를 수 있음)
            every { cryptoUtil.hashing(samePassword) } returns hashedSamePasswordAgain

            // When: 비밀번호 변경 실행
            userService.updateUserPassword(userId, passwordUpdateRequest)

            // Then: 검증
            verify(exactly = 1) { userRepository.findByUserIdAndIsDeleted(userId) }
            verify(exactly = 1) { cryptoUtil.verifyHash(samePassword, "hashedSamePassword") }
            verify(exactly = 1) { cryptoUtil.hashing(samePassword) }
            
            // 사용자 엔티티의 비밀번호가 새 해시값으로 변경되었는지 확인
            testUserEntity.passwordHash shouldBe hashedSamePasswordAgain
        }
    }
})
