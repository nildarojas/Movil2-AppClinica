package pe.edu.idat.clinicasanmiguel.network

data class RecetaApiResponse(
    val idConsulta: Int,
    val idCita: Int,
    val medico: String,
    val especialidad: String,
    val fechaHoraCita: String,
    val fechaAtencion: String,
    val diagnostico: String,
    val medicamentos: List<String>
)