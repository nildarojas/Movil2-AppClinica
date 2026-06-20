package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class SeleccionarEspecialidadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_especialidad)

        val lvEspecialidades = findViewById<ListView>(R.id.lvEspecialidadesReserva)
        val especialidades = listOf("Cardiología", "Pediatría", "Dermatología", "Neurología", "Medicina General")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, especialidades)
        lvEspecialidades.adapter = adapter

        lvEspecialidades.setOnItemClickListener { _, _, _, _ ->
            startActivity(Intent(this, SeleccionarMedicoHorarioActivity::class.java))
            finish()
        }
    }
}