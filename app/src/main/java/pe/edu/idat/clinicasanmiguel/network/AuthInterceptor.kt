package pe.edu.idat.clinicasanmiguel.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    context: Context
) : Interceptor {

    private val sessionManager =
        SessionManager(
            context.applicationContext
        )

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val solicitudOriginal =
            chain.request()

        val constructor =
            solicitudOriginal
                .newBuilder()
                .header(
                    "Accept",
                    "application/json"
                )

        val token =
            sessionManager.obtenerToken()

        if (!token.isNullOrBlank()) {
            constructor.header(
                "Authorization",
                "Bearer $token"
            )
        }

        return chain.proceed(
            constructor.build()
        )
    }
}