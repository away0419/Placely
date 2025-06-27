package com.placely.common.security.exception

enum class SecurityCustomErrorCode (
    val httpStatusCode: Int,
    val code: String,
    val msg: String
) {
    /* security */
    SECURITY_PRINCIPAL_IS_NULL(500, "S008", "Security Principal is null"),

    /* jwt */
    JWT_TOKEN_TYPE_MISMATCH(401, "J001", "JWT Token Type Mismatch"),
    JWT_COOKIE_IS_NOT_FOUND(401, "J002", "JWT Cookie is not found"),
    JWT_AUTH_HEADER_IS_NOT_FOUND(401, "J003", "JWT Auth Header is not found"),
    JWT_TOKEN_EXPIRED(401, "J004", "JWT Token Expired"),
    JWT_TOKEN_IS_NULL(401, "J005", "JWT Token is null"),
    JWT_TAMPERED_INVALID(401, "J006", "JWT Token Tampered or Invalid"),
    JWT_TOKEN_MALFORMED(401, "J007", "JWT Token Malformed"),
    JWT_TOKEN_ILLEGAL_ARGUMENT(401, "J008", "JWT Token illegal argument"),
    JWT_TOKEN_ACCESS_DENIED(403, "J009", "JWT Token access denied"),

    /* oauth2 */
    OAUTH2_SERVICE_IS_NOT_FOUND(500, "O001", "OAuth2 Service is not found"),
    OAUTH2_CONTENT_IS_NOT_FOUND(500, "O002", "OAuth2 Content is not found"),
    OAUTH2_CONTENT_TYPE_MISMATCH(500, "O003", "OAuth2 Content Type Mismatch"),
    OAUTH2_USER_INFO_IS_NULL(500, "O004", "OAuth2 User Info is null"),
    OAUTH2_USER_INFO_KEY_IS_NULL(500, "O005", "OAuth2 User Info Key is null"),
    OAUTH2_USER_INFO_EMAIL_IS_NULL(500, "O006", "OAuth2 User Info Email is null"),
    OAUTH2_USER_INFO_NAME_IS_NULL(500, "O007", "OAuth2 User Info Name is null"),
}