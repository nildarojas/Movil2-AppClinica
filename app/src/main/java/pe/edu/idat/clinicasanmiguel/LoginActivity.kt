package pe.edu.idat.clinicasanmiguel

import android.annotation.SuppressLint
import android.content.Context
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
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository

class LoginActivity : AppCompatActivity() {
    private lateinit var tvRegistro: TextView
    private lateinit var tvOlvidaste: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var usuarioRepository: UsuarioRepository

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        usuarioRepository = UsuarioRepository(this)

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
            val intent = Intent(this, SolicitarRecuperacionActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese sus datos", Toast.LENGTH_SHORT).show()
            } else {
                val usuarioLogueado = usuarioRepository.login(correo, contrasena)

                if (usuarioLogueado != null) {
                    val preferencias = getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
                    preferencias.edit().apply {
                        putInt("ID_USUARIO", usuarioLogueado.id)
                        putString("ROL_USUARIO", usuarioLogueado.rol)
                        putString("NOMBRE_USUARIO", "${usuarioLogueado.nombre} ${usuarioLogueado.apellido}")
                        apply()
                    }

                    if (usuarioLogueado.rol == "ADMIN") {
                        Toast.makeText(this, "Bienvenido ADMIN: ${usuarioLogueado.nombre}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, AdminActivity::class.java))
                    } else {
                        Toast.makeText(this, "Bienvenido PACIENTE: ${usuarioLogueado.nombre}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, InicioActivity::class.java))
                    }
                    finish()
                } else {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
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