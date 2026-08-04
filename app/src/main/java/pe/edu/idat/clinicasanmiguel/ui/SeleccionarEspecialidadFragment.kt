package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock
import pe.edu.idat.clinicasanmiguel.network.EspecialidadApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeleccionarEspecialidadFragment :
    Fragment(R.layout.activity_seleccionar_especialidad) {

    private lateinit var rvEspecialidades: RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        rvEspecialidades =
            view.findViewById(
                R.id.lvEspecialidadesReserva
            )

        rvEspecialidades.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        cargarEspecialidadesDesdeApi()
    }

    private fun cargarEspecialidadesDesdeApi() {
        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .listarEspecialidades()
            .enqueue(
                object :
                    Callback<List<EspecialidadApiResponse>> {

                    override fun onResponse(
                        call: Call<List<EspecialidadApiResponse>>,
                        response: Response<List<EspecialidadApiResponse>>
                    ) {
                        if (!isAdded) {
                            return
                        }

                        if (response.isSuccessful) {
                            val especialidades =
                                response.body()
                                    ?: emptyList()

                            mostrarEspecialidades(
                                especialidades
                            )

                            if (especialidades.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No existen especialidades registradas en la nube",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        when (response.code()) {
                            401 -> {
                                cerrarSesion(
                                    obtenerMensajeError(response)
                                        ?: "Tu sesión ha vencido"
                                )
                            }

                            403 -> {
                                Toast.makeText(
                                    requireContext(),
                                    obtenerMensajeError(response)
                                        ?: "No tienes permiso para consultar las especialidades",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            else -> {
                                Toast.makeText(
                                    requireContext(),
                                    obtenerMensajeError(response)
                                        ?: "No se pudieron cargar las especialidades. Código ${response.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<List<EspecialidadApiResponse>>,
                        throwable: Throwable
                    ) {
                        if (!isAdded) {
                            return
                        }

                        Toast.makeText(
                            requireContext(),
                            "No se pudo conectar con el servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun mostrarEspecialidades(
        especialidades: List<EspecialidadApiResponse>
    ) {
        val lista =
            especialidades.map {
                EspecialidadMock(
                    id = it.id,
                    nombre = it.nombre,
                    area = "",
                    estado = ""
                )
            }

        rvEspecialidades.adapter =
            EspecialidadAdminAdapter(
                lista = lista,
                esModoAdmin = false
            ) { especialidadSeleccionada ->

                if (especialidadSeleccionada.id <= 0) {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo identificar la especialidad",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@EspecialidadAdminAdapter
                }

                val siguientePaso =
                    SeleccionarMedicoHorarioFragment()
                        .apply {
                            arguments =
                                Bundle().apply {
                                    putInt(
                                        "ID_ESPECIALIDAD",
                                        especialidadSeleccionada.id
                                    )

                                    putString(
                                        "NOMBRE_ESPECIALIDAD",
                                        especialidadSeleccionada.nombre
                                    )
                                }
                        }

                parentFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.flContenedor,
                        siguientePaso
                    )
                    .addToBackStack(null)
                    .commit()
            }
    }

    private fun obtenerMensajeError(
        response: Response<*>
    ): String? {
        return try {
            val contenido =
                response.errorBody()
                    ?.string()

            if (contenido.isNullOrBlank()) {
                null
            } else {
                JSONObject(contenido)
                    .optString("mensaje")
                    .takeIf {
                        it.isNotBlank()
                    }
            }
        } catch (exception: Exception) {
            null
        }
    }

    private fun cerrarSesion(
        mensaje: String
    ) {
        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_LONG
        ).show()

        SessionManager(
            requireContext()
        ).limpiarSesion()

        val intent =
            Intent(
                requireContext(),
                LoginActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(intent)
        requireActivity().finish()
    }
}