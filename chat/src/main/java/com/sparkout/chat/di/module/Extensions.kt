package com.sparkout.chat.di.module

import okhttp3.OkHttpClient

fun OkHttpClient.reverseInterceptors(): OkHttpClient {
    val builder = this.newBuilder()
    builder.interceptors().reverse()
    return builder.build()
}
