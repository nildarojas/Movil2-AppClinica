package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.NotificacionesAdapter
import pe.edu.idat.clinicasanmiguel.adapter.NotificacionMock

class NotificacionesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        val rv = findViewById<RecyclerView>(R.id.rvNotificaciones)
        rv.layoutManager = LinearLayoutManager(this)

        val listaNotificaciones = listOf(
            NotificacionMock(1, "Recordatorio: Cita de Cardiología mañana a las 9:30 AM", "2026-06-25"),
            NotificacionMock(2, "Actualización: La Dra. Abigail ha sido asignada a tu cita", "2026-06-24")
        )

        rv.adapter = NotificacionesAdapter(listaNotificaciones)
    }
}