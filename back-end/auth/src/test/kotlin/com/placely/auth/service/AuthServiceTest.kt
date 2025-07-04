package com.placely.auth.service

import com.placely.auth.dto.LoginRequest
import com.placely.auth.entity.TokenEntity
import com.placely.auth.entity.UserEntity
import com.placely.auth.entity.UserStatus
import com.placely.auth.repository.TokenRepository
import com.placely.auth.repository.UserRepository
import com.placely.common.redis.RedisUtil
import com.placely.common.security.jwt.JwtUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*

/**
 * 인증 서비스 테스트 - Kotest 스타일
 */
class AuthServiceTest : DescribeSpec({

    val userRepository = mockk<UserRepository>()
    val tokenRepository = mockk<TokenRepository>()
    val jwtTokenUtil = mockk<JwtUtil>()
    val redisUtil = mockk<RedisUtil>()
    val tokenService = mockk<TokenService>()

    val authService = AuthService(
        userRepository = userRepository,
        tokenRepository = tokenRepository,
        jwtTokenUtil = jwtTokenUtil,
        redisUtil = redisUtil,
        tokenService = tokenService,
    )

    afterEach {
        clearAllMocks()
    }

    describe("로그인 기능 테스트") {
        context("유효한 사용자 정보로 로그인을 시도할 때") {
            it("성공적으로 토큰과 사용자 정보를 반환해야 한다") {
                // given: 테스트용 사용자 데이터와 모킹 설정
                val loginRequest = LoginRequest(
                    username = "testuser",
                    password = "password123"
                )

                val testUser = UserEntity(
                    userId = 1L,
                    username = "testuser",
                    email = "test@example.com",
                    passwordHash = "hashedPassword",
                    fullName = "테스트 사용자",
                    phone = "010-1234-5678",
                    status = UserStatus.ACTIVE
                )

                val mockAccessToken = "mock-access-token"
                val mockRefreshToken = "mock-refresh-token"
                val mockExpirationSeconds = 3600L
                val mockTokenEntity = mockk<TokenEntity>()
                val mockExpiresAt = LocalDateTime.now().plusSeconds(mockExpirationSeconds)


                // Mock 설정
                every { userRepository.findByUsernameOrEmailForLogin(loginRequest.username) } returns Optional.of(
                    testUser
                )
                every { mockTokenEntity.expiresAt } returns mockExpiresAt
                every { jwtTokenUtil.generateAccessToken(testUser.userId.toString(), any()) } returns mockAccessToken
                every { jwtTokenUtil.generateRefreshToken(testUser.userId.toString(), any()) } returns mockRefreshToken
                every { jwtTokenUtil.getAccessTokenExpirationSeconds() } returns mockExpirationSeconds
                every { tokenService.saveTokenToDatabase(any(), any(), any(), any()) } returns mockTokenEntity
                every { redisUtil.save(any(), any(), mockExpiresAt) } returns Unit
                every { userRepository.updateLastLoginTime(any(), any()) } returns 1

                // when: 로그인 실행
                val result = authService.login(loginRequest)

                // then: 결과 검증
                result shouldNotBe null
                result.accessToken shouldBe mockAccessToken
                result.refreshToken shouldBe mockRefreshToken
                result.expiresIn shouldBe mockExpirationSeconds
                result.user.userId shouldBe testUser.userId
                result.user.username shouldBe testUser.username
                result.user.email shouldBe testUser.email
                result.user.fullName shouldBe testUser.fullName

                // Mock 호출 검증
                verify(exactly = 1) { userRepository.findByUsernameOrEmailForLogin(loginRequest.username) }
                verify(exactly = 2) {
                    tokenService.saveTokenToDatabase(
                        any(),
                        any(),
                        any(),
                        any()
                    )
                } // ACCESS, REFRESH 2번 호출
                verify(exactly = 1) { redisUtil.save(any(), any(), mockExpiresAt) } // Redis에 토큰 정보 저장
                verify(exactly = 1) { userRepository.updateLastLoginTime(testUser.userId, any()) }
            }
        }

        context("존재하지 않는 사용자로 로그인을 시도할 때") {
            it("IllegalArgumentException 예외가 발생해야 한다") {
                // given: 존재하지 않는 사용자 정보
                val loginRequest = LoginRequest(
                    username = "nonexistentuser",
                    password = "password123"
                )

                every { userRepository.findByUsernameOrEmailForLogin(loginRequest.username) } returns Optional.empty()

                // when & then: 예외 발생 검증
                val exception = shouldThrow<IllegalArgumentException> {
                    authService.login(loginRequest)
                }

                exception.message shouldBe "사용자를 찾을 수 없습니다"
                verify(exactly = 1) { userRepository.findByUsernameOrEmailForLogin(loginRequest.username) }
                verify(exactly = 0) { tokenService.saveTokenToDatabase(any(), any(), any(), any()) }
            }
        }

        context("이메일로 로그인을 시도할 때") {
            it("성공적으로 로그인되어야 한다") {
                // given: 이메일을 사용한 로그인 요청
                val loginRequest = LoginRequest(
                    username = "test@example.com",
                    password = "password123"
                )

                val testUser = UserEntity(
                    userId = 1L,
                    username = "testuser",
                    email = "test@example.com",
                    passwordHash = "hashedPassword",
                    fullName = "테스트 사용자",
                    status = UserStatus.ACTIVE
                )

                val mockAccessToken = "mock-access-token"
                val mockRefreshToken = "mock-refresh-token"
                val mockExpiresAt = LocalDateTime.now().plusSeconds(3600L)

                // TokenEntity mock 생성
                val mockTokenEntity = mockk<TokenEntity>()
                every { mockTokenEntity.expiresAt } returns mockExpiresAt

                every { userRepository.findByUsernameOrEmailForLogin(loginRequest.username) } returns Optional.of(
                    testUser
                )
                every { jwtTokenUtil.generateAccessToken(testUser.userId.toString(), any()) } returns mockAccessToken
                every { jwtTokenUtil.generateRefreshToken(testUser.userId.toString(), any()) } returns mockRefreshToken
                every { jwtTokenUtil.getAccessTokenExpirationSeconds() } returns 3600L
                every { tokenService.saveTokenToDatabase(any(), any(), any(), any()) } returns mockTokenEntity
                every { redisUtil.save(any(), any(), mockExpiresAt) } returns Unit
                every { userRepository.updateLastLoginTime(any(), any()) } returns 1

                // when: 로그인 실행
                val result = authService.login(loginRequest)

                // then: 결과 검증
                result shouldNotBe null
                result.user.email shouldBe "test@example.com"
                verify(exactly = 1) { userRepository.findByUsernameOrEmailForLogin(loginRequest.username) }
            }
        }
    }

    describe("로그아웃 기능 테스트") {
        context("사용자가 로그아웃을 요청할 때") {
            it("Redis에서 토큰 정보가 삭제되고 DB에서 토큰이 무효화되어야 한다") {
                // given: refresh token과 사용자 ID
                val refreshToken = "mock-refresh-token"
                val userId = 1L

                every { jwtTokenUtil.getUserIdFromToken(refreshToken) } returns userId
                every { redisUtil.delete(any()) } returns Unit
                every { tokenRepository.revokeAllTokensByUserId(userId) } returns 2

                // when: 로그아웃 실행
                authService.logout(refreshToken)

                // then: Redis 삭제 및 토큰 무효화 메서드 호출 검증
                verify(exactly = 1) { jwtTokenUtil.getUserIdFromToken(refreshToken) }
                verify(exactly = 1) { redisUtil.delete(any()) }
                verify(exactly = 1) { tokenRepository.revokeAllTokensByUserId(userId) }
            }
        }
    }
}) 