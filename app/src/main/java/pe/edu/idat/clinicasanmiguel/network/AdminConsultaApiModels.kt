package pe.edu.idat.clinicasanmiguel.network

data class UsuarioListadoApiResponse(
    val id: Int,
    val dni: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val fechaNacimiento: String,
    val genero: String,
    val rol: String
)

data class CitaGlobalApiResponse(
    val idCita: Int,
    val paciente: String,
    val especialidad: String,
    val medico: String,
    val fechaHora: String,
    val estado: String
)