package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.adapter.CitaPacienteMock

class HistorialCompletoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_completo)

        val rv = findViewById<RecyclerView>(R.id.rvHistorialCompleto)
        rv.layoutManager = LinearLayoutManager(this)

        val datosPasados = listOf(
            CitaPacienteMock(101, "Pediatría", "Dra. Abigail Valdez", "2026-05-10 | 10:00 AM", "CONFIRMADA"),
            CitaPacienteMock(102, "Cardiología", "Dr. Bryant Yacila", "2026-06-01 | 04:30 PM", "CANCELADA")
        )

        rv.adapter = CitasAdapter(datosPasados, true)
    }
}