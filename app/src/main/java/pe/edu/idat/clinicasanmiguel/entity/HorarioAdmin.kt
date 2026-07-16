package pe.edu.idat.clinicasanmiguel.entity

data class HorarioAdmin(
    val id: Int,
    val idMedico: Int,
    val medico: String,
    val especialidad: String,
    val fechaHoraTexto: String,
    val estado: String
)