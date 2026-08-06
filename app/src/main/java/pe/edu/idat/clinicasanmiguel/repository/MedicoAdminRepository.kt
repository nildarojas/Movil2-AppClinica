package pe.edu.idat.clinicasanmiguel.repository

import android.content.Context
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.entity.MedicoAdmin
import pe.edu.idat.clinicasanmiguel.network.ApiService
import pe.edu.idat.clinicasanmiguel.network.CrearMedicoApiRequest
import pe.edu.idat.clinicasanmiguel.network.MedicoApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class MedicoAdminRepository(
    context: Context
) {

    private val apiService: ApiService =
        RetrofitClient.obtenerApiService(
            context.applicationContext
        )

    fun listarMedicosApi(
        callback: (ResultadoCargaMedicosAdminApi) -> Unit
    ) {
        apiService
            .listarMedicosAdmin()
            .enqueue(
                object :
                    Callback<List<MedicoApiResponse>> {

                    override fun onResponse(
                        call: Call<List<MedicoApiResponse>>,
                        response: Response<List<MedicoApiResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (respuesta == null) {
                                callback(
                                    ResultadoCargaMedicosAdminApi.Error(
                                        "La API devolvió una respuesta vacía"
                                    )
                                )

                                return
                            }

                            val medicos =
                                respuesta
                                    .map { medicoApi ->
                                        MedicoAdmin(
                                            id = medicoApi.id,
                                            nombre = medicoApi.nombre,
                                            idEspecialidad =
                                                medicoApi.idEspecialidad,
                                            especialidad =
                                                medicoApi.especialidad
                                        )
                                    }
                                    .sortedBy {
                                        it.nombre.lowercase()
                                    }

                            callback(
                                ResultadoCargaMedicosAdminApi.Exito(
                                    medicos
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
                                    ResultadoCargaMedicosAdminApi
                                        .SesionExpirada(
                                            mensaje
                                                ?: "La sesión ha vencido"
                                        )
                                )
                            }

                            403 -> {
                                callback(
                                    ResultadoCargaMedicosAdminApi
                                        .SinPermiso(
                                            mensaje
                                                ?: "No tienes permiso para consultar médicos"
                                        )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoCargaMedicosAdminApi.Error(
                                        mensaje
                                            ?: "El servidor respondió con el código ${response.code()}"
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<List<MedicoApiResponse>>,
                        throwable: Throwable
                    ) {
                        if (call.isCanceled) {
                            return
                        }

                        if (throwable is IOException) {
                            callback(
                                ResultadoCargaMedicosAdminApi
                                    .SinConexion(
                                        "Necesitas conexión a Internet para consultar médicos."
                                    )
                            )
                        } else {
                            callback(
                                ResultadoCargaMedicosAdminApi.Error(
                                    throwable.message
                                        ?: "Ocurrió un error al consultar médicos."
                                )
                            )
                        }
                    }
                }
            )
    }

    fun registrarMedicoApi(
        nombre: String,
        idEspecialidad: Int,
        correo: String,
        password: String,
        callback: (ResultadoRegistroMedicoAdminApi) -> Unit
    ) {
        val nombreLimpio =
            nombre
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        val correoLimpio =
            correo
                .trim()
                .lowercase()

        if (
            nombreLimpio.isBlank() ||
            idEspecialidad <= 0 ||
            correoLimpio.isBlank() ||
            password.isBlank()
        ) {
            callback(
                ResultadoRegistroMedicoAdminApi.Error(
                    "Complete correctamente todos los datos del médico"
                )
            )

            return
        }

        val request =
            CrearMedicoApiRequest(
                nombre = nombreLimpio,
                idEspecialidad = idEspecialidad,
                correo = correoLimpio,
                password = password
            )

        apiService
            .registrarMedicoAdmin(
                request
            )
            .enqueue(
                object :
                    Callback<MedicoApiResponse> {

                    override fun onResponse(
                        call: Call<MedicoApiResponse>,
                        response: Response<MedicoApiResponse>
                    ) {
                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (respuesta == null) {
                                callback(
                                    ResultadoRegistroMedicoAdminApi.Error(
                                        "La API devolvió una respuesta incompleta"
                                    )
                                )

                                return
                            }

                            val medico =
                                MedicoAdmin(
                                    id = respuesta.id,
                                    nombre = respuesta.nombre,
                                    idEspecialidad =
                                        respuesta.idEspecialidad,
                                    especialidad =
                                        respuesta.especialidad
                                )

                            callback(
                                ResultadoRegistroMedicoAdminApi.Exito(
                                    medico = medico,
                                    mensaje =
                                        "Médico registrado correctamente"
                                )
                            )

                            return
                        }

                        val mensaje =
                            obtenerMensajeError(
                                response
                            )

                        when (response.code()) {
                            400 -> {
                                callback(
                                    ResultadoRegistroMedicoAdminApi.Error(
                                        mensaje
                                            ?: "Revise los datos ingresados"
                                    )
                                )
                            }

                            401 -> {
                                callback(
                                    ResultadoRegistroMedicoAdminApi
                                        .SesionExpirada(
                                            mensaje
                                                ?: "La sesión ha vencido"
                                        )
                                )
                            }

                            403 -> {
                                callback(
                                    ResultadoRegistroMedicoAdminApi
                                        .SinPermiso(
                                            mensaje
                                                ?: "Solo un administrador puede registrar médicos"
                                        )
                                )
                            }

                            404 -> {
                                callback(
                                    ResultadoRegistroMedicoAdminApi.Error(
                                        mensaje
                                            ?: "La especialidad seleccionada no existe"
                                    )
                                )
                            }

                            409 -> {
                                callback(
                                    ResultadoRegistroMedicoAdminApi
                                        .Duplicado(
                                            mensaje
                                                ?: "El médico o correo ya se encuentra registrado"
                                        )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoRegistroMedicoAdminApi.Error(
                                        mensaje
                                            ?: "El servidor respondió con el código ${response.code()}"
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<MedicoApiResponse>,
                        throwable: Throwable
                    ) {
                        if (call.isCanceled) {
                            return
                        }

                        if (throwable is IOException) {
                            callback(
                                ResultadoRegistroMedicoAdminApi
                                    .SinConexion(
                                        "Necesitas conexión a Internet para registrar médicos."
                                    )
                            )
                        } else {
                            callback(
                                ResultadoRegistroMedicoAdminApi.Error(
                                    throwable.message
                                        ?: "Ocurrió un error al registrar el médico."
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

sealed class ResultadoCargaMedicosAdminApi {

    data class Exito(
        val medicos: List<MedicoAdmin>
    ) : ResultadoCargaMedicosAdminApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoCargaMedicosAdminApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoCargaMedicosAdminApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoCargaMedicosAdminApi()

    data class Error(
        val mensaje: String
    ) : ResultadoCargaMedicosAdminApi()
}

sealed class ResultadoRegistroMedicoAdminApi {

    data class Exito(
        val medico: MedicoAdmin,
        val mensaje: String
    ) : ResultadoRegistroMedicoAdminApi()

    data class Duplicado(
        val mensaje: String
    ) : ResultadoRegistroMedicoAdminApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoRegistroMedicoAdminApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoRegistroMedicoAdminApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoRegistroMedicoAdminApi()

    data class Error(
        val mensaje: String
    ) : ResultadoRegistroMedicoAdminApi()
}