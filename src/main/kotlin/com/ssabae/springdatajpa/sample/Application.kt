package com.ssabae.springdatajpa.sample

import com.ssabae.springdatajpa.sample.account.Account
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
