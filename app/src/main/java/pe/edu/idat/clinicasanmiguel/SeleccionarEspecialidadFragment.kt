package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class SeleccionarEspecialidadFragment : Fragment(R.layout.activity_seleccionar_especialidad) {

    private lateinit var citaRepository: CitaRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        citaRepository = CitaRepository(requireContext())

        val rv = view.findViewById<RecyclerView>(R.id.lvEspecialidadesReserva)
        rv.layoutManager = LinearLayoutManager(requireContext())
        val especialidadesDb = citaRepository.obtenerEspecialidades()

        val listaMock = especialidadesDb.map {
            EspecialidadMock(it.nombre, "", "")
        }


        rv.adapter = EspecialidadAdminAdapter(listaMock, false) { especialidadSeleccionada ->
            val idReal = especialidadesDb.find { it.nombre == especialidadSeleccionada.nombre }?.id ?: 1

            val siguientePaso = SeleccionarMedicoHorarioFragment().apply {
                arguments = Bundle().apply {
                    putInt("ID_ESPECIALIDAD", idReal)
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