package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.CitasGlobalAdminAdapter
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class CitasGlobalesFragment : Fragment() {

    private lateinit var rvCitasGlobales: RecyclerView
    private lateinit var citaRepository: CitaRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_maestro_citas_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCitasGlobales = view.findViewById(R.id.rvCitasGlobalAdmin)
        rvCitasGlobales.layoutManager = LinearLayoutManager(requireContext())

        citaRepository = CitaRepository(requireContext())
    }

    override fun onResume() {
        super.onResume()
        cargarCitasDesdeSQLite()
    }

    private fun cargarCitasDesdeSQLite() {
        val citasReales = citaRepository.obtenerTodasLasCitas()
        rvCitasGlobales.adapter = CitasGlobalAdminAdapter(citasReales)
    }
}