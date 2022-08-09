package com.sparkout.chat.di.module

import android.content.Context
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.sparkout.chat.apiservice.GifApiCall
import com.sparkout.chat.common.BaseUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import org.jetbrains.annotations.NotNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

// Created by krish on 17-Jul-20.
// Copyright (c) 2020 Pikchat. All rights reserved.
@Module
@InstallIn(SingletonComponent::class)
class GifModule() {
    @Provides
    @Singleton
    fun provideTestApi(@Named("provideGifRetrofit") retrofit: Retrofit): GifApiCall =
        retrofit.create(GifApiCall::class.java)

    @NotNull
    @Provides
    @Named("provideGifRetrofit")
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BaseUtils.GIF_URL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(
                    LoggingInterceptor.Builder()
                        .setLevel(Level.BASIC)
                        .log(Platform.INFO)
                        .request("Request")
                        .response("Response")
                        .build())
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build())
        .build()
}