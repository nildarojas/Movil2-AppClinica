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
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistorialCompletoFragment :
    Fragment(R.layout.activity_historial_completo) {

    private lateinit var rvHistorial:
            RecyclerView

    private lateinit var adapter:
            CitasAdapter

    private val listaHistorial =
        mutableListOf<CitaPacienteMock>()

    private var cargandoHistorial =
        false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        rvHistorial =
            view.findViewById(
                R.id.rvHistorialCompleto
            )

        rvHistorial.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        adapter =
            CitasAdapter(
                lista = listaHistorial,
                esHistorial = true,
                onCancelarCita = {}
            )

        rvHistorial.adapter =
            adapter
    }

    override fun onResume() {
        super.onResume()

        cargarHistorialDesdeApi()
    }

    private fun cargarHistorialDesdeApi() {
        if (cargandoHistorial) {
            return
        }

        cargandoHistorial = true

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .listarHistorialCitas()
            .enqueue(
                object :
                    Callback<List<CitaApiResponse>> {

                    override fun onResponse(
                        call: Call<List<CitaApiResponse>>,
                        response: Response<List<CitaApiResponse>>
                    ) {
                        cargandoHistorial = false

                        if (!isAdded) {
                            return
                        }

                        if (response.isSuccessful) {
                            val historialApi =
                                response.body()
                                    ?: emptyList()

                            listaHistorial.clear()

                            listaHistorial.addAll(
                                historialApi.map { cita ->
                                    CitaPacienteMock(
                                        id = cita.id,
                                        especialidad =
                                            cita.especialidad,
                                        medico =
                                            cita.medico,
                                        fechaHora =
                                            cita.fechaHora,
                                        estado =
                                            cita.estado
                                    )
                                }
                            )

                            adapter.notifyDataSetChanged()

                            if (listaHistorial.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No tienes citas en el historial",
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
                        call: Call<List<CitaApiResponse>>,
                        throwable: Throwable
                    ) {
                        cargandoHistorial = false

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

    private fun procesarErrorRespuesta(
        response: Response<*>
    ) {
        if (!isAdded) {
            return
        }

        val mensaje =
            obtenerMensajeError(response)

        when (response.code()) {
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
                        ?: "No tienes permiso para consultar el historial",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "No se pudo cargar el historial. Código ${response.code()}",
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