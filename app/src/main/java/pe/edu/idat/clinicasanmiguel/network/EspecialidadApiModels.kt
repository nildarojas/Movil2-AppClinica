package pe.edu.idat.clinicasanmiguel.network

data class EspecialidadApiResponse(
    val id: Int,
    val nombre: String
)

data class CrearEspecialidadApiRequest(
    val nombre: String
)