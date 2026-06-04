package pe.edu.idat.clinicasanmiguel

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    private lateinit var tvRegistro: TextView
    private lateinit var tvOlvidaste: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        tvRegistro = findViewById(R.id.tvRegistro)
        tvOlvidaste = findViewById(R.id.tvOlvidaste)
        btnLogin = findViewById(R.id.btnLogin)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)

        tvRegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        tvOlvidaste.setOnClickListener {
            Toast.makeText(this, "Flujo de recuperación (Próxima entrega)", Toast.LENGTH_SHORT).show()
        }

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese sus datos", Toast.LENGTH_SHORT).show()
            } else {
                // Simulación estática local de Roles (Mock Data)
                if (correo == "paciente@idat.com" && contrasena == "123456") {
                    Toast.makeText(this, "Bienvenido PACIENTE", Toast.LENGTH_SHORT).show()

                    // Navegación limpia hacia la interfaz del Paciente
                    val intent = Intent(this, PacienteActivity::class.java)
                    startActivity(intent)
                    finish()

                } else if (correo == "admin@idat.com" && contrasena == "admin") {
                    Toast.makeText(this, "Bienvenido ADMIN", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}