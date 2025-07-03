package com.placely.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.placely.auth.dto.LoginRequest
import com.placely.auth.dto.LoginResponse
import com.placely.auth.dto.UserInfo
import com.placely.auth.service.AuthService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * 인증 컨트롤러 테스트 - Kotest 스타일
 */
@WebMvcTest(AuthController::class)
class AuthControllerTest : DescribeSpec() {
    
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var authService: AuthService

    init {
        describe("로그인 API 테스트") {
            context("유효한 사용자 정보로 로그인할 때") {
                it("200 OK와 토큰 정보를 반환해야 한다") {
                    // given: 로그인 요청 데이터와 예상 응답 준비
                    val loginRequest = LoginRequest(
                        username = "testuser",
                        password = "password123"
                    )
                    
                    val expectedResponse = LoginResponse(
                        accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        expiresIn = 3600,
                        user = UserInfo(
                            userId = 1L,
                            username = "testuser",
                            email = "test@example.com",
                            fullName = "테스트 사용자",
                            phone = "010-1234-5678"
                        )
                    )

                    every { authService.login(loginRequest) } returns expectedResponse

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
                        .andExpect(jsonPath("$.expiresIn").value(3600))
                        .andExpect(jsonPath("$.user.userId").value(1))
                        .andExpect(jsonPath("$.user.username").value("testuser"))
                        .andExpect(jsonPath("$.user.email").value("test@example.com"))
                        .andExpect(jsonPath("$.user.fullName").value("테스트 사용자"))
                }
            }

            context("잘못된 사용자 정보로 로그인할 때") {
                it("400 Bad Request를 반환해야 한다") {
                    // given: 잘못된 로그인 요청 데이터
                    val loginRequest = LoginRequest(
                        username = "wronguser",
                        password = "wrongpassword"
                    )

                    every { authService.login(loginRequest) } throws 
                        IllegalArgumentException("사용자를 찾을 수 없습니다")

                    // when & then: 로그인 API 호출 및 결과 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isBadRequest)
                }
            }

            context("서버 내부 오류가 발생할 때") {
                it("500 Internal Server Error를 반환해야 한다") {
                    // given: 로그인 요청 데이터
                    val loginRequest = LoginRequest(
                        username = "testuser",
                        password = "password123"
                    )

                    every { authService.login(loginRequest) } throws 
                        RuntimeException("데이터베이스 연결 오류")

                    // when & then: 로그인 API 호출 및 결과 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest))
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isInternalServerError)
                }
            }

            context("필수 필드가 누락된 요청일 때") {
                it("400 Bad Request를 반환해야 한다") {
                    // given: 사용자명이 누락된 로그인 요청
                    val invalidRequest = """
                        {
                            "password": "password123"
                        }
                    """.trimIndent()

                    // when & then: 로그인 API 호출 및 결과 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest)
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isBadRequest)
                }
            }

            context("빈 값으로 로그인 요청할 때") {
                it("400 Bad Request를 반환해야 한다") {
                    // given: 빈 값을 포함한 로그인 요청
                    val emptyRequest = LoginRequest(
                        username = "",
                        password = ""
                    )

                    // when & then: 로그인 API 호출 및 결과 검증
                    mockMvc.perform(
                        post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyRequest))
                    )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(status().isBadRequest)
                }
            }
        }

        describe("헬스체크 API 테스트") {
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
    }
} 