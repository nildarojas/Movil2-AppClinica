package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class SeleccionarMedicoHorarioFragment : Fragment(R.layout.activity_seleccionar_medico_horario) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val acMedico = view.findViewById<AutoCompleteTextView>(R.id.acMedicoReserva)
        val acHorario = view.findViewById<AutoCompleteTextView>(R.id.acHorarioReserva)
        val btnConfirmar = view.findViewById<MaterialButton>(R.id.btnConfirmarReservaFinal)
        val especialidad = arguments?.getString("NOMBRE_ESPECIALIDAD") ?: ""
        val medicos = arrayOf("Dr. Bryant Yacila", "Dra. Abigail Valdez", "Dra. Nilda Rojas")
        val horarios = arrayOf("Lunes 08 de Junio - 08:30 AM", "Miércoles 10 de Junio - 10:15 AM", "Viernes 12 de Junio - 04:00 PM")

        acMedico.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_perfil_item, medicos))
        acHorario.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_perfil_item, horarios))

        acMedico.setOnClickListener { acMedico.showDropDown() }
        acHorario.setOnClickListener { acHorario.showDropDown() }

        btnConfirmar.setOnClickListener {
            Toast.makeText(requireContext(), "Cita en $especialidad registrada con éxito", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
}