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
import pe.edu.idat.clinicasanmiguel.adapter.CitaPacienteMock
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.network.CancelarCitaApiResponse
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import pe.edu.idat.clinicasanmiguel.ReprogramarCitaActivity

class MisCitasFragment :
    Fragment(R.layout.activity_mis_citas) {

    private lateinit var rvMisCitas:
            RecyclerView

    private lateinit var adapter:
            CitasAdapter

    private val listaCitas =
        mutableListOf<CitaPacienteMock>()

    private var cancelandoCita =
        false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        rvMisCitas =
            view.findViewById(
                R.id.rvMisCitas
            )

        rvMisCitas.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        adapter =
            CitasAdapter(
                lista = listaCitas,
                esHistorial = false,
                onCancelarCita = { cita ->
                    cancelarCitaDesdeApi(
                        cita.id
                    )
                },
                onReprogramarCita = { cita ->
                    abrirReprogramacion(
                        cita
                    )
                }


            )



        rvMisCitas.adapter =
            adapter
    }



    override fun onResume() {
        super.onResume()

        cargarCitasDesdeApi()
    }

    private fun cargarCitasDesdeApi() {
        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .listarCitasActivas()
            .enqueue(
                object :
                    Callback<List<CitaApiResponse>> {

                    override fun onResponse(
                        call:
                        Call<List<CitaApiResponse>>,
                        response:
                        Response<List<CitaApiResponse>>
                    ) {
                        if (!isAdded) {
                            return
                        }

                        if (response.isSuccessful) {
                            val citasApi =
                                response.body()
                                    ?: emptyList()

                            listaCitas.clear()

                            listaCitas.addAll(
                                citasApi.map { cita ->
                                    CitaPacienteMock(
                                        id = cita.id,
                                        especialidad = cita.especialidad,
                                        medico = cita.medico,
                                        fechaHora = cita.fechaHora,
                                        estado = cita.estado,
                                        idMedico = cita.idMedico
                                    )
                                }
                            )

                            adapter.notifyDataSetChanged()

                            if (listaCitas.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No tienes citas activas",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call:
                        Call<List<CitaApiResponse>>,
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

    private fun abrirReprogramacion(
        cita: CitaPacienteMock
    ) {
        if (cita.id <= 0 || cita.idMedico <= 0) {
            Toast.makeText(
                requireContext(),
                "No se pudo identificar la cita o el médico",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val intent =
            Intent(
                requireContext(),
                ReprogramarCitaActivity::class.java
            ).apply {
                putExtra(
                    "id_cita",
                    cita.id
                )

                putExtra(
                    "id_medico",
                    cita.idMedico
                )

                putExtra(
                    "especialidad",
                    cita.especialidad
                )

                putExtra(
                    "medico",
                    cita.medico
                )

                putExtra(
                    "fecha_hora",
                    cita.fechaHora
                )
            }

        startActivity(intent)
    }

    private fun cancelarCitaDesdeApi(
        idCita: Int
    ) {
        if (
            cancelandoCita ||
            idCita <= 0
        ) {
            return
        }

        cancelandoCita = true

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .cancelarCita(idCita)
            .enqueue(
                object :
                    Callback<CancelarCitaApiResponse> {

                    override fun onResponse(
                        call:
                        Call<CancelarCitaApiResponse>,
                        response:
                        Response<CancelarCitaApiResponse>
                    ) {
                        cancelandoCita = false

                        if (!isAdded) {
                            return
                        }

                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (
                                respuesta != null &&
                                !respuesta.exito
                            ) {
                                Toast.makeText(
                                    requireContext(),
                                    respuesta.mensaje,
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            Toast.makeText(
                                requireContext(),
                                respuesta?.mensaje
                                    ?: "Cita cancelada correctamente",
                                Toast.LENGTH_LONG
                            ).show()

                            cargarCitasDesdeApi()
                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call:
                        Call<CancelarCitaApiResponse>,
                        throwable: Throwable
                    ) {
                        cancelandoCita = false

                        if (!isAdded) {
                            return
                        }

                        Toast.makeText(
                            requireContext(),
                            "No se pudo cancelar la cita",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun procesarErrorRespuesta(
        response: Response<*>
    ) {
        if (!isAdded) {
            return
        }

        val mensaje =
            obtenerMensajeError(response)

        when (response.code()) {
            400 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "La cita no puede cancelarse",
                    Toast.LENGTH_LONG
                ).show()
            }

            401 -> {
                cerrarSesion(
                    mensaje
                        ?: "Tu sesión ha vencido"
                )
            }

            403 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "No tienes permiso para realizar esta operación",
                    Toast.LENGTH_LONG
                ).show()
            }

            404 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "La cita no fue encontrada",
                    Toast.LENGTH_LONG
                ).show()
            }

            409 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "La cita ya cambió de estado",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "El servidor respondió con el código ${response.code()}",
                    Toast.LENGTH_LONG
                ).show()
            }
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