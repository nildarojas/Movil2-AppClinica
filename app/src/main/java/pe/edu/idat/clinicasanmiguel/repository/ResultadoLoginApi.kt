package pe.edu.idat.clinicasanmiguel.repository

import pe.edu.idat.clinicasanmiguel.entity.Usuario

sealed class ResultadoLoginApi {

    data class Exito(
        val usuarioLocal: Usuario,
        val idUsuarioApi: Int,
        val token: String,
        val mensaje: String
    ) : ResultadoLoginApi()

    data class CredencialesInvalidas(
        val mensaje: String
    ) : ResultadoLoginApi()

    data class SinConexion(
        val mensaje: String
    ) : ResultadoLoginApi()

    data class Error(
        val mensaje: String
    ) : ResultadoLoginApi()
}