package com.placely.auth.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.placely.auth.dto.LoginRequest
import com.placely.auth.entity.User
import com.placely.auth.entity.UserStatus
import com.placely.auth.repository.TokenRepository
import com.placely.auth.repository.UserRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime

/**
 * 인증 기능 통합 테스트 - Kotest 스타일
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    private lateinit var mockMvc: MockMvc

    init {
        beforeEach {
            mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build()
        }

        describe("실제 데이터베이스를 사용한 로그인 통합 테스트") {
            context("유효한 사용자로 로그인할 때") {
                it("성공적으로 토큰을 발급해야 한다") {
                    // given: 테스트용 사용자 데이터 저장
                    val testUser = User(
                        username = "integrationtest",
                        email = "integration@test.com",
                        passwordHash = "hashedPassword123",
                        fullName = "통합테스트 사용자",
                        phone = "010-9999-9999",
                        status = UserStatus.ACTIVE,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    userRepository.save(testUser)

                    val loginRequest = LoginRequest(
                        username = "integrationtest",
                        password = "password123"
                    )

                    // when & then: 로그인 API 호출 및 결과 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.accessToken").exists())
                        .andExpect(jsonPath("$.refreshToken").exists())
                        .andExpect(jsonPath("$.tokenType").value("Bearer"))
                        .andExpect(jsonPath("$.expiresIn").exists())
                        .andExpect(jsonPath("$.user.username").value("integrationtest"))
                        .andExpect(jsonPath("$.user.email").value("integration@test.com"))
                        .andExpect(jsonPath("$.user.fullName").value("통합테스트 사용자"))
                }
            }

            context("이메일로 로그인할 때") {
                it("성공적으로 로그인되어야 한다") {
                    // given: 테스트용 사용자 데이터 저장
                    val testUser = User(
                        username = "emailtest",
                        email = "emailtest@example.com",
                        passwordHash = "hashedPassword123",
                        fullName = "이메일테스트 사용자",
                        status = UserStatus.ACTIVE,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    userRepository.save(testUser)

                    val loginRequest = LoginRequest(
                        username = "emailtest@example.com", // 이메일로 로그인
                        password = "password123"
                    )

                    // when & then: 로그인 API 호출 및 결과 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isOk)
                        .andExpect(jsonPath("$.user.email").value("emailtest@example.com"))
                }
            }

            context("존재하지 않는 사용자로 로그인할 때") {
                it("400 Bad Request를 반환해야 한다") {
                    // given: 존재하지 않는 사용자 정보
                    val loginRequest = LoginRequest(
                        username = "nonexistent",
                        password = "password123"
                    )

                    // when & then: 로그인 API 호출 및 실패 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isBadRequest)
                }
            }

            context("비활성 상태 사용자로 로그인할 때") {
                it("로그인에 실패해야 한다") {
                    // given: 비활성 상태의 테스트 사용자
                    val inactiveUser = User(
                        username = "inactiveuser",
                        email = "inactive@test.com",
                        passwordHash = "hashedPassword123",
                        fullName = "비활성 사용자",
                        status = UserStatus.INACTIVE, // 비활성 상태
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    userRepository.save(inactiveUser)

                    val loginRequest = LoginRequest(
                        username = "inactiveuser",
                        password = "password123"
                    )

                    // when & then: 로그인 API 호출 및 실패 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isBadRequest)
                }
            }

            context("삭제된 사용자로 로그인할 때") {
                it("로그인에 실패해야 한다") {
                    // given: 삭제된 상태의 테스트 사용자
                    val deletedUser = User(
                        username = "deleteduser",
                        email = "deleted@test.com",
                        passwordHash = "hashedPassword123",
                        fullName = "삭제된 사용자",
                        status = UserStatus.ACTIVE,
                        isDeleted = "Y", // 삭제된 상태
                        deletedAt = LocalDateTime.now(),
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    userRepository.save(deletedUser)

                    val loginRequest = LoginRequest(
                        username = "deleteduser",
                        password = "password123"
                    )

                    // when & then: 로그인 API 호출 및 실패 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isBadRequest)
                }
            }
        }

        describe("헬스체크 API 통합 테스트") {
            context("헬스체크를 요청할 때") {
                it("서비스 상태 정보를 반환해야 한다") {
                    // when & then: 헬스체크 API 호출 및 결과 검증
                    mockMvc.perform(get("/health"))
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.status").value("UP"))
                        .andExpect(jsonPath("$.service").value("auth-service"))
                        .andExpect(jsonPath("$.timestamp").exists())
                }
            }
        }

        describe("토큰 저장 확인 통합 테스트") {
            context("로그인 성공 시") {
                it("토큰이 데이터베이스에 저장되어야 한다") {
                    // given: 테스트용 사용자 데이터 저장
                    val testUser = User(
                        username = "tokentest",
                        email = "token@test.com",
                        passwordHash = "hashedPassword123",
                        fullName = "토큰테스트 사용자",
                        status = UserStatus.ACTIVE,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    val savedUser = userRepository.save(testUser)

                    val loginRequest = LoginRequest(
                        username = "tokentest",
                        password = "password123"
                    )

                    // 로그인 전 토큰 개수
                    val tokenCountBefore = tokenRepository.count()

                    // when: 로그인 실행
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                    )
                        .andExpect(status().isOk)

                    // then: 토큰이 데이터베이스에 저장되었는지 확인
                    val tokenCountAfter = tokenRepository.count()
                    
                    // ACCESS와 REFRESH 토큰 2개가 추가되어야 함
                    tokenCountAfter shouldBe tokenCountBefore + 2
                }
            }
        }
    }
} 