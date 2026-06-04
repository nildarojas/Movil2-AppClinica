package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaEspecialidadesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_especialidades)

        val lvEspecialidades =
            findViewById<ListView>(R.id.lvEspecialidades)

        val fabNuevaEspecialidad =
            findViewById<FloatingActionButton>(R.id.fabNuevaEspecialidad)

        val especialidades = listOf(
            "Cardiología",
            "Pediatría",
            "Dermatología",
            "Neurología",
            "Medicina General"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            especialidades
        )

        lvEspecialidades.adapter = adapter

        fabNuevaEspecialidad.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    RegistrarEspecialidadActivity::class.java
                )
            )

        }
    }
}