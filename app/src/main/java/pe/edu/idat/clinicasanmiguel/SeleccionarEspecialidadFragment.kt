package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock

class SeleccionarEspecialidadFragment : Fragment(R.layout.activity_seleccionar_especialidad) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.lvEspecialidadesReserva)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val lista = listOf(
            EspecialidadMock("Cardiología", "", ""),
            EspecialidadMock("Pediatría", "", "")
        )

        rv.adapter = EspecialidadAdminAdapter(lista, false) { especialidadSeleccionada ->
            val siguientePaso = SeleccionarMedicoHorarioFragment().apply {
                arguments = Bundle().apply {
                    putString("NOMBRE_ESPECIALIDAD", especialidadSeleccionada.nombre)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.flContenedor, siguientePaso)
                .addToBackStack(null)
                .commit()
        }
    }
}