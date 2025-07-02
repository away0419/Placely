package com.placely.auth.controller

import com.placely.auth.dto.LoginRequest
import com.placely.auth.dto.LoginResponse
import com.placely.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

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
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
//
//    /**
//     * 로그아웃 API
//     */
//    @PostMapping("/logout")
//    fun logout(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, String>> {
//        return try {
//            // Bearer 토큰에서 실제 토큰 추출
//            val token = authorization.substring(7) // "Bearer " 제거
//            val userId = authService.getUserIdFromToken(token)
//
//            authService.logout(userId)
//
//            ResponseEntity.ok(mapOf("message" to "로그아웃이 완료되었습니다"))
//        } catch (e: Exception) {
//            ResponseEntity.badRequest().body(mapOf("error" to "로그아웃 처리 중 오류가 발생했습니다"))
//        }
//    }
//
//    /**
//     * 토큰 유효성 검증 API
//     */
//    @PostMapping("/validate")
//    fun validateToken(@RequestHeader("Authorization") authorization: String): ResponseEntity<Map<String, Any>> {
//        return try {
//            val token = authorization.substring(7) // "Bearer " 제거
//            val isValid = authService.validateToken(token)
//
//            if (isValid) {
//                val userId = authService.getUserIdFromToken(token)
//                ResponseEntity.ok(mapOf(
//                    "valid" to true,
//                    "userId" to userId
//                ))
//            } else {
//                ResponseEntity.ok(mapOf("valid" to false))
//            }
//        } catch (e: Exception) {
//            ResponseEntity.badRequest().body(mapOf(
//                "valid" to false,
//                "error" to "토큰 검증 중 오류가 발생했습니다"
//            ))
//        }
//    }

    @Operation(
        summary = "서비스 상태 확인",
        description = "Auth 서비스의 현재 상태를 확인합니다."
    )
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "service" to "auth-service",
                "timestamp" to java.time.LocalDateTime.now().toString()
            )
        )
    }
}