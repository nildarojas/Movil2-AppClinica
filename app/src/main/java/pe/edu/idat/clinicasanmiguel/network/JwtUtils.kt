package pe.edu.idat.clinicasanmiguel.network

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    fun esTokenVigente(
        token: String
    ): Boolean {
        return try {
            val partes = token.split(".")

            if (partes.size != 3) {
                false
            } else {
                val payloadCodificado =
                    completarPadding(
                        partes[1]
                    )

                val payloadBytes =
                    Base64.decode(
                        payloadCodificado,
                        Base64.URL_SAFE or
                                Base64.NO_WRAP
                    )

                val payload =
                    JSONObject(
                        String(
                            payloadBytes,
                            Charsets.UTF_8
                        )
                    )

                if (!payload.has("exp")) {
                    false
                } else {
                    val fechaExpiracion =
                        payload.getLong("exp")

                    val fechaActual =
                        System.currentTimeMillis() /
                                1000L

                    fechaExpiracion >
                            fechaActual
                }
            }
        } catch (exception: Exception) {
            false
        }
    }

    private fun completarPadding(
        valor: String
    ): String {
        val resto =
            valor.length % 4

        return if (resto == 0) {
            valor
        } else {
            valor +
                    "=".repeat(
                        4 - resto
                    )
        }
    }
}