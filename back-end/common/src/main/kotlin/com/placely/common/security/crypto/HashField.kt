package com.placely.common.security.crypto

/**
 * 필드 해싱 어노테이션
 * 비밀번호 등 해시로 저장할 필드에 적용 (기본 해싱 사용)
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HashField 