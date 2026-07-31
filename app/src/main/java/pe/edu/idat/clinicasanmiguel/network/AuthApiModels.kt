package pe.edu.idat.clinicasanmiguel.network

data class LoginApiRequest(
    val correo: String,
    val password: String
)

data class UsuarioLoginApi(
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

data class LoginApiResponse(
    val exito: Boolean,
    val mensaje: String,
    val token: String?,
    val usuario: UsuarioLoginApi?
)

data class RegistroApiRequest(
    val dni: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val password: String,
    val telefono: String,
    val fechaNacimiento: String,
    val genero: String
)

data class RegistroApiResponse(
    val exito: Boolean,
    val mensaje: String,
    val idUsuario: Int?
)