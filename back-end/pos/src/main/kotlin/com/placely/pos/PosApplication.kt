package com.placely.pos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PosApplication

fun main(args: Array<String>) {
	runApplication<PosApplication>(*args)
} 