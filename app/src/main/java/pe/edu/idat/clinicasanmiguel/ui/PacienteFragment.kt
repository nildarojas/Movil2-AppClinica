package pe.edu.idat.clinicasanmiguel.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PacienteFragment :
    Fragment(R.layout.activity_paciente) {

    private lateinit var tvSaludoBienvenida:
            TextView

    private lateinit var layoutVacio:
            LinearLayout

    private lateinit var layoutCita:
            LinearLayout

    private lateinit var btnHorarios:
            MaterialButton

    private lateinit var btnHorariosVacio:
            MaterialButton

    private lateinit var tvHomeEspecialidad:
            TextView

    private lateinit var tvHomeMedico:
            TextView

    private lateinit var tvHomeFechaHora:
            TextView

    private lateinit var tvHomeEstado:
            TextView

    private var cargandoUltimaCita =
        false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        tvSaludoBienvenida =
            view.findViewById(
                R.id.tvSaludoBienvenida
            )

        val ivCampanaNotificacion =
            view.findViewById<ImageView>(
                R.id.ivCampanaNotificacion
            )

        btnHorarios =
            view.findViewById(
                R.id.btnHorarios
            )

        btnHorariosVacio =
            view.findViewById(
                R.id.btnHorariosVacio
            )

        val btnVerOtrasCitas =
            view.findViewById<MaterialButton>(
                R.id.btnVerOtrasCitas
            )

        layoutVacio =
            view.findViewById(
                R.id.layoutEstadoVacio
            )

        layoutCita =
            view.findViewById(
                R.id.layoutUltimaCita
            )

        tvHomeEspecialidad =
            view.findViewById(
                R.id.tvHomeEspecialidad
            )

        tvHomeMedico =
            view.findViewById(
                R.id.tvHomeMedico
            )

        tvHomeFechaHora =
            view.findViewById(
                R.id.tvHomeFechaHora
            )

        tvHomeEstado =
            view.findViewById(
                R.id.tvHomeEstado
            )

        val preferencias =
            requireContext()
                .getSharedPreferences(
                    "sesion_clinica",
                    Context.MODE_PRIVATE
                )

        val nombreUsuario =
            preferencias.getString(
                "NOMBRE_USUARIO",
                "Paciente"
            ) ?: "Paciente"

        tvSaludoBienvenida.text =
            "¡Bienvenido,\n$nombreUsuario!"

        btnHorarios.setOnClickListener {
            cambiarPantallaDesdeInicio(
                SeleccionarEspecialidadFragment()
            )
        }

        btnHorariosVacio.setOnClickListener {
            cambiarPantallaDesdeInicio(
                SeleccionarEspecialidadFragment()
            )
        }

        btnVerOtrasCitas.setOnClickListener {
            cambiarPantallaDesdeInicio(
                MisCitasFragment()
            )
        }

        ivCampanaNotificacion.setOnClickListener {
            cambiarPantallaDesdeInicio(
                NotificacionesFragment()
            )
        }

        mostrarCargando()
    }

    override fun onResume() {
        super.onResume()

        cargarUltimaCitaDesdeApi()
    }

    private fun cargarUltimaCitaDesdeApi() {
        if (cargandoUltimaCita) {
            return
        }

        cargandoUltimaCita = true

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .obtenerUltimaCita()
            .enqueue(
                object :
                    Callback<CitaApiResponse> {

                    override fun onResponse(
                        call: Call<CitaApiResponse>,
                        response: Response<CitaApiResponse>
                    ) {
                        cargandoUltimaCita = false

                        if (!isAdded) {
                            return
                        }

                        if (response.isSuccessful) {
                            val ultimaCita =
                                response.body()

                            if (ultimaCita == null) {
                                mostrarEstadoVacio()
                            } else {
                                mostrarUltimaCita(
                                    ultimaCita
                                )
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
                                mostrarEstadoVacio()

                                Toast.makeText(
                                    requireContext(),
                                    obtenerMensajeError(response)
                                        ?: "No tienes permiso para consultar las citas",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            404 -> {
                                mostrarEstadoVacio()
                            }

                            else -> {
                                mostrarEstadoVacio()

                                Toast.makeText(
                                    requireContext(),
                                    obtenerMensajeError(response)
                                        ?: "No se pudo cargar la última cita. Código ${response.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<CitaApiResponse>,
                        throwable: Throwable
                    ) {
                        cargandoUltimaCita = false

                        if (!isAdded) {
                            return
                        }

                        mostrarEstadoVacio()

                        Toast.makeText(
                            requireContext(),
                            "No se pudo conectar con el servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun mostrarUltimaCita(
        cita: CitaApiResponse
    ) {
        layoutVacio.visibility =
            View.GONE

        layoutCita.visibility =
            View.VISIBLE

        btnHorarios.visibility =
            View.VISIBLE

        btnHorariosVacio.visibility =
            View.GONE

        tvHomeEspecialidad.text =
            cita.especialidad

        tvHomeMedico.text =
            "Médico: ${cita.medico}"

        tvHomeFechaHora.text =
            "Horario: ${cita.fechaHora}"

        tvHomeEstado.text =
            cita.estado
    }

    private fun mostrarEstadoVacio() {
        layoutCita.visibility =
            View.GONE

        layoutVacio.visibility =
            View.VISIBLE

        btnHorarios.visibility =
            View.GONE

        btnHorariosVacio.visibility =
            View.VISIBLE

        tvHomeEspecialidad.text =
            ""

        tvHomeMedico.text =
            ""

        tvHomeFechaHora.text =
            ""

        tvHomeEstado.text =
            ""
    }

    private fun mostrarCargando() {
        layoutCita.visibility =
            View.GONE

        layoutVacio.visibility =
            View.GONE

        btnHorarios.visibility =
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

    private fun cambiarPantallaDesdeInicio(
        fragment: Fragment
    ) {
        if (!isAdded) {
            return
        }

        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(
                R.id.flContenedor,
                fragment
            )
            .addToBackStack(
                fragment::class.java.simpleName
            )
            .commit()
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