package pe.edu.idat.clinicasanmiguel.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
import pe.edu.idat.clinicasanmiguel.network.CrearCitaApiRequest
import pe.edu.idat.clinicasanmiguel.network.MedicoApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeleccionarMedicoHorarioFragment :
    Fragment(R.layout.activity_seleccionar_medico_horario) {

    private lateinit var acMedico: AutoCompleteTextView
    private lateinit var acHorario: AutoCompleteTextView
    private lateinit var btnConfirmar: MaterialButton

    private var medicos =
        emptyList<MedicoApiResponse>()

    private var idMedicoSeleccionado =
        -1

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        acMedico =
            view.findViewById(
                R.id.acMedicoReserva
            )

        acHorario =
            view.findViewById(
                R.id.acHorarioReserva
            )

        btnConfirmar =
            view.findViewById(
                R.id.btnConfirmarReservaFinal
            )

        val idEspecialidad =
            arguments?.getInt(
                "ID_ESPECIALIDAD",
                -1
            ) ?: -1

        val nombreEspecialidad =
            arguments?.getString(
                "NOMBRE_ESPECIALIDAD"
            ).orEmpty()

        if (idEspecialidad <= 0) {
            Toast.makeText(
                requireContext(),
                "La especialidad seleccionada no es válida",
                Toast.LENGTH_LONG
            ).show()

            parentFragmentManager.popBackStack()
            return
        }

        acMedico.setOnClickListener {
            acMedico.showDropDown()
        }

        acHorario.setOnClickListener {
            acHorario.showDropDown()
        }

        acMedico.setOnItemClickListener {
                _,
                _,
                position,
                _ ->

            if (position !in medicos.indices) {
                idMedicoSeleccionado = -1
                return@setOnItemClickListener
            }

            val medico =
                medicos[position]

            idMedicoSeleccionado =
                medico.id

            acHorario.setText(
                "",
                false
            )

            acHorario.setAdapter(null)

            cargarHorariosDesdeApi(
                medico.id
            )
        }

        btnConfirmar.setOnClickListener {
            reservarCita(
                nombreEspecialidad
            )
        }

        cargarMedicosDesdeApi(
            idEspecialidad
        )
    }

    private fun cargarMedicosDesdeApi(
        idEspecialidad: Int
    ) {
        acMedico.isEnabled = false
        acHorario.isEnabled = false
        btnConfirmar.isEnabled = false

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .listarMedicosPorEspecialidad(
                idEspecialidad
            )
            .enqueue(
                object :
                    Callback<List<MedicoApiResponse>> {

                    override fun onResponse(
                        call: Call<List<MedicoApiResponse>>,
                        response: Response<List<MedicoApiResponse>>
                    ) {
                        if (!isAdded) {
                            return
                        }

                        acMedico.isEnabled = true

                        if (response.isSuccessful) {
                            medicos =
                                response.body()
                                    ?: emptyList()

                            val nombres =
                                medicos.map {
                                    it.nombre
                                }

                            acMedico.setAdapter(
                                ArrayAdapter(
                                    requireContext(),
                                    R.layout.spinner_perfil_item,
                                    nombres
                                )
                            )

                            if (medicos.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No existen médicos registrados para esta especialidad",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<List<MedicoApiResponse>>,
                        throwable: Throwable
                    ) {
                        if (!isAdded) {
                            return
                        }

                        acMedico.isEnabled = true

                        Toast.makeText(
                            requireContext(),
                            "No se pudo conectar con el servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun cargarHorariosDesdeApi(
        idMedico: Int
    ) {
        acHorario.isEnabled = false
        btnConfirmar.isEnabled = false

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .listarHorariosConEstado(
                idMedico = idMedico
            )
            .enqueue(
                object :
                    Callback<List<String>> {

                    override fun onResponse(
                        call: Call<List<String>>,
                        response: Response<List<String>>
                    ) {
                        if (!isAdded) {
                            return
                        }

                        acHorario.isEnabled = true

                        if (response.isSuccessful) {
                            val horarios =
                                response.body()
                                    ?: emptyList()

                            acHorario.setAdapter(
                                ArrayAdapter(
                                    requireContext(),
                                    R.layout.spinner_perfil_item,
                                    horarios
                                )
                            )

                            if (horarios.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "Este médico no tiene horarios disponibles",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            btnConfirmar.isEnabled = true
                            acHorario.showDropDown()
                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<List<String>>,
                        throwable: Throwable
                    ) {
                        if (!isAdded) {
                            return
                        }

                        acHorario.isEnabled = true

                        Toast.makeText(
                            requireContext(),
                            "No se pudieron cargar los horarios",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun reservarCita(
        nombreEspecialidad: String
    ) {
        val medicoSeleccionado =
            acMedico.text
                .toString()
                .trim()

        val horarioSeleccionado =
            acHorario.text
                .toString()
                .trim()

        if (
            idMedicoSeleccionado <= 0 ||
            medicoSeleccionado.isEmpty() ||
            horarioSeleccionado.isEmpty()
        ) {
            Toast.makeText(
                requireContext(),
                "Selecciona un médico y un horario",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (
            horarioSeleccionado.contains(
                "(Ocupado por ti)"
            )
        ) {
            Toast.makeText(
                requireContext(),
                "Ya tienes una cita registrada en ese horario",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (
            horarioSeleccionado.contains(
                "(Médico ocupado en este horario)"
            )
        ) {
            Toast.makeText(
                requireContext(),
                "El médico ya tiene una cita en ese horario",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val dialogCarga =
            ProgressDialog(
                requireContext()
            ).apply {
                setMessage(
                    "Registrando cita..."
                )

                setCancelable(false)
                show()
            }

        btnConfirmar.isEnabled = false

        val request =
            CrearCitaApiRequest(
                idMedico =
                    idMedicoSeleccionado,
                fechaHora =
                    horarioSeleccionado
            )

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .reservarCita(request)
            .enqueue(
                object :
                    Callback<CitaApiResponse> {

                    override fun onResponse(
                        call: Call<CitaApiResponse>,
                        response: Response<CitaApiResponse>
                    ) {
                        dialogCarga.dismiss()

                        if (!isAdded) {
                            return
                        }

                        btnConfirmar.isEnabled = true

                        if (response.isSuccessful) {
                            val cita =
                                response.body()

                            if (cita == null) {
                                Toast.makeText(
                                    requireContext(),
                                    "La API devolvió una respuesta incompleta",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            val especialidad =
                                nombreEspecialidad
                                    .ifBlank {
                                        cita.especialidad
                                    }

                            Toast.makeText(
                                requireContext(),
                                "Cita N° ${cita.id} en $especialidad registrada correctamente",
                                Toast.LENGTH_LONG
                            ).show()

                            abrirMisCitas()
                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<CitaApiResponse>,
                        throwable: Throwable
                    ) {
                        dialogCarga.dismiss()

                        if (!isAdded) {
                            return
                        }

                        btnConfirmar.isEnabled = true

                        Toast.makeText(
                            requireContext(),
                            "No se pudo registrar la cita",
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
                        ?: "Esta acción requiere una cuenta de paciente",
                    Toast.LENGTH_LONG
                ).show()
            }

            404 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "No se encontró la información solicitada",
                    Toast.LENGTH_LONG
                ).show()
            }

            409 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "El horario seleccionado ya no está disponible",
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

    private fun abrirMisCitas() {
        if (!isAdded) {
            return
        }

        val fragmentManager =
            parentFragmentManager

        fragmentManager.popBackStackImmediate(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        fragmentManager
            .beginTransaction()
            .replace(
                R.id.flContenedor,
                MisCitasFragment()
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