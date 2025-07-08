package com.placely.auth.controller

import com.placely.auth.dto.AuthUserDTO
import com.placely.auth.dto.AuthUserUpdateRequest
import com.placely.auth.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "유저 관련 API", description = "유저 정보 관련 기능을 제공하는 API")
@RestController
class UserController(
    private val userService: UserService
) {
    @Operation(
        summary = "유저 정보 변경",
        description = "유저 정보를 변경합니다."
    )
    @PutMapping("/user")
    fun updateUserInfo(@RequestBody request: AuthUserUpdateRequest): ResponseEntity<Int> {
        val authUserDTO = AuthUserDTO(
            email = request.email,
            phone = request.phone,
            fullName = request.fullName,
            birthDate = request.birthDate,
            gender = request.gender
        )

        val result = userService.updateUserInfo(authUserDTO)

        return ResponseEntity.ok(result)
    }
}