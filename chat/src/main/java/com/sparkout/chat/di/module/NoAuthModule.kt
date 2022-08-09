package com.sparkout.chat.di.module

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.sparkout.chat.BuildConfig
import com.sparkout.chat.apiservice.ApiCall
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
object NoAuthModule {


    @NotNull
    @Provides
    @Named("provideWithoutAuthRetrofit")
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BuildConfig.BASE_URL)
        .client(
            OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addInterceptor(
                    LoggingInterceptor.Builder()
                        .setLevel(Level.BASIC)
                        .log(Platform.INFO)
                        .addHeader("Connection", "close")
                        .addHeader("Transfer-Encoding", "chunked")
                        .addHeader("Accept-Encoding", "identity")
                        .request("Request")
                        .response("Response")
                        .build()
                )
                .build()
        )
        .build()


    @Provides
    @Singleton
    fun provideApiService(@Named("provideWithoutAuthRetrofit") retrofit: Retrofit): ApiCall =
        retrofit.create(ApiCall::class.java)

    @Provides
    @Singleton
    fun providerApiHelper(apiService: ApiGPsComChatService): BaseDataSource {
        return ApiHelperImpl(apiService)
    }
}