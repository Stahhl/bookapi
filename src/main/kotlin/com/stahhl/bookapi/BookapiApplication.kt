package com.stahhl.bookapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BookapiApplication

fun main(args: Array<String>) {
    runApplication<BookapiApplication>(*args)
}
