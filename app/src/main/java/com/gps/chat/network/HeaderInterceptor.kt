package com.gps.chat.network

import android.util.Log
import com.gps.chat.network.AuthProvider
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
//            requestBuilder.addHeader(CONST.AUTHORIZATION, token)
        }
        return chain.proceed(requestBuilder.build())
    }
}