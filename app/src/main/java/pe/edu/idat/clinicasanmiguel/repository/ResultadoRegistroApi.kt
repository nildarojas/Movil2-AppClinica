package pe.edu.idat.clinicasanmiguel.repository

data class DatosRegistroApi(
    val idUsuarioApi: Int,
    val idUsuarioLocal: Long
)

sealed class ResultadoRegistroApi {

    data class Exito(
        val datos: DatosRegistroApi,
        val mensaje: String
    ) : ResultadoRegistroApi()

    data class Duplicado(
        val mensaje: String
    ) : ResultadoRegistroApi()

    data class RegistroLocal(
        val idUsuarioLocal: Long,
        val mensaje: String
    ) : ResultadoRegistroApi()

    data class Error(
        val mensaje: String
    ) : ResultadoRegistroApi()
}