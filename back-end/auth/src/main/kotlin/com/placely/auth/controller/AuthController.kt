package com.placely.auth.controller

import com.placely.auth.dto.*
import com.placely.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"]) // 개발 환경용, 프로덕션에서는 특정 도메인만 허용
class AuthController(
    private val authService: AuthService
) {
    
    /**
     * 로그인
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse.success(response, "로그인에 성공했습니다."))
    }
    
    /**
     * 회원가입
     */
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<ApiResponse<SignUpResponse>> {
        val response = authService.signUp(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "회원가입이 완료되었습니다."))
    }
    
    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<RefreshTokenResponse>> {
        val response = authService.refreshToken(request)
        return ResponseEntity.ok(ApiResponse.success(response, "토큰이 갱신되었습니다."))
    }
    
    /**
     * 비밀번호 변경
     */
    @PutMapping("/password")
    fun changePassword(
        @RequestHeader("Authorization") authorization: String,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<ApiResponse<String>> {
        val userId = extractUserIdFromToken(authorization)
        authService.changePassword(userId, request)
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다."))
    }
    
    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String): ResponseEntity<ApiResponse<String>> {
        val userId = extractUserIdFromToken(authorization)
        authService.logout(userId)
        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."))
    }
    
    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") authorization: String): ResponseEntity<ApiResponse<UserInfo>> {
        val userId = extractUserIdFromToken(authorization)
        val userInfo = authService.getUserInfo(userId)
        return ResponseEntity.ok(ApiResponse.success(userInfo))
    }
    
    /**
     * 사용자명 중복 확인
     */
    @GetMapping("/check/username")
    fun checkUsername(@RequestParam username: String): ResponseEntity<ApiResponse<UserExistsResponse>> {
        val response = authService.checkUsernameExists(username)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check/email")
    fun checkEmail(@RequestParam email: String): ResponseEntity<ApiResponse<UserExistsResponse>> {
        val response = authService.checkEmailExists(email)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    /**
     * 비밀번호 강도 검증
     */
    @PostMapping("/validate/password")
    fun validatePassword(@RequestBody request: Map<String, String>): ResponseEntity<ApiResponse<PasswordStrengthResponse>> {
        val password = request["password"] ?: throw IllegalArgumentException("비밀번호를 입력해주세요.")
        val response = authService.validatePasswordStrength(password)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    /**
     * Authorization 헤더에서 사용자 ID 추출
     * 실제로는 JWT 토큰을 파싱해야 하지만, 여기서는 간단히 구현
     * 추후 Security Filter에서 처리할 예정
     */
    private fun extractUserIdFromToken(authorization: String): Long {
        // "Bearer " 접두사 제거
        val token = authorization.removePrefix("Bearer ").trim()
        
        // 여기서는 임시로 1을 반환
        // 실제로는 JWT 토큰에서 사용자 ID를 추출해야 함
        return 1L
    }
} 