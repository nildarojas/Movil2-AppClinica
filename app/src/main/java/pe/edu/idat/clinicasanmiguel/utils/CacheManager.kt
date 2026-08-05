package pe.edu.idat.clinicasanmiguel.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.idat.clinicasanmiguel.network.SessionManager

class CacheManager(
    context: Context
) {

    private val contextoAplicacion =
        context.applicationContext

    private val preferencias =
        contextoAplicacion.getSharedPreferences(
            PREFERENCIAS_CACHE,
            Context.MODE_PRIVATE
        )

    private val gson =
        Gson()

    fun <T> guardarLista(
        recurso: String,
        datos: List<T>
    ): Boolean {
        val clave =
            obtenerClavePorUsuario(
                recurso
            ) ?: return false

        return guardarJson(
            clave = clave,
            contenidoJson = gson.toJson(
                datos
            )
        )
    }

    fun <T> obtenerLista(
        recurso: String,
        claseElemento: Class<T>
    ): List<T>? {
        val clave =
            obtenerClavePorUsuario(
                recurso
            ) ?: return null

        val contenidoJson =
            obtenerJson(
                clave
            ) ?: return null

        return try {
            val tipoLista =
                TypeToken.getParameterized(
                    List::class.java,
                    claseElemento
                ).type

            gson.fromJson<List<T>>(
                contenidoJson,
                tipoLista
            ) ?: emptyList()

        } catch (exception: Exception) {
            limpiarClaveInterna(
                clave
            )

            null
        }
    }

    fun <T> guardarObjeto(
        recurso: String,
        dato: T
    ): Boolean {
        val clave =
            obtenerClavePorUsuario(
                recurso
            ) ?: return false

        return guardarJson(
            clave = clave,
            contenidoJson = gson.toJson(
                dato
            )
        )
    }

    fun <T> obtenerObjeto(
        recurso: String,
        claseObjeto: Class<T>
    ): T? {
        val clave =
            obtenerClavePorUsuario(
                recurso
            ) ?: return null

        val contenidoJson =
            obtenerJson(
                clave
            ) ?: return null

        return try {
            gson.fromJson(
                contenidoJson,
                claseObjeto
            )
        } catch (exception: Exception) {
            limpiarClaveInterna(
                clave
            )

            null
        }
    }

    fun obtenerFechaActualizacion(
        recurso: String
    ): Long? {
        val clave =
            obtenerClavePorUsuario(
                recurso
            ) ?: return null

        val claveFecha =
            obtenerClaveFecha(
                clave
            )

        if (!preferencias.contains(claveFecha)) {
            return null
        }

        val fecha =
            preferencias.getLong(
                claveFecha,
                -1L
            )

        return fecha.takeIf {
            it > 0L
        }
    }

    fun limpiar(
        recurso: String
    ) {
        val clave =
            obtenerClavePorUsuario(
                recurso
            ) ?: return

        limpiarClaveInterna(
            clave
        )
    }

    private fun guardarJson(
        clave: String,
        contenidoJson: String
    ): Boolean {
        return try {
            preferencias
                .edit()
                .putString(
                    clave,
                    contenidoJson
                )
                .putLong(
                    obtenerClaveFecha(
                        clave
                    ),
                    System.currentTimeMillis()
                )
                .apply()

            true

        } catch (exception: Exception) {
            false
        }
    }

    private fun obtenerJson(
        clave: String
    ): String? {
        if (!preferencias.contains(clave)) {
            return null
        }

        return preferencias.getString(
            clave,
            null
        )
    }

    private fun obtenerClavePorUsuario(
        recurso: String
    ): String? {
        val idUsuario =
            SessionManager(
                contextoAplicacion
            ).obtenerIdUsuarioApi()
                ?: return null

        return "${recurso}_usuario_$idUsuario"
    }

    private fun obtenerClaveFecha(
        clave: String
    ): String {
        return "${clave}_fecha_actualizacion"
    }

    private fun limpiarClaveInterna(
        clave: String
    ) {
        preferencias
            .edit()
            .remove(
                clave
            )
            .remove(
                obtenerClaveFecha(
                    clave
                )
            )
            .apply()
    }

    companion object {
        private const val PREFERENCIAS_CACHE =
            "cache_clinica"

        const val CITAS_ACTIVAS =
            "citas_activas"

        const val HISTORIAL_CITAS =
            "historial_citas"

        const val NOTIFICACIONES =
            "notificaciones"

        const val PERFIL_USUARIO =
            "perfil_usuario"
    }
}