package pe.edu.idat.clinicasanmiguel

import android.content.Context
import android.os.Bundle
import android.view.View
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
        val horarios = arrayOf("Lunes 08 de Junio - 08:30 AM", "Miércoles 10 de Junio - 10:15 AM", "Viernes 12 de Junio - 04:00 PM")

        acMedico.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_perfil_item, listaNombresMedicos))
        acHorario.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_perfil_item, horarios))

        acMedico.setOnClickListener { acMedico.showDropDown() }
        acHorario.setOnClickListener { acHorario.showDropDown() }

        btnConfirmar.setOnClickListener {
            val medicoSeleccionado = acMedico.text.toString()
            val horarioSeleccionado = acHorario.text.toString()

            if (medicoSeleccionado.isEmpty() || horarioSeleccionado.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, seleccione médico y horario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val preferencias = requireContext().getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
            val idPacienteLogueado = preferencias.getInt("ID_USUARIO", -1)
            val idMedicoSeleccionado = medicosData[medicoSeleccionado] ?: -1

            if (idPacienteLogueado != -1 && idMedicoSeleccionado != -1) {
                val idCitaGenerada = citaRepository.insertarCita(
                    idPaciente = idPacienteLogueado,
                    idMedico = idMedicoSeleccionado,
                    fechaHora = horarioSeleccionado
                )
                Toast.makeText(requireContext(), "Cita N° $idCitaGenerada en $nombreEspecialidad agendada con éxito", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } else {
                Toast.makeText(requireContext(), "Error de sesión. Vuelva a iniciar sesión.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}