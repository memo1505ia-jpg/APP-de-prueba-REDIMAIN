package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class SendOtpRequest(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class SendOtpResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "pin") val pin: String
)

@JsonClass(generateAdapter = true)
data class VerifyOtpResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null
)

interface BackendApiService {
    @POST("api/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("api/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse
}

object BackendRetrofitClient {
    private var customService: BackendApiService? = null

    fun setTestService(service: BackendApiService) {
        customService = service
    }

    private val defaultService: BackendApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            
        // Use BACKEND_URL from BuildConfig, fall back to default localhost if not set
        val baseUrl = try {
            BuildConfig.BACKEND_URL.ifBlank { "http://10.0.2.2:3000/" }
        } catch (e: Exception) {
            "http://10.0.2.2:3000/"
        }

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BackendApiService::class.java)
    }

    val service: BackendApiService
        get() = customService ?: defaultService
}
