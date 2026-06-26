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
            EspecialidadMock("Pediatría", "Piso 1 - Área Infantil", "ACTIVO"),
            EspecialidadMock("Ginecología", "Piso 3 - Bloque B", "ACTIVO"),
            EspecialidadMock("Gastroenterología", "Piso 2 - Consultorio 204", "ACTIVO")
        )
        rvEspecialidades.adapter = EspecialidadAdminAdapter(dataSimulada)

        val fabNuevaEspecialidad = findViewById<FloatingActionButton>(R.id.fabNuevaEspecialidad)

        fabNuevaEspecialidad.setOnClickListener {
            val intent = Intent(this, RegistrarEspecialidadActivity::class.java)
            startActivity(intent)
        }
    }
}