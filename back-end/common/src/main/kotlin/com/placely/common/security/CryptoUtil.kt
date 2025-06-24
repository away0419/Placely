package com.placely.common.security

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-GCM 암복호화 유틸리티
 */
@Component
class CryptoUtil {
    
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val KEY_LENGTH = 256
    }
    
    /**
     * 새로운 AES 키 생성
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_LENGTH)
        return keyGenerator.generateKey()
    }
    
    /**
     * Base64 문자열로부터 SecretKey 생성
     */
    fun keyFromString(keyString: String): SecretKey {
        val decodedKey = Base64.getDecoder().decode(keyString)
        return SecretKeySpec(decodedKey, ALGORITHM)
    }
    
    /**
     * SecretKey를 Base64 문자열로 변환
     */
    fun keyToString(secretKey: SecretKey): String {
        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }
    
    /**
     * 데이터 암호화
     * @param plainText 암호화할 평문
     * @param secretKey 암호화 키
     * @return Base64로 인코딩된 암호화 데이터 (IV + 암호문)
     */
    fun encrypt(plainText: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        
        // 랜덤 IV 생성
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
        
        val encryptedData = cipher.doFinal(plainText.toByteArray())
        
        // IV + 암호문을 결합하여 반환
        val encryptedWithIv = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.size)
        System.arraycopy(encryptedData, 0, encryptedWithIv, iv.size, encryptedData.size)
        
        return Base64.getEncoder().encodeToString(encryptedWithIv)
    }
    
    /**
     * 데이터 복호화
     * @param encryptedText Base64로 인코딩된 암호화 데이터
     * @param secretKey 복호화 키
     * @return 복호화된 평문
     */
    fun decrypt(encryptedText: String, secretKey: SecretKey): String {
        val encryptedWithIv = Base64.getDecoder().decode(encryptedText)
        
        // IV와 암호문 분리
        val iv = ByteArray(GCM_IV_LENGTH)
        val encryptedData = ByteArray(encryptedWithIv.size - GCM_IV_LENGTH)
        
        System.arraycopy(encryptedWithIv, 0, iv, 0, iv.size)
        System.arraycopy(encryptedWithIv, iv.size, encryptedData, 0, encryptedData.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
        
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData)
    }
    
    /**
     * 개인정보 암호화 (기본 키 사용)
     * 환경변수나 설정에서 키를 가져와야 함
     */
    fun encryptPersonalInfo(plainText: String, keyString: String): String {
        val secretKey = keyFromString(keyString)
        return encrypt(plainText, secretKey)
    }
    
    /**
     * 개인정보 복호화 (기본 키 사용)
     */
    fun decryptPersonalInfo(encryptedText: String, keyString: String): String {
        val secretKey = keyFromString(keyString)
        return decrypt(encryptedText, secretKey)
    }
} 