package pe.edu.idat.clinicasanmiguel.network

data class CrearMedicoApiRequest(
    val nombre: String,
    val idEspecialidad: Int,
    val correo: String,
    val password: String
)