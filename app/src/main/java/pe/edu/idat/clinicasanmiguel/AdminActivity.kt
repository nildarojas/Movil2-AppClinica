package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton

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

        val btnCerrarSesion =
            findViewById<MaterialButton>(R.id.btnCerrarSesion)

        cardEspecialidades.setOnClickListener {

            // Próxima pantalla
            // startActivity(Intent(this, RegistrarEspecialidadActivity::class.java))

        }

        cardDoctores.setOnClickListener {

            // Próxima pantalla
            // startActivity(Intent(this, RegistrarDoctorActivity::class.java))

        }

        cardHorarios.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Próximamente")
                .setMessage("Módulo de horarios disponible en el siguiente sprint.")
                .setPositiveButton("Aceptar", null)
                .show()

        }

        cardUsuarios.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Próximamente")
                .setMessage("Módulo de usuarios disponible en el siguiente sprint.")
                .setPositiveButton("Aceptar", null)
                .show()

        }

        cardCitas.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Próximamente")
                .setMessage("Módulo de citas disponible en el siguiente sprint.")
                .setPositiveButton("Aceptar", null)
                .show()

        }

        cardMiPerfil.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Mi Perfil")
                .setMessage("Funcionalidad pendiente para la siguiente entrega.")
                .setPositiveButton("Aceptar", null)
                .show()

        }

        cardCambiarPassword.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Cambiar Contraseña")
                .setMessage("Funcionalidad pendiente para la siguiente entrega.")
                .setPositiveButton("Aceptar", null)
                .show()

        }

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