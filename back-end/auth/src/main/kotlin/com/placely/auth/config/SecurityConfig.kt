package com.placely.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * 보안 설정
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    /**
     * 보안 필터 체인 설정
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // REST API이므로 CSRF 비활성화
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 사용으로 세션 비활성화
            }
            .authorizeHttpRequests { auth ->
                auth
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
            .httpBasic { it.disable() } // HTTP Basic 인증 비활성화
            .formLogin { it.disable() } // 폼 로그인 비활성화
            .build()
    }
} 