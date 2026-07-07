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

class HistorialCompletoFragment : Fragment(R.layout.activity_historial_completo) {

    private lateinit var rvHistorial: RecyclerView
    private val listaHistorialLocales = mutableListOf<CitaPacienteMock>()
    private lateinit var adapter: CitasAdapter
    private lateinit var citaRepository: CitaRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        citaRepository = CitaRepository(requireContext())
        rvHistorial = view.findViewById(R.id.rvHistorialCompleto)
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        adapter = CitasAdapter(listaHistorialLocales, esHistorial = true) {
        }
        rvHistorial.adapter = adapter

        cargarHistorialDesdePersistencia()
    }

    private fun cargarHistorialDesdePersistencia() {
        val preferencias = requireContext().getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
        val idPacienteLogueado = preferencias.getInt("ID_USUARIO", -1)

        if (idPacienteLogueado != -1) {
            val historialDb = citaRepository.obtenerHistorialCitasPorPaciente(idPacienteLogueado)

            listaHistorialLocales.clear()
            historialDb.forEach { cita ->
                listaHistorialLocales.add(
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