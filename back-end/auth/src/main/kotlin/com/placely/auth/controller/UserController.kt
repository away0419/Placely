package com.placely.auth.controller

import com.placely.auth.dto.AuthUserDTO
import com.placely.auth.dto.AuthUserUpdateRequest
import com.placely.auth.dto.PasswordUpdateRequest
import com.placely.auth.entity.Gender
import com.placely.auth.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@Tag(name = "유저 관련 API", description = "유저 정보 관련 기능을 제공하는 API")
@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {
    @Operation(
        summary = "유저 정보 변경",
        description = "유저 정보를 변경합니다."
    )
    @PutMapping
    fun updateUserInfo(@RequestBody request: AuthUserUpdateRequest): ResponseEntity<Long> {

        val authentication = SecurityContextHolder.getContext().authentication // 인증된 사용자 가져오기
        val userId = authentication.principal as Long // JWTAuthenticationFilter에서 설정한 userId
        val genderEnum = request.gender?.let { Gender.valueOf(it) }
        val authUserDTO = AuthUserDTO(
            userId = userId,
            email = request.email,
            phone = request.phone,
            fullName = request.fullName,
            birthDate = request.birthDate,
            gender = genderEnum,
            updatedAt = LocalDateTime.now(),
            updatedBy = userId
        )

        val result = userService.updateUserInfo(authUserDTO)

        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "유저 비밀번호 변경",
        description = "유저 비밀번호를 변경합니다."
    )
    @PutMapping("/password")
    fun updatePassword(@AuthenticationPrincipal userDetails: UserDetails, @RequestBody passwordUpdateRequest: PasswordUpdateRequest){
        val userId = userDetails.username.toLong()
        userService.updateUserPassword(userId, passwordUpdateRequest)
    }
}