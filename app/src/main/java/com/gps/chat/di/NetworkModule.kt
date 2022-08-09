package com.gps.chat.di

import com.gps.chat.BuildConfig
import com.gps.chat.network.*
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("GpsCom")
    @Singleton
    fun provideOkHttpClient(
        @Named("Generic") okHttpClient: OkHttpClient,
        headerInterceptor: HeaderInterceptor
    ): OkHttpClient {
        return okHttpClient.newBuilder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .addInterceptor(headerInterceptor)
            .build()
    }

    @Provides
    @Named("GpsCom")
    @Singleton
    fun provideRetrofit(@Named("GpsCom") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.BASE_URL_API)
            .client(okHttpClient)
            .build()

    }

    @Provides
    @Named("Generic")
    @Singleton
    fun provideOkHttpClientHeader(
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                LoggingInterceptor.Builder()
                    .setLevel(Level.BASIC)
                    .log(Platform.INFO)
                    .request("Request")
                    .response("Response")
                    .build()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@Named("GpsCom") retrofit: Retrofit): ApiGPsComService =
        retrofit.create(ApiGPsComService::class.java)

    @Provides
    @Singleton
    fun providerApiHelper(apiService: ApiGPsComService): BaseDataSource {
        return ApiHelperImpl(apiService)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Binding {
        @Binds
        fun provideAuthProvider(authTokenProviderImpl: AuthTokenProviderImpl): AuthProvider
    }


}