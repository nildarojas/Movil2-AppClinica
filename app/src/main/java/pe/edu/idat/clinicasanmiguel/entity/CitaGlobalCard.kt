package pe.edu.idat.clinicasanmiguel.entity

data class CitaGlobalCard(
    val idCita: Int,
    val paciente: String,
    val especialidad: String,
    val medico: String,
    val fechaHora: String,
    val estado: String
)