package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class CambiarPasswordInternoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cambiar_password_interno)

        val etActual = findViewById<TextInputEditText>(R.id.etPasswordActual)
        val etNueva1 = findViewById<TextInputEditText>(R.id.etPasswordNueva1)
        val etNueva2 = findViewById<TextInputEditText>(R.id.etPasswordNueva2)
        val btnActualizar = findViewById<Button>(R.id.btnActualizarPasswordInterno)

        btnActualizar.setOnClickListener {
            val txtActual = etActual.text.toString().trim()
            val txtNueva1 = etNueva1.text.toString().trim()
            val txtNueva2 = etNueva2.text.toString().trim()

            if (txtActual.isEmpty() || txtNueva1.isEmpty() || txtNueva2.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (txtNueva1 != txtNueva2) {
                Toast.makeText(this, "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (txtNueva1.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}