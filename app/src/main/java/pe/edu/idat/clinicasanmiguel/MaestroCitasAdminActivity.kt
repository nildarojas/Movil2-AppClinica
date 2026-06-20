package pe.edu.idat.clinicasanmiguel
import pe.edu.idat.clinicasanmiguel.adapter.CitaGlobalMock
import pe.edu.idat.clinicasanmiguel.adapter.CitasGlobalAdminAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MaestroCitasAdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maestro_citas_admin)

        val rv = findViewById<RecyclerView>(R.id.rvCitasGlobalAdmin)
        rv.layoutManager = LinearLayoutManager(this)

        val citasSimuladas = listOf(
            CitaGlobalMock("Franklin Elias", "Dr. Bryant Yacila (Cardiología)", "2026-06-22 | 08:30 AM", "PENDIENTE"),
            CitaGlobalMock("Nilda Rojas", "Dra. Abigail Valdez (Pediatría)", "2026-06-23 | 10:15 AM", "CONFIRMADA"),
            CitaGlobalMock("Marcos Huamán", "Dr. Bryant Yacila (Cardiología)", "2026-06-24 | 04:00 PM", "CANCELADA")
        )

        rv.adapter = CitasGlobalAdminAdapter(citasSimuladas)
    }
}