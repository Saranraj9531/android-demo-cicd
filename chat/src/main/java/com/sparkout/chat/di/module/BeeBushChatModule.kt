package com.sparkout.chat.di.module

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.sparkout.chat.BuildConfig
import com.sparkout.chat.apiservice.ApiCall
import com.sparkout.chat.apiservice.BeeBushChatApiCall
import com.sparkout.chat.common.BaseUtils
import com.sparkout.chat.common.ChatApp
import com.sparkout.chat.common.Global.HEADER_AUTHORIZATION_KEY_FOR_TOKEN
import com.sparkout.chat.common.Global.HEADER_DEVICE_ID
import com.sparkout.chat.common.Global.HEADER_DEVICE_TYPE
import com.sparkout.chat.common.Global.HEADER_LOGIN_STATUS
import com.sparkout.chat.common.Global.HEADER_VERSION
import com.sparkout.chat.common.SharedPreferenceEditor
import com.sparkout.chat.network.ApiGPsComChatService
import com.sparkout.chat.network.ApiHelperImpl
import com.sparkout.chat.network.BaseDataSource
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

@Module
@InstallIn(SingletonComponent::class)
object BeeBushChatModule {
    @Provides
    @Singleton
    fun provideBeeBushApi(@Named("provideBeeBushChatRetrofit") retrofit: Retrofit): BeeBushChatApiCall =
        retrofit.create(BeeBushChatApiCall::class.java)


    @NotNull
    @Provides
    @Named("provideBeeBushChatRetrofit")
    fun provideBeeBushRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.BASE_URL) //http://messenger.bbcloudhub.com/service/
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        LoggingInterceptor.Builder()
                            .setLevel(Level.BASIC)
                            .log(Platform.INFO)
                            .addHeader("Content-Type", "application/json")
                            .response("Response")
                            .build()
                    )
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@Named("provideBeeBushChatRetrofit") retrofit: Retrofit): ApiGPsComChatService =
        retrofit.create(ApiGPsComChatService::class.java)

    @Provides
    @Singleton
    fun providerApiHelper(apiService: ApiGPsComChatService): BaseDataSource {
        return ApiHelperImpl(apiService)
    }
}