package pe.edu.idat.clinicasanmiguel.repository

import android.content.Context
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.network.ApiService
import pe.edu.idat.clinicasanmiguel.network.CitaGlobalApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class CitaAdminRepository(
    context: Context
) {

    private val apiService: ApiService =
        RetrofitClient.obtenerApiService(
            context.applicationContext
        )

    fun listarCitasGlobalesApi(
        callback: (ResultadoCargaCitasAdminApi) -> Unit
    ) {
        apiService
            .listarCitasGlobalesAdmin()
            .enqueue(
                object :
                    Callback<List<CitaGlobalApiResponse>> {

                    override fun onResponse(
                        call: Call<List<CitaGlobalApiResponse>>,
                        response: Response<List<CitaGlobalApiResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (respuesta == null) {
                                callback(
                                    ResultadoCargaCitasAdminApi.Error(
                                        "La API devolvió una respuesta vacía."
                                    )
                                )

                                return
                            }

                            callback(
                                ResultadoCargaCitasAdminApi.Exito(
                                    respuesta.sortedByDescending {
                                        it.idCita
                                    }
                                )
                            )

                            return
                        }

                        val mensaje =
                            obtenerMensajeError(
                                response
                            )

                        when (response.code()) {
                            401 -> {
                                callback(
                                    ResultadoCargaCitasAdminApi
                                        .SesionExpirada(
                                            mensaje
                                                ?: "La sesión ha vencido."
                                        )
                                )
                            }

                            403 -> {
                                callback(
                                    ResultadoCargaCitasAdminApi
                                        .SinPermiso(
                                            mensaje
                                                ?: "No tienes permiso para consultar las citas."
                                        )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoCargaCitasAdminApi.Error(
                                        mensaje
                                            ?: "El servidor respondió con el código ${response.code()}."
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<List<CitaGlobalApiResponse>>,
                        throwable: Throwable
                    ) {
                        if (call.isCanceled) {
                            return
                        }

                        if (throwable is IOException) {
                            callback(
                                ResultadoCargaCitasAdminApi
                                    .SinConexion(
                                        "Necesitas conexión a Internet para consultar las citas."
                                    )
                            )
                        } else {
                            callback(
                                ResultadoCargaCitasAdminApi.Error(
                                    throwable.message
                                        ?: "Ocurrió un error al consultar las citas."
                                )
                            )
                        }
                    }
                }
            )
    }

    private fun obtenerMensajeError(
        response: Response<*>
    ): String? {
        return try {
            val contenido =
                response
                    .errorBody()
                    ?.string()

            if (contenido.isNullOrBlank()) {
                null
            } else {
                val json =
                    JSONObject(
                        contenido
                    )

                json.optString(
                    "mensaje"
                )
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?: json.optString(
                        "title"
                    )
                        .takeIf {
                            it.isNotBlank()
                        }
            }
        } catch (exception: Exception) {
            null
        }
    }
}

sealed class ResultadoCargaCitasAdminApi {

    data class Exito(
        val citas: List<CitaGlobalApiResponse>
    ) : ResultadoCargaCitasAdminApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoCargaCitasAdminApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoCargaCitasAdminApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoCargaCitasAdminApi()

    data class Error(
        val mensaje: String
    ) : ResultadoCargaCitasAdminApi()
}