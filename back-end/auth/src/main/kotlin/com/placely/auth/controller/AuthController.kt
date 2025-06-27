package com.placely.auth.controller

import com.placely.auth.dto.LoginRequest
import com.placely.auth.dto.LoginResponse
import com.placely.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * 로그아웃 API
     */
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, String>> {
        return try {
            // Bearer 토큰에서 실제 토큰 추출
            val token = authorization.substring(7) // "Bearer " 제거
            val userId = authService.getUserIdFromToken(token)
            
            authService.logout(userId)
            
            ResponseEntity.ok(mapOf("message" to "로그아웃이 완료되었습니다"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to "로그아웃 처리 중 오류가 발생했습니다"))
        }
    }

    /**
     * 토큰 유효성 검증 API
     */
    @PostMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
        return try {
            val token = authorization.substring(7) // "Bearer " 제거
            val isValid = authService.validateToken(token)
            
            if (isValid) {
                val userId = authService.getUserIdFromToken(token)
                ResponseEntity.ok(mapOf(
                    "valid" to true,
                    "userId" to userId
                ))
            } else {
                ResponseEntity.ok(mapOf("valid" to false))
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "valid" to false,
                "error" to "토큰 검증 중 오류가 발생했습니다"
            ))
        }
    }

    /**
     * 헬스체크 API
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "auth-service"
        ))
    }
} 