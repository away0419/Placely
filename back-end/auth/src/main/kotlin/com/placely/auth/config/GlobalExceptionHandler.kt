package com.placely.auth.config

import com.placely.auth.dto.ApiResponse
import com.placely.auth.dto.ErrorResponse
import com.placely.auth.exception.AuthException
import com.placely.auth.exception.TokenException
import com.placely.auth.exception.UserAccountException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리 핸들러
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * 인증 예외 처리
     */
    @ExceptionHandler(AuthException::class)
    fun handleAuthException(ex: AuthException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("인증 예외 발생: {}", ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.message ?: "인증에 실패했습니다."))
    }
    
    /**
     * 토큰 예외 처리
     */
    @ExceptionHandler(TokenException::class)
    fun handleTokenException(ex: TokenException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("토큰 예외 발생: {}", ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.message ?: "토큰이 유효하지 않습니다."))
    }
    
    /**
     * 사용자 계정 예외 처리
     */
    @ExceptionHandler(UserAccountException::class)
    fun handleUserAccountException(ex: UserAccountException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("사용자 계정 예외 발생: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.message ?: "사용자 계정 처리 중 오류가 발생했습니다."))
    }
    
    /**
     * 유효성 검증 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("유효성 검증 예외 발생: {}", ex.message)
        
        val errors = ex.bindingResult.allErrors.map { error ->
            when (error) {
                is FieldError -> "${error.field}: ${error.defaultMessage}"
                else -> error.defaultMessage ?: "알 수 없는 오류"
            }
        }
        
        val errorResponse = ErrorResponse(
            error = "VALIDATION_ERROR",
            message = "입력값 검증에 실패했습니다.",
            details = errors
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * 잘못된 인자 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("잘못된 인자 예외 발생: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.message ?: "잘못된 요청입니다."))
    }
    
    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("예상치 못한 예외 발생", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("서버 내부 오류가 발생했습니다."))
    }
} 