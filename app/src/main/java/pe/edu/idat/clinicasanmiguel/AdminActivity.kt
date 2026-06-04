package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val cardEspecialidades = findViewById<CardView>(R.id.cardEspecialidades)
        val cardDoctores = findViewById<CardView>(R.id.cardDoctores)
        val cardHorarios = findViewById<CardView>(R.id.cardHorarios)
        val cardUsuarios = findViewById<CardView>(R.id.cardUsuarios)
        val cardCitas = findViewById<CardView>(R.id.cardCitas)
        val cardMiPerfil = findViewById<CardView>(R.id.cardMiPerfil)
        val cardCambiarPassword = findViewById<CardView>(R.id.cardCambiarPassword)

        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // Especialidades
        cardEspecialidades.setOnClickListener {

            // Próxima Activity
            // startActivity(Intent(this, RegistrarEspecialidadActivity::class.java))

        }

        // Doctores
        cardDoctores.setOnClickListener {

            // Próxima Activity
            // startActivity(Intent(this, RegistrarDoctorActivity::class.java))

        }

        // Horarios
        cardHorarios.setOnClickListener {

            // Sprint 2

        }

        // Usuarios
        cardUsuarios.setOnClickListener {

            // Sprint 2

        }

        // Citas
        cardCitas.setOnClickListener {

            // Sprint 2

        }

        // Mi Perfil
        cardMiPerfil.setOnClickListener {

            // Sprint 2

        }

        // Cambiar Contraseña
        cardCambiarPassword.setOnClickListener {

            // Sprint 2

        }

        // Cerrar Sesión
        btnCerrarSesion.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Está seguro de que desea salir del sistema?")
                .setPositiveButton("Sí") { _, _ ->

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}