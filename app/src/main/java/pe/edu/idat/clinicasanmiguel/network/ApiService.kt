package pe.edu.idat.clinicasanmiguel.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
}