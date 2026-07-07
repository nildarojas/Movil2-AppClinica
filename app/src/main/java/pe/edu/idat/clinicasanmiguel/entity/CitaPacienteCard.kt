package pe.edu.idat.clinicasanmiguel.entity

data class CitaPacienteCard(
    val idCita: Int,
    val especialidad: String,
    val medico: String,
    val fechaHora: String,
    val estado: String
)