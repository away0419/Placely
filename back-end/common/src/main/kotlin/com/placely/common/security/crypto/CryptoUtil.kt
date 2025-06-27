package com.placely.common.security.crypto

import com.placely.common.config.CryptoProperties
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 암복호화 유틸리티 - Spring Component로 설정값 자동 주입
 */
@Component
class CryptoUtil(
    private val cryptoProperties: CryptoProperties
) {

    companion object {
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }

    /**
     * 데이터 암호화 (설정에서 키 자동 조회)
     * @param plainText String 평문
     * @return String 암호화된 문자열
     */
    fun encrypt(plainText: String): String {

        // 랜덤 IV 생성
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        val secretKey = keyFromString(cryptoProperties.encryptionKey, CryptoConstants.ENCRYPTION)
        val cipher = Cipher.getInstance(CryptoConstants.ENCRYPTION.transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

        // IV + 암호문을 결합하여 반환
        val encryptedData = cipher.doFinal(plainText.toByteArray())
        val encryptedWithIv = ByteArray(iv.size + encryptedData.size)
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.size)
        System.arraycopy(encryptedData, 0, encryptedWithIv, iv.size, encryptedData.size)

        return Base64.getEncoder().encodeToString(encryptedWithIv)
    }

    /**
     * 데이터 복호화 (설정에서 키 자동 조회)
     * @param encryptedText String 암호화된 문자열
     * @return String 복호화된 문자열
     */
    fun decrypt(encryptedText: String): String {

        // IV와 암호문 분리
        val iv = ByteArray(GCM_IV_LENGTH)
        val encryptedWithIv = Base64.getDecoder().decode(encryptedText)
        val encryptedData = ByteArray(encryptedWithIv.size - GCM_IV_LENGTH)

        System.arraycopy(encryptedWithIv, 0, iv, 0, iv.size)
        System.arraycopy(encryptedWithIv, iv.size, encryptedData, 0, encryptedData.size)

        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        val cipher = Cipher.getInstance(CryptoConstants.DECRYPTION.transformation)
        val secretKey = keyFromString(cryptoProperties.encryptionKey, CryptoConstants.DECRYPTION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData)
    }


    /**
     * Salt를 사용한 해싱 (설정에서 Salt 자동 조회)
     * @param plainText String 평문
     * @return Pair<String, String> (해시값, salt)
     */
    fun hashing(plainText: String): String {
        val salt = cryptoProperties.personalSalt
        val saltedText = plainText + salt

        val md = MessageDigest.getInstance(CryptoConstants.HASHING.algorithm)
        val hash = md.digest(saltedText.toByteArray())

        return Base64.getEncoder().encodeToString(hash)
    }

    /**
     * Salt 해싱 검증 (설정에서 Salt 자동 조회)
     * @param plainText String 평문
     * @param hashedValue String 해시값
     * @return Boolean 검증 결과
     */
    fun verifyHash(plainText: String, hashedValue: String): Boolean {
        val salt = cryptoProperties.personalSalt
        val saltedText = plainText + salt
        val md = MessageDigest.getInstance(CryptoConstants.HASHING.algorithm)
        val hash = md.digest(saltedText.toByteArray())
        val computedHash = Base64.getEncoder().encodeToString(hash)

        return hashedValue == computedHash
    }

    // ==================================================================================
    // ================================== private util ==================================
    // ==================================================================================

    /**
     * Base64 문자열로부터 SecretKey 생성
     * @param keyString String 키
     * @param cryptoConstants CryptoConstants 암호화 상수
     * @return SecretKey SecretKey
     */
    private fun keyFromString(keyString: String, cryptoConstants: CryptoConstants): SecretKey {
        val decodedKey = Base64.getDecoder().decode(keyString)
        return SecretKeySpec(decodedKey, cryptoConstants.algorithm)
    }
} 