package pe.edu.idat.clinicasanmiguel.ui

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class SeleccionarMedicoHorarioFragment :
    Fragment(R.layout.activity_seleccionar_medico_horario) {

    private lateinit var citaRepository: CitaRepository

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        citaRepository = CitaRepository(requireContext())

        val acMedico =
            view.findViewById<AutoCompleteTextView>(
                R.id.acMedicoReserva
            )

        val acHorario =
            view.findViewById<AutoCompleteTextView>(
                R.id.acHorarioReserva
            )

        val btnConfirmar =
            view.findViewById<MaterialButton>(
                R.id.btnConfirmarReservaFinal
            )

        val idEspecialidad =
            arguments?.getInt("ID_ESPECIALIDAD") ?: 1

        val nombreEspecialidad =
            arguments?.getString("NOMBRE_ESPECIALIDAD").orEmpty()

        val medicosData =
            citaRepository.obtenerMedicosPorEspecialidad(
                idEspecialidad
            )

        val listaNombresMedicos =
            medicosData.keys.toTypedArray()

        acMedico.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.spinner_perfil_item,
                listaNombresMedicos
            )
        )

        acMedico.setOnClickListener {
            acMedico.showDropDown()
        }

        acHorario.setOnClickListener {
            acHorario.showDropDown()
        }

        val preferencias =
            requireContext().getSharedPreferences(
                "sesion_clinica",
                Context.MODE_PRIVATE
            )

        val idPacienteLogueado =
            preferencias.getInt(
                "ID_USUARIO",
                -1
            )

        acMedico.setOnItemClickListener {
                parent,
                _,
                position,
                _ ->

            val medicoSeleccionado =
                parent
                    .getItemAtPosition(position)
                    .toString()

            val idMedicoSeleccionado =
                medicosData[medicoSeleccionado] ?: -1

            acHorario.setText("")
            acHorario.setAdapter(null)

            if (
                idPacienteLogueado == -1 ||
                idMedicoSeleccionado == -1
            ) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar la información del paciente o médico",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnItemClickListener
            }

            val horariosConEstado =
                citaRepository.obtenerHorariosConEstado(
                    idPaciente = idPacienteLogueado,
                    idMedico = idMedicoSeleccionado,
                    horarioOriginal = ""
                )

            if (horariosConEstado.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Este médico todavía no tiene horarios disponibles",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnItemClickListener
            }

            acHorario.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_perfil_item,
                    horariosConEstado
                )
            )

            acHorario.showDropDown()
        }

        btnConfirmar.setOnClickListener {

            val medicoSeleccionado =
                acMedico.text
                    .toString()
                    .trim()

            val horarioSeleccionado =
                acHorario.text
                    .toString()
                    .trim()

            if (
                medicoSeleccionado.isEmpty() ||
                horarioSeleccionado.isEmpty()
            ) {
                Toast.makeText(
                    requireContext(),
                    "Por favor, seleccione médico y horario",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (
                horarioSeleccionado.contains(
                    "(Ocupado por ti)"
                )
            ) {
                Toast.makeText(
                    requireContext(),
                    "No puedes seleccionar un horario donde ya tienes otra cita",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            if (
                horarioSeleccionado.contains(
                    "(Médico ocupado en este horario)"
                )
            ) {
                Toast.makeText(
                    requireContext(),
                    "Este horario no está disponible para el médico seleccionado",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val idMedicoSeleccionado =
                medicosData[medicoSeleccionado] ?: -1

            if (
                idPacienteLogueado == -1 ||
                idMedicoSeleccionado == -1
            ) {
                Toast.makeText(
                    requireContext(),
                    "Error de sesión. Vuelva a iniciar sesión.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val dialogCarga =
                ProgressDialog(requireContext()).apply {
                    setMessage("Registrando cita...")
                    setCancelable(false)
                    show()
                }

            btnConfirmar.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({

                if (!isAdded) {
                    dialogCarga.dismiss()
                    return@postDelayed
                }

                val idCitaGenerada =
                    citaRepository.insertarCita(
                        idPaciente = idPacienteLogueado,
                        idMedico = idMedicoSeleccionado,
                        fechaHora = horarioSeleccionado
                    )

                dialogCarga.dismiss()
                btnConfirmar.isEnabled = true

                if (idCitaGenerada > 0) {
                    Toast.makeText(
                        requireContext(),
                        "Cita N° $idCitaGenerada en $nombreEspecialidad agendada con éxito",
                        Toast.LENGTH_LONG
                    ).show()

                    parentFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo registrar la cita. Inténtelo nuevamente.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }, 1500)
        }
    }
}