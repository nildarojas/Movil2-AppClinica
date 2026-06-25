package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListaHorariosAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_horarios_admin)

        val fabNuevoHorario = findViewById<FloatingActionButton>(R.id.fabNuevoHorario)

        fabNuevoHorario.setOnClickListener {
            startActivity(Intent(this, RegistrarHorarioActivity::class.java))
        }
    }
}