package com.placely.common.security.crypto

import jakarta.persistence.PostLoad
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.reflect.Field
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

/**
 * 암복호화 JPA Entity Listener
 * Entity의 생명주기에 따라 자동으로 암복호화 처리
 * @EntityListeners(CryptoEntityListener::class)로 Entity에 적용
 */
@Component
class CryptoEntityListener {

    @Autowired
    private lateinit var cryptoUtil: CryptoUtil

    /**
     * Entity 저장/수정 전 암호화/해싱 처리
     */
    @PrePersist
    @PreUpdate
    fun beforeSave(entity: Any) {
        processEntityForSave(entity)
    }

    /**
     * Entity 조회 후 복호화 처리
     */
    @PostLoad
    fun afterLoad(entity: Any) {
        processEntityForLoad(entity)
    }

    /**
     * 저장 시 Entity 처리 (암호화/해싱)
     */
    private fun processEntityForSave(entity: Any) {
        val clazz = entity::class

        clazz.declaredMemberProperties.forEach { property ->
            val javaField = property.javaField
            if (javaField != null) {
                when {
                    javaField.isAnnotationPresent(EncryptField::class.java) -> {
                        val annotation = javaField.getAnnotation(EncryptField::class.java)
                        encryptField(entity, javaField, annotation)
                    }

                    javaField.isAnnotationPresent(HashField::class.java) -> {
                        val annotation = javaField.getAnnotation(HashField::class.java)
                        hashField(entity, javaField, annotation)
                    }
                }
            }
        }
    }

    /**
     * 조회 시 Entity 처리 (복호화)
     */
    private fun processEntityForLoad(entity: Any) {
        val clazz = entity::class

        clazz.declaredMemberProperties.forEach { property ->
            val javaField = property.javaField
            if (javaField != null && javaField.isAnnotationPresent(EncryptField::class.java)) {
                decryptField(entity, javaField)
            }
        }
    }

    /**
     * 필드 암호화 처리
     */
    private fun encryptField(entity: Any, field: Field, annotation: EncryptField) {
        field.isAccessible = true
        val plainValue = field.get(entity) as? String ?: return

        // 이미 암호화된 값인지 체크 (Base64 패턴으로 간단 체크)
        if (isAlreadyEncrypted(plainValue)) return

        try {
            // 암호화
            val encryptedValue = cryptoUtil.encrypt(plainValue)
            field.set(entity, encryptedValue)

            // 검색용 해시 생성 (기본 해싱 사용)
            if (annotation.searchable) {
                val searchHashFieldName = annotation.searchHashField.ifEmpty { "${field.name}_hash" }
                setHashField(entity, searchHashFieldName, cryptoUtil.hashing(plainValue))
            }

        } catch (e: Exception) {
            throw RuntimeException("필드 암호화 실패: ${field.name}", e)
        }
    }

    /**
     * 필드 복호화 처리
     */
    private fun decryptField(entity: Any, field: Field) {
        field.isAccessible = true
        val encryptedValue = field.get(entity) as? String ?: return

        // 암호화된 값이 아니면 스킵
        if (!isAlreadyEncrypted(encryptedValue)) return

        try {
            val decryptedValue = cryptoUtil.decrypt(encryptedValue)
            field.set(entity, decryptedValue)
        } catch (e: Exception) {
            // 복호화 실패 시 원본 값 유지 (로그만 남김)
            println("복호화 실패: ${field.name} - ${e.message}")
        }
    }

    /**
     * 필드 해싱 처리 (기본 해싱만 사용)
     */
    private fun hashField(entity: Any, field: Field, annotation: HashField) {
        field.isAccessible = true
        val plainValue = field.get(entity) as? String ?: return

        // 이미 해싱된 값인지 체크 (Base64 패턴으로 간단 체크)
        if (isAlreadyHashed(plainValue)) return

        try {
            val hashedValue = cryptoUtil.hashing(plainValue)
            field.set(entity, hashedValue)
        } catch (e: Exception) {
            throw RuntimeException("필드 해싱 실패: ${field.name}", e)
        }
    }

    /**
     * 해시 필드 설정 (리플렉션 사용)
     */
    private fun setHashField(entity: Any, fieldName: String, value: String) {
        try {
            val hashField = entity::class.java.getDeclaredField(fieldName)
            hashField.isAccessible = true
            hashField.set(entity, value)
        } catch (e: NoSuchFieldException) {
            // 해시 필드가 없으면 무시 (선택적 필드)
        } catch (e: Exception) {
            throw RuntimeException("해시 필드 설정 실패: $fieldName", e)
        }
    }

    /**
     * 이미 암호화된 값인지 체크 (간단한 Base64 패턴 체크)
     */
    private fun isAlreadyEncrypted(value: String): Boolean {
        return try {
            // Base64로 디코딩이 가능하고 길이가 적절하면 암호화된 것으로 간주
            val decoded = java.util.Base64.getDecoder().decode(value)
            decoded.size > 16 // AES-GCM은 최소 16바이트 이상
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 이미 해싱된 값인지 체크 (Base64 패턴과 길이로 체크)
     */
    private fun isAlreadyHashed(value: String): Boolean {
        return try {
            // SHA-256 해시는 Base64로 인코딩하면 44자
            val decoded = java.util.Base64.getDecoder().decode(value)
            decoded.size == 32 // SHA-256은 32바이트
        } catch (e: Exception) {
            false
        }
    }
} 