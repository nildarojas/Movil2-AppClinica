package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.CitaGlobalMock
import pe.edu.idat.clinicasanmiguel.adapter.CitasGlobalAdminAdapter
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class MaestroCitasAdminActivity : AppCompatActivity() {

    private lateinit var rvCitasGlobales: RecyclerView
    private lateinit var citaRepository: CitaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maestro_citas_admin)

        rvCitasGlobales =
            findViewById(R.id.rvCitasGlobalAdmin)

        rvCitasGlobales.layoutManager =
            LinearLayoutManager(this)

        citaRepository =
            CitaRepository(this)
    }

    override fun onResume() {
        super.onResume()

        cargarCitasDesdeSQLite()
    }

    private fun cargarCitasDesdeSQLite() {
        val citasReales =
            citaRepository.obtenerTodasLasCitas()

        val citasParaMostrar =
            citasReales.map { cita ->

                CitaGlobalMock(
                    cita.paciente,

                    "${cita.medico} (${cita.especialidad})",

                    cita.fechaHora,

                    cita.estado
                )
            }

        rvCitasGlobales.adapter =
            CitasGlobalAdminAdapter(
                citasParaMostrar
            )
    }
}