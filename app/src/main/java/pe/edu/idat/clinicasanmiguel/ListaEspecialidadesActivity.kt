package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock

class ListaEspecialidadesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_especialidades)

        val rvEspecialidades = findViewById<RecyclerView>(R.id.rvEspecialidades)
        rvEspecialidades.layoutManager = LinearLayoutManager(this)

        val dataSimulada = listOf(
            EspecialidadMock("Cardiología", "Piso 2 - Bloque A", "ACTIVO"),
            EspecialidadMock("Pediatría", "Piso 1 - Área Infantil", "ACTIVO")
        )

        rvEspecialidades.adapter = EspecialidadAdminAdapter(dataSimulada, true)

        findViewById<FloatingActionButton>(R.id.fabNuevaEspecialidad).setOnClickListener {
            startActivity(Intent(this, RegistrarEspecialidadActivity::class.java))
        }
    }
}