package com.arjun.covid19tracker.di

import com.arjun.covid19tracker.Constants
import com.arjun.covid19tracker.RestApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(ApplicationComponent::class)
object NetworkModule {

    @Provides
    fun providesOkhttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient().newBuilder()
    }

    @Provides
    fun provideRetrofitBuilder(okHttpClientBuilder: OkHttpClient.Builder): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClientBuilder.build())
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    fun provideRestApi(retrofit: Retrofit): RestApi {
        return retrofit.create(RestApi::class.java)
    }
}