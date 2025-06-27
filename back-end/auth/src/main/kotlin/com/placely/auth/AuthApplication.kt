package com.placely.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Auth 서비스 메인 애플리케이션
 */
@SpringBootApplication
@ComponentScan(basePackages = ["com.placely.auth", "com.placely.common"])
class AuthApplication

fun main(args: Array<String>) {
	runApplication<AuthApplication>(*args)
} 