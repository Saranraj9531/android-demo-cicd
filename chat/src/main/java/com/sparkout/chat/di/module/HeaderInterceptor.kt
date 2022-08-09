package com.sparkout.chat.di.module

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.HEADER_AUTHORIZATION_KEY_FOR_TOKEN
import com.sparkout.chat.common.Global.HEADER_DEVICE_ID
import com.sparkout.chat.common.Global.HEADER_DEVICE_TYPE
import com.sparkout.chat.common.Global.HEADER_LOGIN_STATUS
import com.sparkout.chat.common.Global.HEADER_VERSION
import com.sparkout.chat.common.SharedPreferenceEditor
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeaderInterceptor @Inject constructor() : Interceptor, ViewModel() {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader(
                BaseUtils.HEADER_USER_ID,
                SharedPreferenceEditor.getData(
                    HEADER_LOGIN_STATUS
                )
            )
            .addHeader(BaseUtils.HEADER_KEY, BaseUtils.KEY)
            .addHeader(BaseUtils.HEADER_CHAT_KEY, BaseUtils.CHAT_KEY)
            .addHeader(
                BaseUtils.HEADER_AUTHORIZATION_KEY_FOR_TOKEN,
                SharedPreferenceEditor.getData(
                    HEADER_AUTHORIZATION_KEY_FOR_TOKEN
                )
            )
            .addHeader(
                BaseUtils.HEADER_LOGIN_STATUS,
                SharedPreferenceEditor.getData(
                    HEADER_LOGIN_STATUS
                )
            )
            .addHeader(
                BaseUtils.HEADER_DEVICE_ID,
                SharedPreferenceEditor.getData(
                    HEADER_DEVICE_ID
                )
            )
            .addHeader(
                BaseUtils.HEADER_VERSION,
                SharedPreferenceEditor.getData(
                    HEADER_VERSION
                )
            )
            .addHeader(
                BaseUtils.HEADER_DEVICE_TYPE,
                SharedPreferenceEditor.getData(
                    HEADER_DEVICE_TYPE
                )
            )
            .build()
        return chain.proceed(requestBuilder)
    }
}