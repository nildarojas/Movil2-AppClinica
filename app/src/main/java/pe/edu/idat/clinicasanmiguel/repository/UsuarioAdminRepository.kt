package pe.edu.idat.clinicasanmiguel.repository

import android.content.Context
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.network.ApiService
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.UsuarioListadoApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class UsuarioAdminRepository(
    context: Context
) {

    private val apiService: ApiService =
        RetrofitClient.obtenerApiService(
            context.applicationContext
        )

    fun listarUsuariosApi(
        callback: (ResultadoCargaUsuariosAdminApi) -> Unit
    ) {
        apiService
            .listarUsuariosAdmin()
            .enqueue(
                object :
                    Callback<List<UsuarioListadoApiResponse>> {

                    override fun onResponse(
                        call: Call<List<UsuarioListadoApiResponse>>,
                        response: Response<List<UsuarioListadoApiResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (respuesta == null) {
                                callback(
                                    ResultadoCargaUsuariosAdminApi.Error(
                                        "La API devolvió una respuesta vacía"
                                    )
                                )

                                return
                            }

                            callback(
                                ResultadoCargaUsuariosAdminApi.Exito(
                                    respuesta.sortedBy {
                                        it.id
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
                                    ResultadoCargaUsuariosAdminApi
                                        .SesionExpirada(
                                            mensaje
                                                ?: "La sesión ha vencido"
                                        )
                                )
                            }

                            403 -> {
                                callback(
                                    ResultadoCargaUsuariosAdminApi
                                        .SinPermiso(
                                            mensaje
                                                ?: "No tienes permiso para consultar usuarios"
                                        )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoCargaUsuariosAdminApi.Error(
                                        mensaje
                                            ?: "El servidor respondió con el código ${response.code()}"
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<List<UsuarioListadoApiResponse>>,
                        throwable: Throwable
                    ) {
                        if (call.isCanceled) {
                            return
                        }

                        if (throwable is IOException) {
                            callback(
                                ResultadoCargaUsuariosAdminApi
                                    .SinConexion(
                                        "Necesitas conexión a Internet para consultar usuarios."
                                    )
                            )
                        } else {
                            callback(
                                ResultadoCargaUsuariosAdminApi.Error(
                                    throwable.message
                                        ?: "Ocurrió un error al consultar usuarios."
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
                JSONObject(
                    contenido
                )
                    .optString(
                        "mensaje"
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

sealed class ResultadoCargaUsuariosAdminApi {

    data class Exito(
        val usuarios: List<UsuarioListadoApiResponse>
    ) : ResultadoCargaUsuariosAdminApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoCargaUsuariosAdminApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoCargaUsuariosAdminApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoCargaUsuariosAdminApi()

    data class Error(
        val mensaje: String
    ) : ResultadoCargaUsuariosAdminApi()
}