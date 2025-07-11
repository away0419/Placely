package com.placely.auth.controller

import com.placely.auth.dto.LoginRequest
import com.placely.auth.dto.LoginResponse
import com.placely.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@Tag(name = "인증/인가 API", description = "사용자 인증, 인가 관련 기능을 제공하는 API")
class AuthController(
    private val authService: AuthService
) {

    @Operation(
        summary = "로그인",
        description = "서비스 로그인 합니다."
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        log.info { "로그인 요청 수신 - 사용자: ${request.username}" }
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            log.warn { "로그인 실패: ${e.message}, 사용자: ${request.username}" }
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            log.error(e) { "로그인 처리 중 서버 오류 발생, 사용자: ${request.username}" }
            ResponseEntity.internalServerError().build()
        }
    }

    @Operation(
        summary = "로그아웃",
        description = "현재 토큰을 무효화하고 로그아웃 처리합니다."
    )
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, String>> {
        return try {
            // Bearer 토큰에서 실제 토큰 추출
            val token = authorization.substring(7) // "Bearer " 제거
            
            // 토큰으로 로그아웃 처리 (Redis에서 토큰 삭제 및 블랙리스트 추가)
            authService.logout(token)

            ResponseEntity.ok(mapOf("message" to "로그아웃이 완료되었습니다"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to "로그아웃 처리 중 오류가 발생했습니다"))
        }
    }

    @Operation(
        summary = "서비스 상태 확인",
        description = "Auth 서비스의 현재 상태를 확인합니다."
    )
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "redis" to "auth-service",
                "timestamp" to java.time.LocalDateTime.now().toString()
            )
        )
    }
}