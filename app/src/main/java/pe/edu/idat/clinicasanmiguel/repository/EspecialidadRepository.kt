package pe.edu.idat.clinicasanmiguel.repository

import android.content.ContentValues
import android.content.Context
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.data.AppDatabaseHelper
import pe.edu.idat.clinicasanmiguel.entity.Especialidad
import pe.edu.idat.clinicasanmiguel.network.ApiService
import pe.edu.idat.clinicasanmiguel.network.CrearEspecialidadApiRequest
import pe.edu.idat.clinicasanmiguel.network.EspecialidadApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EspecialidadRepository(
    context: Context
) {

    private val dbHelper =
        AppDatabaseHelper(
            context.applicationContext
        )

    private val apiService: ApiService =
        RetrofitClient.obtenerApiService(
            context.applicationContext
        )

    fun obtenerEspecialidadesLocales(): List<Especialidad> {

        val lista =
            mutableListOf<Especialidad>()

        val db =
            dbHelper.readableDatabase

        val cursor =
            db.rawQuery(
                """
            SELECT
                id,
                nombre
            FROM csma_especialidades
            ORDER BY nombre COLLATE NOCASE
            """.trimIndent(),
                null
            )

        cursor.use { resultado ->

            while (resultado.moveToNext()) {

                val especialidad =
                    Especialidad(
                        id = resultado.getInt(
                            resultado.getColumnIndexOrThrow(
                                "id"
                            )
                        ),
                        nombre = resultado.getString(
                            resultado.getColumnIndexOrThrow(
                                "nombre"
                            )
                        )
                    )

                lista.add(
                    especialidad
                )
            }
        }

        return lista
    }

    fun sincronizarEspecialidadesApi(
        callback:
            (ResultadoCargaEspecialidadesApi) -> Unit
    ) {
        apiService
            .listarEspecialidades()
            .enqueue(
                object :
                    Callback<
                            List<EspecialidadApiResponse>
                            > {

                    override fun onResponse(
                        call:
                        Call<
                                List<
                                        EspecialidadApiResponse
                                        >
                                >,
                        response:
                        Response<
                                List<
                                        EspecialidadApiResponse
                                        >
                                >
                    ) {
                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (respuesta != null) {
                                val especialidades =
                                    respuesta.map {
                                        Especialidad(
                                            id = it.id,
                                            nombre =
                                                it.nombre
                                        )
                                    }

                                val guardado =
                                    guardarEspecialidadesLocales(
                                        especialidades
                                    )

                                if (guardado) {
                                    callback(
                                        ResultadoCargaEspecialidadesApi
                                            .Exito(
                                                obtenerEspecialidadesLocales()
                                            )
                                    )
                                } else {
                                    callback(
                                        ResultadoCargaEspecialidadesApi
                                            .Error(
                                                especialidades =
                                                    obtenerEspecialidadesLocales(),
                                                mensaje =
                                                    "Azure respondió correctamente, pero no se pudo actualizar la copia local"
                                            )
                                    )
                                }

                                return
                            }

                            callback(
                                ResultadoCargaEspecialidadesApi
                                    .Error(
                                        especialidades =
                                            obtenerEspecialidadesLocales(),
                                        mensaje =
                                            "La API devolvió una respuesta vacía"
                                    )
                            )

                            return
                        }

                        when (response.code()) {
                            401 -> {
                                callback(
                                    ResultadoCargaEspecialidadesApi
                                        .SesionExpirada(
                                            "La sesión ha vencido"
                                        )
                                )
                            }

                            403 -> {
                                callback(
                                    ResultadoCargaEspecialidadesApi
                                        .SinPermiso(
                                            "No tiene permiso para consultar esta información"
                                        )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoCargaEspecialidadesApi
                                        .Error(
                                            especialidades =
                                                obtenerEspecialidadesLocales(),
                                            mensaje =
                                                obtenerMensajeError(
                                                    response
                                                )
                                                    ?: "El servidor respondió con el código ${response.code()}"
                                        )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call:
                        Call<
                                List<
                                        EspecialidadApiResponse
                                        >
                                >,
                        throwable: Throwable
                    ) {
                        callback(
                            ResultadoCargaEspecialidadesApi
                                .SinConexion(
                                    especialidades =
                                        obtenerEspecialidadesLocales(),
                                    mensaje =
                                        "Sin conexión. Se muestran las especialidades guardadas en el dispositivo."
                                )
                        )
                    }
                }
            )
    }

    fun registrarEspecialidadApi(
        nombre: String,
        callback:
            (ResultadoRegistroEspecialidadApi) -> Unit
    ) {
        val nombreLimpio =
            nombre
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        if (nombreLimpio.isEmpty()) {
            callback(
                ResultadoRegistroEspecialidadApi
                    .Error(
                        "Ingrese el nombre de la especialidad"
                    )
            )

            return
        }

        val request =
            CrearEspecialidadApiRequest(
                nombre = nombreLimpio
            )

        apiService
            .registrarEspecialidad(request)
            .enqueue(
                object :
                    Callback<
                            EspecialidadApiResponse
                            > {

                    override fun onResponse(
                        call:
                        Call<
                                EspecialidadApiResponse
                                >,
                        response:
                        Response<
                                EspecialidadApiResponse
                                >
                    ) {
                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (respuesta != null) {
                                val especialidad =
                                    Especialidad(
                                        id = respuesta.id,
                                        nombre =
                                            respuesta.nombre
                                    )

                                val guardado =
                                    guardarEspecialidadesLocales(
                                        listOf(
                                            especialidad
                                        )
                                    )

                                if (guardado) {
                                    callback(
                                        ResultadoRegistroEspecialidadApi
                                            .Exito(
                                                especialidad =
                                                    especialidad,
                                                mensaje =
                                                    "Especialidad registrada correctamente"
                                            )
                                    )
                                } else {
                                    callback(
                                        ResultadoRegistroEspecialidadApi
                                            .Error(
                                                "La especialidad fue registrada en Azure, pero no se pudo guardar la copia local"
                                            )
                                    )
                                }

                                return
                            }

                            callback(
                                ResultadoRegistroEspecialidadApi
                                    .Error(
                                        "La API devolvió una respuesta incompleta"
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
                                    ResultadoRegistroEspecialidadApi
                                        .Error(
                                            mensaje
                                                ?: "Revise el nombre ingresado"
                                        )
                                )
                            }

                            401 -> {
                                callback(
                                    ResultadoRegistroEspecialidadApi
                                        .SesionExpirada(
                                            mensaje
                                                ?: "La sesión ha vencido"
                                        )
                                )
                            }

                            403 -> {
                                callback(
                                    ResultadoRegistroEspecialidadApi
                                        .SinPermiso(
                                            mensaje
                                                ?: "Solo un administrador puede registrar especialidades"
                                        )
                                )
                            }

                            409 -> {
                                callback(
                                    ResultadoRegistroEspecialidadApi
                                        .Duplicado(
                                            mensaje
                                                ?: "La especialidad ya se encuentra registrada"
                                        )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoRegistroEspecialidadApi
                                        .Error(
                                            mensaje
                                                ?: "El servidor respondió con el código ${response.code()}"
                                        )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call:
                        Call<
                                EspecialidadApiResponse
                                >,
                        throwable: Throwable
                    ) {
                        callback(
                            ResultadoRegistroEspecialidadApi
                                .SinConexion(
                                    "Necesitas conexión a Internet para registrar una especialidad."
                                )
                        )
                    }
                }
            )
    }

    private fun guardarEspecialidadesLocales(
        especialidades:
        List<Especialidad>
    ): Boolean {
        val db =
            dbHelper.writableDatabase

        db.beginTransaction()

        try {
            especialidades.forEach {
                    especialidad ->

                val valores =
                    ContentValues().apply {
                        put(
                            "id",
                            especialidad.id
                        )

                        put(
                            "nombre",
                            especialidad.nombre
                        )
                    }

                val filasActualizadas =
                    db.update(
                        "csma_especialidades",
                        valores,
                        "id = ?",
                        arrayOf(
                            especialidad.id
                                .toString()
                        )
                    )

                if (filasActualizadas == 0) {
                    val resultado =
                        db.insert(
                            "csma_especialidades",
                            null,
                            valores
                        )

                    if (resultado == -1L) {
                        throw IllegalStateException(
                            "No se pudo guardar la especialidad"
                        )
                    }
                }
            }

            db.setTransactionSuccessful()

            return true
        } catch (exception: Exception) {
            exception.printStackTrace()

            return false
        } finally {
            db.endTransaction()
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
                JSONObject(contenido)
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

sealed class ResultadoCargaEspecialidadesApi {

    data class Exito(
        val especialidades:
        List<Especialidad>
    ) : ResultadoCargaEspecialidadesApi()

    data class SinConexion(
        val especialidades:
        List<Especialidad>,
        val mensaje: String
    ) : ResultadoCargaEspecialidadesApi()

    data class Error(
        val especialidades:
        List<Especialidad>,
        val mensaje: String
    ) : ResultadoCargaEspecialidadesApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoCargaEspecialidadesApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoCargaEspecialidadesApi()
}

sealed class ResultadoRegistroEspecialidadApi {

    data class Exito(
        val especialidad: Especialidad,
        val mensaje: String
    ) : ResultadoRegistroEspecialidadApi()

    data class Duplicado(
        val mensaje: String
    ) : ResultadoRegistroEspecialidadApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoRegistroEspecialidadApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoRegistroEspecialidadApi()

    data class SinPermiso(
        val mensaje: String
    ) : ResultadoRegistroEspecialidadApi()

    data class Error(
        val mensaje: String
    ) : ResultadoRegistroEspecialidadApi()
}