package pe.edu.idat.clinicasanmiguel.network

import android.content.Context

class SessionManager(
    context: Context
) {
    private val preferencias =
        context.applicationContext.getSharedPreferences(
            "sesion_clinica",
            Context.MODE_PRIVATE
        )

    fun obtenerToken(): String? {
        return preferencias.getString(
            "TOKEN",
            null
        )
    }

    fun obtenerIdUsuarioApi(): Int? {
        val id = preferencias.getInt(
            "ID_USUARIO_API",
            -1
        )

        return if (id > 0) id else null
    }

    fun obtenerRol(): String? {
        return preferencias.getString(
            "ROL_USUARIO",
            null
        )
    }

    fun tieneSesionGuardada(): Boolean {
        return !obtenerToken().isNullOrBlank()
    }

    fun esSesionRemota(): Boolean {
        return preferencias.getBoolean(
            "SESION_REMOTA",
            false
        )
    }

    fun limpiarSesion() {
        preferencias.edit()
            .clear()
            .apply()
    }

    fun obtenerIdUsuarioLocal(): Int? {
        val id =
            preferencias.getInt(
                "ID_USUARIO",
                -1
            )

        return if (id > 0) {
            id
        } else {
            null
        }
    }

    fun actualizarNombreUsuario(
        nombre: String,
        apellido: String
    ) {
        preferencias.edit()
            .putString(
                "NOMBRE_USUARIO",
                "$nombre $apellido".trim()
            )
            .apply()
    }
}