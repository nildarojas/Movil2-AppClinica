package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SolicitarRecuperacionActivity : AppCompatActivity() {

    private lateinit var etCorreo: TextInputEditText
    private lateinit var btnEnviarCodigo: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_olvide_password)

        etCorreo = findViewById(R.id.etCorreoRecuperacion)
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo)

        btnEnviarCodigo.setOnClickListener {
            solicitarRecuperacionLocal()
        }
    }

    private fun solicitarRecuperacionLocal() {
        val correo = etCorreo.text.toString().trim()

        if (correo.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Código de recuperación enviado localmente", Toast.LENGTH_LONG).show()

        val intent = Intent(this, ResetearPasswordActivity::class.java)
        intent.putExtra("correo", correo)
        startActivity(intent)
    }
}