package com.sparkout.chat.network

data class ServerException(val error: String) : Throwable()
