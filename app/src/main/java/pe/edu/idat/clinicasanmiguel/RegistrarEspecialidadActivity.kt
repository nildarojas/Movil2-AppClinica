package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class RegistrarEspecialidadActivity : AppCompatActivity() {

    private lateinit var etNombreEspecialidad: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_especialidad)

        etNombreEspecialidad =
            findViewById(R.id.etNombreEspecialidad)

        btnGuardar =
            findViewById(R.id.btnGuardarEspecialidad)

        btnCancelar =
            findViewById(R.id.btnCancelarEspecialidad)

        btnGuardar.setOnClickListener {

            val nombreEspecialidad =
                etNombreEspecialidad.text.toString().trim()

            if (nombreEspecialidad.isEmpty()) {

                etNombreEspecialidad.error =
                    "Ingrese el nombre de la especialidad"

                etNombreEspecialidad.requestFocus()

                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Especialidad registrada correctamente",
                Toast.LENGTH_SHORT
            ).show()

            etNombreEspecialidad.setText("")
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}