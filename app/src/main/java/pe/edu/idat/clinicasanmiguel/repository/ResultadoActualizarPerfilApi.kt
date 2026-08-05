package pe.edu.idat.clinicasanmiguel.repository

import pe.edu.idat.clinicasanmiguel.entity.Usuario

sealed class ResultadoActualizarPerfilApi {

    data class Exito(
        val usuario: Usuario,
        val cacheActualizada: Boolean,
        val mensaje: String
    ) : ResultadoActualizarPerfilApi()

    data class DatosDuplicados(
        val mensaje: String
    ) : ResultadoActualizarPerfilApi()

    data class SesionExpirada(
        val mensaje: String
    ) : ResultadoActualizarPerfilApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoActualizarPerfilApi()

    data class Error(
        val mensaje: String
    ) : ResultadoActualizarPerfilApi()
}