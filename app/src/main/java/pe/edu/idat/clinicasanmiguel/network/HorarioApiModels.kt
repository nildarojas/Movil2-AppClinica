package pe.edu.idat.clinicasanmiguel.network

data class HorarioApiResponse(
    val id: Int,
    val idMedico: Int,
    val medico: String,
    val fechaHoraTexto: String,
    val estado: String
)

data class CrearHorarioApiRequest(
    val idMedico: Int,
    val fechaHoraTexto: String
)