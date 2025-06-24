package com.placely.common.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan

/**
 * 공통 모듈 자동 설정
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties::class)
@ComponentScan(basePackages = ["com.placely.common"])
class CommonAutoConfiguration 