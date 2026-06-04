package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistrarEspecialidadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_especialidad)

        val btnGuardar =
            findViewById<Button>(R.id.btnGuardarEspecialidad)

        val btnCancelar =
            findViewById<Button>(R.id.btnCancelarEspecialidad)

        btnGuardar.setOnClickListener {

            Toast.makeText(
                this,
                "Especialidad registrada correctamente",
                Toast.LENGTH_SHORT
            ).show()

        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}