package pe.edu.idat.clinicasanmiguel.repository

import android.content.Context
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.network.ApiService
import pe.edu.idat.clinicasanmiguel.network.RecetaApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.utils.CacheManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class RecetaRepository(
    context: Context
) {

    private val contextoAplicacion =
        context.applicationContext

    private val apiService: ApiService =
        RetrofitClient.obtenerApiService(
            contextoAplicacion
        )

    private val cacheManager =
        CacheManager(
            contextoAplicacion
        )

    fun listarRecetasApi(
        callback: (ResultadoCargaRecetasApi) -> Unit
    ): Call<List<RecetaApiResponse>> {

        val llamada =
            apiService.listarMisRecetas()

        llamada.enqueue(
            object :
                Callback<List<RecetaApiResponse>> {

                override fun onResponse(
                    call: Call<List<RecetaApiResponse>>,
                    response: Response<List<RecetaApiResponse>>
                ) {
                    if (call.isCanceled) {
                        return
                    }

                    if (response.isSuccessful) {
                        val recetas =
                            response.body()
                                ?: emptyList()

                        cacheManager.guardarLista(
                            recurso =
                                CacheManager.RECETAS_MEDICAS,
                            datos =
                                recetas
                        )

                        callback(
                            ResultadoCargaRecetasApi.Exito(
                                recetas
                            )
                        )

                        return
                    }

                    val mensajeServidor =
                        obtenerMensajeError(
                            response
                        )

                    when (response.code()) {
                        401 -> {
                            callback(
                                ResultadoCargaRecetasApi
                                    .SesionExpirada(
                                        mensajeServidor
                                            ?: "Tu sesión ha vencido."
                                    )
                            )
                        }

                        403 -> {
                            callback(
                                ResultadoCargaRecetasApi
                                    .SinPermiso(
                                        mensajeServidor
                                            ?: "No tienes permiso para consultar recetas médicas."
                                    )
                            )
                        }

                        else -> {
                            callback(
                                ResultadoCargaRecetasApi.Error(
                                    mensajeServidor
                                        ?: "El servidor respondió con el código ${response.code()}."
                                )
                            )
                        }
                    }
                }

                override fun onFailure(
                    call: Call<List<RecetaApiResponse>>,
                    throwable: Throwable
                ) {
                    if (call.isCanceled) {
                        return
                    }

                    val sinConexion =
                        throwable is IOException

                    if (sinConexion) {
                        callback(
                            ResultadoCargaRecetasApi
                                .SinConexion(
                                    "No fue posible comunicarse con la API."
                                )
                        )
                    } else {
                        callback(
                            ResultadoCargaRecetasApi.Error(
                                throwable.message
                                    ?: "Ocurrió un error al consultar tus recetas médicas."
                            )
                        )
                    }
                }
            }
        )

        return llamada
    }

    fun obtenerRecetasGuardadas():
            List<RecetaApiResponse>? {

        return cacheManager.obtenerLista(
            recurso =
                CacheManager.RECETAS_MEDICAS,
            claseElemento =
                RecetaApiResponse::class.java
        )
    }

    fun obtenerFechaActualizacion():
            Long? {

        return cacheManager.obtenerFechaActualizacion(
            CacheManager.RECETAS_MEDICAS
        )
    }

    private fun obtenerMensajeError(
        response: Response<*>
    ): String? {

        return try {
            val contenido =
                response.errorBody()
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

sealed class ResultadoCargaRecetasApi {

    data class Exito(
        val recetas: List<RecetaApiResponse>
    ) : ResultadoCargaRecetasApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoCargaRecetasApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoCargaRecetasApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoCargaRecetasApi()

    data class Error(
        val mensaje: String
    ) : ResultadoCargaRecetasApi()
}