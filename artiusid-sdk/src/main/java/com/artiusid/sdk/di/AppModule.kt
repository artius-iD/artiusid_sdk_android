/*
 * File: AppModule.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.di

import android.content.Context
import com.artiusid.sdk.services.VerificationService
import com.artiusid.sdk.services.VerificationServiceImpl
import com.artiusid.sdk.utils.CertificateManager
import com.artiusid.sdk.utils.TLSSessionManager
import com.artiusid.sdk.utils.EnvironmentManager
import com.artiusid.sdk.utils.DocumentScanManager
import com.artiusid.sdk.utils.BarcodeScanManager
import com.artiusid.sdk.utils.FaceDetectionManager
import com.artiusid.sdk.services.FaceVerificationService
import com.artiusid.sdk.services.FaceVerificationServiceImpl
import com.artiusid.sdk.services.FaceMeshDetectorService
import com.artiusid.sdk.services.FaceMeshDetectorServiceImpl
import com.artiusid.sdk.data.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.artiusid.sdk.utils.RetrofitFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEnvironmentManager(@ApplicationContext context: Context): EnvironmentManager {
        return EnvironmentManager(context)
    }

    @Provides
    @Singleton
    fun provideTLSSessionManager(@ApplicationContext context: Context): TLSSessionManager {
        return TLSSessionManager(context)
    }

    @Provides
    @Singleton
    fun provideCertificateManager(@ApplicationContext context: Context): CertificateManager {
        return CertificateManager(context)
    }

    @Provides
    @Singleton
    fun provideVerificationService(
        @ApplicationContext context: Context,
        certificateManager: CertificateManager,
        tlsSessionManager: TLSSessionManager
    ): VerificationService {
        return VerificationServiceImpl(context, certificateManager, tlsSessionManager)
    }

    @Provides
    @Singleton
    fun provideDocumentScanManager(): DocumentScanManager = DocumentScanManager()

    @Provides
    @Singleton
    fun provideBarcodeScanManager(): BarcodeScanManager = BarcodeScanManager()

    @Provides
    @Singleton
    fun provideFaceDetectionManager(): FaceDetectionManager = FaceDetectionManager()

    @Provides
    @Singleton
    fun provideFaceVerificationService(@ApplicationContext context: Context): FaceVerificationService = FaceVerificationServiceImpl(context)

    @Provides
    @Singleton
    fun provideFaceMeshDetectorService(@ApplicationContext context: Context): FaceMeshDetectorService = FaceMeshDetectorServiceImpl(context)

    @Provides
    @Singleton
    fun provideVerificationOkHttpClient(tlsSessionManager: TLSSessionManager): okhttp3.OkHttpClient {
        return tlsSessionManager.getOkHttpClient()
    }

    @Provides
    @Singleton
    fun provideVerificationRetrofit(
        @ApplicationContext context: Context,
        verificationOkHttpClient: okhttp3.OkHttpClient
    ): Retrofit {
        val baseUrl = com.artiusid.sdk.utils.UrlBuilder.getVerificationBaseUrl(context)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(verificationOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideVerificationApiService(verificationRetrofit: Retrofit): com.artiusid.sdk.data.api.ApiService {
        return verificationRetrofit.create(com.artiusid.sdk.data.api.ApiService::class.java)
    }

    @Provides
    @Singleton  
    @Named("approvalRequest")
    fun provideApprovalRequestApiService(
        @ApplicationContext context: Context,
        verificationOkHttpClient: okhttp3.OkHttpClient,
        retrofitFactory: com.artiusid.sdk.utils.RetrofitFactory
    ): com.artiusid.sdk.data.api.ApiService {
        return retrofitFactory.createApprovalRequestApiService(verificationOkHttpClient)
    }
} 