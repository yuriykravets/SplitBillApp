package com.partitionsoft.splitbillapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform