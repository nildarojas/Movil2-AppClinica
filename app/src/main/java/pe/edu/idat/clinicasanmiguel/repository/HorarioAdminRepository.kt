package pe.edu.idat.clinicasanmiguel.repository

import android.content.Context
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.entity.HorarioAdmin
import pe.edu.idat.clinicasanmiguel.network.ApiService
import pe.edu.idat.clinicasanmiguel.network.CrearHorarioApiRequest
import pe.edu.idat.clinicasanmiguel.network.HorarioApiResponse
import pe.edu.idat.clinicasanmiguel.network.MedicoApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class HorarioAdminRepository(
    context: Context
) {

    private val apiService: ApiService =
        RetrofitClient.obtenerApiService(
            context.applicationContext
        )

    fun listarHorariosApi(
        callback: (ResultadoCargaHorariosAdminApi) -> Unit
    ) {
        apiService
            .listarHorariosAdmin()
            .enqueue(
                object :
                    Callback<List<HorarioApiResponse>> {

                    override fun onResponse(
                        call: Call<List<HorarioApiResponse>>,
                        response: Response<List<HorarioApiResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val horariosApi =
                                response.body()

                            if (horariosApi == null) {
                                callback(
                                    ResultadoCargaHorariosAdminApi.Error(
                                        "La API devolvió una respuesta vacía"
                                    )
                                )

                                return
                            }

                            if (horariosApi.isEmpty()) {
                                callback(
                                    ResultadoCargaHorariosAdminApi.Exito(
                                        emptyList()
                                    )
                                )

                                return
                            }

                            cargarMedicosParaCompletarHorarios(
                                horariosApi = horariosApi,
                                callback = callback
                            )

                            return
                        }

                        procesarErrorCarga(
                            response = response,
                            callback = callback
                        )
                    }

                    override fun onFailure(
                        call: Call<List<HorarioApiResponse>>,
                        throwable: Throwable
                    ) {
                        if (call.isCanceled) {
                            return
                        }

                        if (throwable is IOException) {
                            callback(
                                ResultadoCargaHorariosAdminApi
                                    .SinConexion(
                                        "Necesitas conexión a Internet para consultar los horarios."
                                    )
                            )
                        } else {
                            callback(
                                ResultadoCargaHorariosAdminApi.Error(
                                    throwable.message
                                        ?: "Ocurrió un error al consultar los horarios."
                                )
                            )
                        }
                    }
                }
            )
    }
    private fun cargarMedicosParaCompletarHorarios(
        horariosApi: List<HorarioApiResponse>,
        callback: (ResultadoCargaHorariosAdminApi) -> Unit
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
                            val medicosApi =
                                response.body()

                            if (medicosApi == null) {
                                callback(
                                    ResultadoCargaHorariosAdminApi.Error(
                                        "No se pudo obtener la información de los médicos"
                                    )
                                )

                                return
                            }

                            val medicosPorId =
                                medicosApi.associateBy {
                                        medico ->

                                    medico.id
                                }

                            val horarios =
                                horariosApi
                                    .map { horarioApi ->

                                        val medicoRelacionado =
                                            medicosPorId[
                                                horarioApi.idMedico
                                            ]

                                        HorarioAdmin(
                                            id = horarioApi.id,
                                            idMedico =
                                                horarioApi.idMedico,
                                            medico =
                                                horarioApi.medico
                                                    .ifBlank {
                                                        medicoRelacionado
                                                            ?.nombre
                                                            .orEmpty()
                                                    },

                                            especialidad =
                                                medicoRelacionado
                                                    ?.especialidad
                                                    .orEmpty(),

                                            fechaHoraTexto =
                                                horarioApi.fechaHoraTexto,

                                            estado =
                                                horarioApi.estado
                                        )
                                    }
                                    .sortedByDescending {
                                            horario ->

                                        horario.id
                                    }

                            callback(
                                ResultadoCargaHorariosAdminApi.Exito(
                                    horarios
                                )
                            )

                            return
                        }

                        procesarErrorCarga(
                            response = response,
                            callback = callback
                        )
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
                                ResultadoCargaHorariosAdminApi
                                    .SinConexion(
                                        "No fue posible completar la información de los horarios porque se perdió la conexión."
                                    )
                            )
                        } else {
                            callback(
                                ResultadoCargaHorariosAdminApi.Error(
                                    throwable.message
                                        ?: "No se pudo completar la información de los médicos."
                                )
                            )
                        }
                    }
                }
            )
    }

    fun registrarHorarioApi(
        idMedico: Int,
        fechaHoraTexto: String,
        callback: (ResultadoRegistroHorarioAdminApi) -> Unit
    ) {
        val horarioLimpio =
            fechaHoraTexto.trim()

        if (idMedico <= 0) {
            callback(
                ResultadoRegistroHorarioAdminApi.Error(
                    "Seleccione correctamente al médico"
                )
            )

            return
        }

        if (horarioLimpio.isBlank()) {
            callback(
                ResultadoRegistroHorarioAdminApi.Error(
                    "Complete correctamente la fecha y las horas"
                )
            )

            return
        }

        val request =
            CrearHorarioApiRequest(
                idMedico = idMedico,
                fechaHoraTexto = horarioLimpio
            )

        apiService
            .registrarHorarioAdmin(
                request
            )
            .enqueue(
                object :
                    Callback<HorarioApiResponse> {

                    override fun onResponse(
                        call: Call<HorarioApiResponse>,
                        response: Response<HorarioApiResponse>
                    ) {
                        if (response.isSuccessful) {
                            val horarioCreado =
                                response.body()

                            if (horarioCreado == null) {
                                callback(
                                    ResultadoRegistroHorarioAdminApi.Error(
                                        "La API devolvió una respuesta incompleta"
                                    )
                                )

                                return
                            }

                            callback(
                                ResultadoRegistroHorarioAdminApi.Exito(
                                    idHorario =
                                        horarioCreado.id,
                                    mensaje =
                                        "Horario registrado correctamente"
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
                                    ResultadoRegistroHorarioAdminApi.Error(
                                        mensaje
                                            ?: "Revise la información del horario"
                                    )
                                )
                            }

                            401 -> {
                                callback(
                                    ResultadoRegistroHorarioAdminApi
                                        .SesionExpirada(
                                            mensaje
                                                ?: "La sesión ha vencido"
                                        )
                                )
                            }

                            403 -> {
                                callback(
                                    ResultadoRegistroHorarioAdminApi
                                        .SinPermiso(
                                            mensaje
                                                ?: "Solo un administrador puede registrar horarios"
                                        )
                                )
                            }

                            404 -> {
                                callback(
                                    ResultadoRegistroHorarioAdminApi.Error(
                                        mensaje
                                            ?: "El médico seleccionado no existe"
                                    )
                                )
                            }

                            409 -> {
                                callback(
                                    ResultadoRegistroHorarioAdminApi
                                        .Duplicado(
                                            mensaje
                                                ?: "El médico ya tiene registrado ese horario"
                                        )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoRegistroHorarioAdminApi.Error(
                                        mensaje
                                            ?: "El servidor respondió con el código ${response.code()}"
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<HorarioApiResponse>,
                        throwable: Throwable
                    ) {
                        if (call.isCanceled) {
                            return
                        }

                        if (throwable is IOException) {
                            callback(
                                ResultadoRegistroHorarioAdminApi
                                    .SinConexion(
                                        "Necesitas conexión a Internet para registrar horarios."
                                    )
                            )
                        } else {
                            callback(
                                ResultadoRegistroHorarioAdminApi.Error(
                                    throwable.message
                                        ?: "Ocurrió un error al registrar el horario."
                                )
                            )
                        }
                    }
                }
            )
    }

    private fun procesarErrorCarga(
        response: Response<*>,
        callback: (ResultadoCargaHorariosAdminApi) -> Unit
    ) {
        val mensaje =
            obtenerMensajeError(
                response
            )

        when (response.code()) {
            401 -> {
                callback(
                    ResultadoCargaHorariosAdminApi
                        .SesionExpirada(
                            mensaje
                                ?: "La sesión ha vencido"
                        )
                )
            }

            403 -> {
                callback(
                    ResultadoCargaHorariosAdminApi
                        .SinPermiso(
                            mensaje
                                ?: "No tienes permiso para consultar los horarios"
                        )
                )
            }

            else -> {
                callback(
                    ResultadoCargaHorariosAdminApi.Error(
                        mensaje
                            ?: "El servidor respondió con el código ${response.code()}"
                    )
                )
            }
        }
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

sealed class ResultadoCargaHorariosAdminApi {

    data class Exito(
        val horarios: List<HorarioAdmin>
    ) : ResultadoCargaHorariosAdminApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoCargaHorariosAdminApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoCargaHorariosAdminApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoCargaHorariosAdminApi()

    data class Error(
        val mensaje: String
    ) : ResultadoCargaHorariosAdminApi()
}

sealed class ResultadoRegistroHorarioAdminApi {

    data class Exito(
        val idHorario: Int,
        val mensaje: String
    ) : ResultadoRegistroHorarioAdminApi()

    data class Duplicado(
        val mensaje: String
    ) : ResultadoRegistroHorarioAdminApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoRegistroHorarioAdminApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoRegistroHorarioAdminApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoRegistroHorarioAdminApi()

    data class Error(
        val mensaje: String
    ) : ResultadoRegistroHorarioAdminApi()
}