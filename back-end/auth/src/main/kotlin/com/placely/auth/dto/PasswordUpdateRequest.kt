package com.placely.auth.dto

data class PasswordUpdateRequest(
    val oldPassword: String,
    val newPassword: String,
)