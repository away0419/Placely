package com.placely.auth.handler

import com.placely.common.security.exception.SecurityCustomException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.json.simple.JSONObject
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

private val log = KotlinLogging.logger {}

/**
 * 인증 시 발생한 오류 처리
 */
@Component
class SecurityCustomAuthenticationEntryPoint(
    private val handlerExceptionResolver: HandlerExceptionResolver
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        log.info{"===========SecurityCustomAuthenticationEntryPoint============"}

        // 커스텀 시큐리티 오류가 발생 했는지 확인.
        val securityCustomException = request?.getAttribute("securityCustomException") as? SecurityCustomException

        // 없을 경우 개발자가 예상치 못한 예외.
        if (securityCustomException == null) {
            val jsonObject: JSONObject // response로 내보려는 정보를 담은 Json 객체
            val responseMap = HashMap<String, Any>() // response 할 데이터를 담기 위한 맵

            responseMap["msg"] = "알 수 없는 오류 발생."
            responseMap["code"] = "----"
            jsonObject = JSONObject(responseMap)

            response?.characterEncoding = "UTF-8"
            response?.contentType = "application/json"
            response?.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR

            val printWriter = response?.writer
            printWriter?.print(jsonObject)
            printWriter?.flush()
            printWriter?.close()
            return
        }

        log.debug { "securityCustomException: ${securityCustomException.message}" }
        handlerExceptionResolver.resolveException(request, response, null, authException)
    }
}