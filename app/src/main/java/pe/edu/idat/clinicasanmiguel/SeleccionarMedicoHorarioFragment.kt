package pe.edu.idat.clinicasanmiguel

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class SeleccionarMedicoHorarioFragment : Fragment(R.layout.activity_seleccionar_medico_horario) {

    private lateinit var citaRepository: CitaRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        citaRepository = CitaRepository(requireContext())

        val acMedico = view.findViewById<AutoCompleteTextView>(R.id.acMedicoReserva)
        val acHorario = view.findViewById<AutoCompleteTextView>(R.id.acHorarioReserva)
        val btnConfirmar = view.findViewById<MaterialButton>(R.id.btnConfirmarReservaFinal)

        val idEspecialidad = arguments?.getInt("ID_ESPECIALIDAD") ?: 1
        val nombreEspecialidad = arguments?.getString("NOMBRE_ESPECIALIDAD") ?: ""
        val medicosData = citaRepository.obtenerMedicosPorEspecialidad(idEspecialidad)
        val listaNombresMedicos = medicosData.keys.toTypedArray()

        acMedico.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_perfil_item, listaNombresMedicos))

        acMedico.setOnClickListener { acMedico.showDropDown() }
        acHorario.setOnClickListener { acHorario.showDropDown() }

        val preferencias = requireContext().getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
        val idPacienteLogueado = preferencias.getInt("ID_USUARIO", -1)

        acMedico.setOnItemClickListener { parent, _, position, id ->
            val medicoSeleccionado = parent.getItemAtPosition(position).toString()
            val idMedicoSeleccionado = medicosData[medicoSeleccionado] ?: -1
            acHorario.setText("")

            if (idPacienteLogueado != -1 && idMedicoSeleccionado != -1) {
                val horariosConEstado = citaRepository.obtenerHorariosConEstado(idPacienteLogueado, idMedicoSeleccionado, "")
                acHorario.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_perfil_item, horariosConEstado))
            }
        }

        btnConfirmar.setOnClickListener {
            val medicoSeleccionado = acMedico.text.toString()
            val horarioSeleccionado = acHorario.text.toString()

            if (medicoSeleccionado.isEmpty() || horarioSeleccionado.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, seleccione médico y horario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (horarioSeleccionado.contains("(Ocupado por ti)")) {
                Toast.makeText(requireContext(), "No puedes seleccionar un horario donde ya tienes otra cita", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (horarioSeleccionado.contains("(Médico ocupado en este horario)")) {
                Toast.makeText(requireContext(), "Este horario no está disponible para el médico seleccionado", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val idMedicoSeleccionado = medicosData[medicoSeleccionado] ?: -1

            if (idPacienteLogueado != -1 && idMedicoSeleccionado != -1) {

                val dialogCarga = ProgressDialog(requireContext()).apply {
                    setMessage("Registrando cita...")
                    setCancelable(false)
                    show()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    val idCitaGenerada = citaRepository.insertarCita(
                        idPaciente = idPacienteLogueado,
                        idMedico = idMedicoSeleccionado,
                        fechaHora = horarioSeleccionado
                    )

                    dialogCarga.dismiss()

                    Toast.makeText(requireContext(), "Cita N° $idCitaGenerada en $nombreEspecialidad agendada con éxito", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }, 1500)

            } else {
                Toast.makeText(requireContext(), "Error de sesión. Vuelva a iniciar sesión.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}