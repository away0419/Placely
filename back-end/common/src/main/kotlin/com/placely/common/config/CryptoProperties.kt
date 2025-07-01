package com.placely.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

/**
 * 암호화 관련 설정 Properties
 * application.yml의 placely.security.crypto 설정을 바인딩
 * @ConstructorBinding 사용 시 default 값 설정 불가능.
 */
@ConfigurationProperties(prefix = "placely.security.crypto")
data class CryptoProperties @ConstructorBinding constructor(
    val encryptionKey: String,  //데이터 암호화용 키
    val personalSalt: String    // 개인정보 해싱용 Salt
) 