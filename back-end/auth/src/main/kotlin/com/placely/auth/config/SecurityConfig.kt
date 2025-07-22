package com.placely.auth.config

import com.placely.auth.filter.JWTAuthenticationFilter
import com.placely.auth.handler.SecurityCustomAccessDeniedHandler
import com.placely.auth.handler.SecurityCustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * 보안 설정
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthorizationFilter: JWTAuthenticationFilter,
    private val securityCustomAuthenticationEntryPoint: SecurityCustomAuthenticationEntryPoint,
    private val securityCustomAccessDeniedHandler: SecurityCustomAccessDeniedHandler
) {

    /**
     * CORS 설정 - 개발환경용 모든 출처 허용
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*") // 모든 출처 허용
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS") // HTTP 메소드 허용
        configuration.allowedHeaders = listOf("*") // 모든 헤더 허용
        configuration.allowCredentials = true // 인증 정보 허용
        configuration.maxAge = 3600L // preflight 캐시 시간 (1시간)
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration) // 모든 경로에 적용
        return source
    }

    /**
     * 보안 필터 체인 설정
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() } // HTTP Basic 인증 비활성화
            .formLogin { it.disable() } // 폼 로그인 비활성화
            .csrf { it.disable() } // REST API이므로 CSRF 비활성화
            .cors { it.configurationSource(corsConfigurationSource()) } // CORS 설정 적용
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 사용으로 세션 비활성화
            }
            .authorizeHttpRequests { // URL 별 인가 규칙 설정
                it
                    .requestMatchers("/login", "/register").permitAll() // 로그인, 회원가입 허용
                    .requestMatchers("/health").permitAll() // 헬스체크 허용 (경로 수정)
                    // Swagger UI 관련 경로 허용 (개발환경용)
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll()
                    .anyRequest().authenticated() // 나머지는 인증 필요
            }
            .addFilterBefore(jwtAuthorizationFilter, BasicAuthenticationFilter::class.java) // 기본 인증 필터 전 jwt 인증 필터 추가
            .exceptionHandling {
                it.authenticationEntryPoint(securityCustomAuthenticationEntryPoint) // 인증 관련 에러 처리
                it.accessDeniedHandler(securityCustomAccessDeniedHandler) // 인가 관련 에러 처리
            }
            .build()
    }
} 