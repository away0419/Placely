package com.placely.auth.filter

import com.placely.common.security.exception.SecurityCustomException
import com.placely.common.security.jwt.JwtConstants
import com.placely.common.security.jwt.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

/**
 *  JWT 인증 필터
 */
@Component
class JWTAuthenticationFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        log.info("===========JWTAuthenticationFilter============")

        try {
            val header = request.getHeader(JwtConstants.AUTHORIZATION_HEADER) // JWT 헤더 추출

            if (header == null) { // JWT 헤더가 없는 경우 넘김. (인증 정보가 저장 되지 않아서 넘겨도 상관 없음)
                log.info("JWT 헤더 없음")
                filterChain.doFilter(request, response)
                return
            }

            val token = jwtUtil.getTokenFromHeader(header)
            log.debug { "token: $token" }
            val userId = jwtUtil.getUserIdFromToken(token)
            log.debug { "userId: $userId" }
            val userRoles = jwtUtil.getUserRoleFromToken(token)
            log.debug { "userRoles: $userRoles" }
            val authorities = userRoles.map { SimpleGrantedAuthority(it) }
            val authentication = UsernamePasswordAuthenticationToken(   // 인증 완료 된 객체 생성
                userId, null, authorities
            )

            SecurityContextHolder.getContext().authentication = authentication // 인증 완료 된 객체 저장
            log.info { "인증 객체 저장 완료" }

        } catch (e: SecurityCustomException) { // 만약 에러 발생한 경우 request에 담아 넘김. 이후 AuthenticationEntryPoint에서 확인함.
            log.info { "" }
            request.setAttribute("securityCustomException", e)
        }

        filterChain.doFilter(request, response)
    }
}