package com.placely.common.security.crypto

/**
 * 필드 암호화 어노테이션
 * Entity의 필드에 적용하여 자동 암복호화 처리
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class EncryptField(
    val searchable: Boolean = false,    // 검색 가능 여부 (true면 검색용 해시도 함께 생성)
    val searchHashField: String = ""    // 검색용 해시 필드명 (기본값: {필드명}_hash)
) 