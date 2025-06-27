package com.placely.common.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan

/**
 * 공통 모듈 자동 설정
 * 공통 모듈을 Nexus에 올려서 다른 서비스에서 사용할 때,
 * 공통 모듈을 다른 프로젝트에 의존성만 추가하면 자동으로 설정 적용되도록 하며
 * JwtProperties 같은 설정 클래스가 자동으로 등록되고
 * com.placely.common 하위에 있는 모든 컴포넌트가 자동으로 빈으로 등록되고
 * 아무 설정 없이도 공통 기능이 자동 활성화됨
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties::class, CryptoProperties::class)
@ComponentScan("com.placely.common")
class CommonAutoConfiguration 