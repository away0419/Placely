package com.placely.auth.config

import com.placely.common.config.CryptoProperties
import com.placely.common.config.JwtProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 설정 값 확인용 테스트 컨트롤러 (개발 환경에서만 사용)
 */
@RestController
@RequestMapping("/test")
class ConfigTestController(
    private val cryptoProperties: CryptoProperties,
    private val jwtProperties: JwtProperties
) {
    
    @GetMapping("/config")
    fun getConfig(): Map<String, Any> {
        return mapOf(
            "crypto" to mapOf(
                "encryptionKey" to if (cryptoProperties.encryptionKey.contains("default")) "기본값 사용" else "환경변수 설정됨",
                "searchSalt" to if (cryptoProperties.searchSalt.contains("default")) "기본값 사용" else "환경변수 설정됨",
                "personalSalt" to if (cryptoProperties.personalSalt.contains("default")) "기본값 사용" else "환경변수 설정됨"
            ),
            "jwt" to mapOf(
                "secret" to if (jwtProperties.secret.contains("default")) "기본값 사용" else "환경변수 설정됨",
                "accessTokenExpiration" to jwtProperties.accessTokenExpiration,
                "refreshTokenExpiration" to jwtProperties.refreshTokenExpiration,
                "issuer" to jwtProperties.issuer
            ),
            "message" to "application-common.yml이 성공적으로 로드되었습니다!"
        )
    }
} 