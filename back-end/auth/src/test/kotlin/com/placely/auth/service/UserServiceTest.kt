package com.placely.auth.service

import com.placely.auth.dto.AuthUserDTO
import com.placely.auth.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class UserServiceTest : FunSpec({

    // 테스트용 목객체 생성
    val userRepository = mockk<UserRepository>()
    val userService = UserService(userRepository)

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
                gender = "M",
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
})
