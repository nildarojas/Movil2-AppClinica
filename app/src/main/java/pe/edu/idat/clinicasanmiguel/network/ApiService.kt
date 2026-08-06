package pe.edu.idat.clinicasanmiguel.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    fun login(
        @Body request: LoginApiRequest
    ): Call<LoginApiResponse>

    @POST("api/auth/register")
    fun registrar(
        @Body request: RegistroApiRequest
    ): Call<RegistroApiResponse>

    @GET("api/especialidades/listar")
    fun listarEspecialidades():
            Call<List<EspecialidadApiResponse>>

    @POST("api/especialidades/registrar")
    fun registrarEspecialidad(
        @Body request: CrearEspecialidadApiRequest
    ): Call<EspecialidadApiResponse>

    @GET("api/medicos/filtrar")
    fun listarMedicosPorEspecialidad(
        @Query("id_especialidad")
        idEspecialidad: Int
    ): Call<List<MedicoApiResponse>>

    @GET("api/horarios/con-estado")
    fun listarHorariosConEstado(
        @Query("id_medico")
        idMedico: Int,

        @Query("horario_original")
        horarioOriginal: String? = null
    ): Call<List<String>>

    @POST("api/citas/reservar")
    fun reservarCita(
        @Body request: CrearCitaApiRequest
    ): Call<CitaApiResponse>

    @GET("api/citas/activas")
    fun listarCitasActivas():
            Call<List<CitaApiResponse>>

    @GET("api/citas/ultima")
    fun obtenerUltimaCita():
            Call<CitaApiResponse>

    @PUT("api/citas/{id}/cancelar")
    fun cancelarCita(
        @Path("id") idCita: Int
    ): Call<CancelarCitaApiResponse>

    @GET("api/citas/historial")
    fun listarHistorialCitas():
            Call<List<CitaApiResponse>>

    @PUT("api/citas/{id}/reprogramar")
    fun reprogramarCita(
        @Path("id") idCita: Int,
        @Body request: ReprogramarCitaApiRequest
    ): Call<CitaApiResponse>

    @GET("api/notificaciones/mias")
    fun listarMisNotificaciones():
            Call<List<NotificacionApiResponse>>

    @PUT("api/auth/cambiar-password")
    fun cambiarPassword(
        @Body request: CambiarPasswordApiRequest
    ): Call<CambiarPasswordApiResponse>

    @POST("api/auth/solicitar-recuperacion")
    fun solicitarRecuperacion(
        @Body request: SolicitarRecuperacionApiRequest
    ): Call<SolicitarRecuperacionApiResponse>

    @POST("api/auth/verificar-codigo-recuperacion")
    fun verificarCodigoRecuperacion(
        @Body request: VerificarCodigoRecuperacionApiRequest
    ): Call<RecuperacionPasswordApiResponse>

    @POST("api/auth/resetear-password")
    fun resetearPassword(
        @Body request: ResetearPasswordApiRequest
    ): Call<RecuperacionPasswordApiResponse>

    @GET("api/auth/perfil")
    fun obtenerPerfil():
            Call<UsuarioLoginApi>

    @PUT("api/auth/perfil")
    fun actualizarPerfil(
        @Body request: ActualizarPerfilApiRequest
    ): Call<UsuarioLoginApi>
    @GET("api/medicos/listar")
    fun listarMedicosAdmin():
            Call<List<MedicoApiResponse>>

    @POST("api/medicos/registrar")
    fun registrarMedicoAdmin(
        @Body request: CrearMedicoApiRequest
    ): Call<MedicoApiResponse>

    @GET("api/horarios/listar")
    fun listarHorariosAdmin():
            Call<List<HorarioApiResponse>>

    @POST("api/horarios/registrar")
    fun registrarHorarioAdmin(
        @Body request: CrearHorarioApiRequest
    ): Call<HorarioApiResponse>
    @GET("api/usuarios/listar")
    fun listarUsuariosAdmin():
            Call<List<UsuarioListadoApiResponse>>

    @GET("api/citas/todas")
    fun listarCitasGlobalesAdmin():
            Call<List<CitaGlobalApiResponse>>

    @GET("api/recetas/mias")
    fun listarMisRecetas():
            Call<List<RecetaApiResponse>>
}
