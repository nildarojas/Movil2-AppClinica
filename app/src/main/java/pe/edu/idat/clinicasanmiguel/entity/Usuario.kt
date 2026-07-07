package pe.edu.idat.clinicasanmiguel.entity

data class Usuario(
    val id: Int = 0,
    val dni: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val telefono: String,
    val fechaNacimiento: String,
    val genero: String,
    val rol: String = "PACIENTE"
)