package com.sparkout.chat.network

import android.util.Log
import com.sparkout.chat.common.Global.AUTHORIZATION
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton


/**
 *Created by Nivetha S on 02-02-2022.
 */
@Singleton
class HeaderInterceptor @Inject constructor(private val authProvider: AuthProvider) :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authProvider.token()
        val requestBuilder = chain.request().newBuilder()
        Log.e("Nive ", "intercept: ${token}")
        if (token.isNotBlank()) {
            requestBuilder.addHeader(AUTHORIZATION, token)
        }
        return chain.proceed(requestBuilder.build())
    }
}