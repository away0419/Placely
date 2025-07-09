package com.placely.auth.handler

import com.placely.common.security.exception.SecurityCustomErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.json.simple.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

/**
 * 인증 후 발생하는 접근 권한 오류 처리.
 */
@Component
class SecurityCustomAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        accessDeniedException: AccessDeniedException?
    ) {
        log.info("=========SecurityCustomAccessDeniedHandler===========")

        val jsonObject: JSONObject // response로 내보려는 정보를 담은 Json 객체
        val responseMap = HashMap<String?, Any?>() // response 할 데이터를 담기 위한 맵

        responseMap["msg"] = SecurityCustomErrorCode.JWT_TOKEN_ACCESS_DENIED.message
        responseMap["code"] = SecurityCustomErrorCode.JWT_TOKEN_ACCESS_DENIED.code
        jsonObject = JSONObject(responseMap)

        response?.characterEncoding = "UTF-8"
        response?.contentType = "application/json"
        response?.status = HttpStatus.FORBIDDEN.value();

        val printWriter = response?.writer
        printWriter?.print(jsonObject)
        printWriter?.flush()
        printWriter?.close()

    }
}