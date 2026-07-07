package pe.edu.idat.clinicasanmiguel

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.adapter.CitaPacienteMock
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class MisCitasFragment : Fragment(R.layout.activity_mis_citas) {

    private lateinit var rvMisCitas: RecyclerView
    private val listaCitasLocales = mutableListOf<CitaPacienteMock>()
    private lateinit var adapter: CitasAdapter
    private lateinit var citaRepository: CitaRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        citaRepository = CitaRepository(requireContext())
        rvMisCitas = view.findViewById(R.id.rvMisCitas)
        rvMisCitas.layoutManager = LinearLayoutManager(requireContext())

        adapter = CitasAdapter(listaCitasLocales)
        rvMisCitas.adapter = adapter
    }
    override fun onResume() {
        super.onResume()
        cargarCitasDesdePersistencia()
    }

    private fun cargarCitasDesdePersistencia() {
        val preferencias = requireContext().getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
        val idPacienteLogueado = preferencias.getInt("ID_USUARIO", -1)

        if (idPacienteLogueado != -1) {
            val citasDb = citaRepository.obtenerCitasActivasPorPaciente(idPacienteLogueado)

            listaCitasLocales.clear()
            citasDb.forEach { cita ->
                listaCitasLocales.add(
                    CitaPacienteMock(
                        id = cita.idCita,
                        especialidad = cita.especialidad,
                        medico = cita.medico,
                        fechaHora = cita.fechaHora,
                        estado = cita.estado
                    )
                )
            }
            adapter.notifyDataSetChanged()
        }
    }
}