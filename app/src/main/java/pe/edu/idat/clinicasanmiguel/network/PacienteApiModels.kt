package pe.edu.idat.clinicasanmiguel.network

data class MedicoApiResponse(
    val id: Int,
    val nombre: String,
    val idEspecialidad: Int,
    val especialidad: String
)

data class CrearCitaApiRequest(
    val idMedico: Int,
    val fechaHora: String
)

data class CitaApiResponse(
    val id: Int,
    val idMedico: Int,
    val medico: String,
    val especialidad: String,
    val fechaHora: String,
    val estado: String
)

data class CancelarCitaApiResponse(
    val exito: Boolean,
    val mensaje: String,
    val idCita: Int,
    val estado: String
)

data class ReprogramarCitaApiRequest(
    val nuevoHorario: String
)
data class NotificacionApiResponse(
    val id: Int,
    val idCita: Int,
    val mensaje: String,
    val fechaHoraCita: String,
    val medico: String,
    val especialidad: String,
    val fechaGeneracion: String
)