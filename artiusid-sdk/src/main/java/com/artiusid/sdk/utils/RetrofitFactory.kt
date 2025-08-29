package com.artiusid.sdk.utils

import android.content.Context
import com.artiusid.sdk.data.api.ApiService
// Removed Dagger import
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Removed javax.inject.Singleton import

// @Singleton annotation removed - using manual initialization
class RetrofitFactory constructor(
     private val context: Context
) {
    
    fun createVerificationRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = UrlBuilder.getVerificationBaseUrl(context)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun createCertificateRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = UrlBuilder.getLoadCertificateBaseUrl(context)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun createApprovalRequestRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = UrlBuilder.getApprovalRequestBaseUrl(context)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun createApprovalResponseRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = UrlBuilder.getApprovalResponseBaseUrl(context)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    fun createVerificationApiService(okHttpClient: OkHttpClient): ApiService {
        return createVerificationRetrofit(okHttpClient).create(ApiService::class.java)
    }
    
    fun createCertificateApiService(okHttpClient: OkHttpClient): ApiService {
        return createCertificateRetrofit(okHttpClient).create(ApiService::class.java)
    }
    
    fun createApprovalRequestApiService(okHttpClient: OkHttpClient): ApiService {
        return createApprovalRequestRetrofit(okHttpClient).create(ApiService::class.java)
    }
    
    fun createApprovalResponseApiService(okHttpClient: OkHttpClient): ApiService {
        return createApprovalResponseRetrofit(okHttpClient).create(ApiService::class.java)
    }
} 