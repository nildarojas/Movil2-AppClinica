package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.adapter.CitaMock

class MisCitasActivity : AppCompatActivity() {

    private lateinit var rvMisCitas: RecyclerView
    private lateinit var adapter: CitasAdapter

    private val listaCitasLocales = mutableListOf<CitaMock>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas)

        rvMisCitas = findViewById(R.id.rvMisCitas)
        rvMisCitas.layoutManager = LinearLayoutManager(this)

        if (listaCitasLocales.isEmpty()) {
            inicializarDatosEstaticos()
        }

        adapter = CitasAdapter(
            listaCitasLocales,
            onCancelarClick = { posicion ->
                mostrarConfirmacionCancelacion(posicion)
            },
            onReprogramarClick = { cita ->
                Toast.makeText(this, "Redirección a Reprogramar: ${cita.especialidad} (Sprint 2)", Toast.LENGTH_SHORT).show()
            }
        )

        rvMisCitas.adapter = adapter
    }

    private fun inicializarDatosEstaticos() {
        listaCitasLocales.add(CitaMock("Cardiología", "Dr. Bryant Yacila", "Fecha: 2026-06-04 | Hora: 08:30 AM", "PENDIENTE"))
        listaCitasLocales.add(CitaMock("Pediatría", "Dra. Abigail Valdez", "Fecha: 2026-06-10 | Hora: 10:15 AM", "CONFIRMADA"))
        listaCitasLocales.add(CitaMock("Medicina General", "Dra. Nilda Rojas", "Fecha: 2026-05-28 | Hora: 04:00 PM", "CANCELADA"))
    }

    private fun mostrarConfirmacionCancelacion(posicion: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar cancelación")
            .setMessage("¿Seguro que deseas cancelar esta cita?")
            .setPositiveButton("Sí") { _, _ ->
                val citaSeleccionada = listaCitasLocales[posicion]
                listaCitasLocales[posicion] = citaSeleccionada.copy(estado = "CANCELADA")

                adapter.notifyItemChanged(posicion)
                Toast.makeText(this, "Cita cancelada localmente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
}