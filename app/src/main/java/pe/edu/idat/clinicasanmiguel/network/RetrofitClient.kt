package pe.edu.idat.clinicasanmiguel.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL =
        "https://clinicasm-bfhdgjgmcpcacgcc.westus3-01.azurewebsites.net/"

    @Volatile
    private var instanciaApi:
            ApiService? = null

    fun obtenerApiService(
        context: Context
    ): ApiService {
        return instanciaApi
            ?: synchronized(this) {
                instanciaApi
                    ?: crearApiService(
                        context.applicationContext
                    ).also {
                        instanciaApi = it
                    }
            }
    }

    private fun crearApiService(
        context: Context
    ): ApiService {
        val logging =
            HttpLoggingInterceptor().apply {
                level =
                    HttpLoggingInterceptor
                        .Level
                        .BASIC
            }

        val clienteHttp =
            OkHttpClient.Builder()
                .addInterceptor(
                    AuthInterceptor(context)
                )
                .addInterceptor(logging)
                .connectTimeout(
                    30,
                    TimeUnit.SECONDS
                )
                .readTimeout(
                    30,
                    TimeUnit.SECONDS
                )
                .writeTimeout(
                    30,
                    TimeUnit.SECONDS
                )
                .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiService::class.java)
    }
}