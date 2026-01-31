package com.reservation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ReservationApplication

fun main(args: Array<String>) {
    runApplication<ReservationApplication>(*args)
}
