package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.adapter.HorarioAdminAdapter


class ListaHorariosAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_horarios_admin)

        val rvHorarios = findViewById<RecyclerView>(R.id.rvHorarios)
        rvHorarios.layoutManager = LinearLayoutManager(this)
        val listaSimulada = listOf(
            HorarioMock("Dr. Carlos Mendoza", "26/06/2026", "08:00 AM - 12:00 PM"),
            HorarioMock("Dra. Ana López", "26/06/2026", "02:00 PM - 06:00 PM"),
            HorarioMock("Dr. Luis Gómez", "27/06/2026", "09:00 AM - 01:00 PM")
        )
        val adapter = HorarioAdminAdapter(listaSimulada)
        rvHorarios.adapter = adapter

        val fabNuevoHorario = findViewById<FloatingActionButton>(R.id.fabNuevoHorario)
        fabNuevoHorario.setOnClickListener {
            startActivity(Intent(this, RegistrarHorarioActivity::class.java))
        }
    }
}