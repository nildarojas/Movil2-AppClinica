package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock

class SeleccionarEspecialidadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_especialidad)

        val rv = findViewById<RecyclerView>(R.id.lvEspecialidadesReserva)
        rv.layoutManager = LinearLayoutManager(this)

        val lista = listOf(
            EspecialidadMock("Cardiología", "", ""),
            EspecialidadMock("Pediatría", "", "")
        )

        rv.adapter = EspecialidadAdminAdapter(lista, false) { especialidadSeleccionada ->

            val intent = Intent(this, SeleccionarMedicoHorarioActivity::class.java)
            intent.putExtra("NOMBRE_ESPECIALIDAD", especialidadSeleccionada.nombre)
            startActivity(intent)
            finish()
        }
    }
}