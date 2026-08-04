package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.NotificacionUi
import pe.edu.idat.clinicasanmiguel.adapter.NotificacionesAdapter
import pe.edu.idat.clinicasanmiguel.network.NotificacionApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificacionesFragment :
    Fragment(R.layout.activity_notificaciones) {

    private lateinit var rvNotificaciones:
            RecyclerView

    private lateinit var tvEstado:
            TextView

    private lateinit var adapter:
            NotificacionesAdapter

    private val listaNotificaciones =
        mutableListOf<NotificacionUi>()

    private var cargando =
        false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        rvNotificaciones =
            view.findViewById(
                R.id.rvNotificaciones
            )

        tvEstado =
            view.findViewById(
                R.id.tvEstadoNotificaciones
            )

        rvNotificaciones.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        adapter =
            NotificacionesAdapter(
                listaNotificaciones
            )

        rvNotificaciones.adapter =
            adapter

        mostrarEstado(
            "Cargando notificaciones..."
        )
    }

    override fun onResume() {
        super.onResume()

        cargarNotificacionesDesdeApi()
    }

    private fun cargarNotificacionesDesdeApi() {
        if (cargando) {
            return
        }

        cargando = true

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .listarMisNotificaciones()
            .enqueue(
                object :
                    Callback<List<NotificacionApiResponse>> {

                    override fun onResponse(
                        call: Call<List<NotificacionApiResponse>>,
                        response: Response<List<NotificacionApiResponse>>
                    ) {
                        cargando = false

                        if (!isAdded) {
                            return
                        }

                        if (response.isSuccessful) {
                            val notificaciones =
                                response.body()
                                    ?: emptyList()

                            listaNotificaciones.clear()

                            listaNotificaciones.addAll(
                                notificaciones.map {
                                    NotificacionUi(
                                        id = it.id,
                                        idCita = it.idCita,
                                        mensaje = it.mensaje,
                                        fechaHoraCita =
                                            it.fechaHoraCita,
                                        medico = it.medico,
                                        especialidad =
                                            it.especialidad,
                                        fechaGeneracion =
                                            it.fechaGeneracion
                                    )
                                }
                            )

                            adapter.notifyDataSetChanged()

                            if (
                                listaNotificaciones.isEmpty()
                            ) {
                                mostrarEstado(
                                    "Aún no tienes notificaciones"
                                )
                            } else {
                                mostrarLista()
                            }

                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<List<NotificacionApiResponse>>,
                        throwable: Throwable
                    ) {
                        cargando = false

                        if (!isAdded) {
                            return
                        }

                        mostrarEstado(
                            "No se pudieron cargar las notificaciones"
                        )

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
        val mensaje =
            obtenerMensajeError(
                response
            )

        when (response.code()) {
            401 -> {
                cerrarSesion(
                    mensaje
                        ?: "Tu sesión ha vencido"
                )
            }

            403 -> {
                mostrarEstado(
                    "No tienes permiso para consultar las notificaciones"
                )

                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "Esta sección requiere una cuenta de paciente",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                mostrarEstado(
                    "No se pudieron cargar las notificaciones"
                )

                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "El servidor respondió con el código ${response.code()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun mostrarLista() {
        tvEstado.visibility =
            View.GONE

        rvNotificaciones.visibility =
            View.VISIBLE
    }

    private fun mostrarEstado(
        mensaje: String
    ) {
        tvEstado.text =
            mensaje

        tvEstado.visibility =
            View.VISIBLE

        rvNotificaciones.visibility =
            View.GONE
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