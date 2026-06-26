package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PacienteActivity : AppCompatActivity() {

    private lateinit var tvSaludoBienvenida: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paciente)

        tvSaludoBienvenida = findViewById(R.id.tvSaludoBienvenida)
        val btnVerCitas = findViewById<Button>(R.id.btnVerCitas)
        val btnHorarios = findViewById<Button>(R.id.btnHorarios)
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        val btnNotificaciones = findViewById<Button>(R.id.btnNotificaciones)
        val acPerfil = findViewById<AutoCompleteTextView>(R.id.acPerfil)

        tvSaludoBienvenida.text = "¡Bienvenido, Franklin Elias!"

        btnVerCitas.setOnClickListener {
            startActivity(Intent(this, MisCitasActivity::class.java))
        }
        btnHorarios.setOnClickListener {

            startActivity(Intent(this, SeleccionarEspecialidadActivity::class.java))
        }
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialCompletoActivity::class.java))
        }
        btnNotificaciones.setOnClickListener {
            Toast.makeText(this, "Módulo de Alertas y Notificaciones (Sprint 2)", Toast.LENGTH_SHORT).show()
        }

        val opciones = arrayOf(
            "👤 Mi Perfil",
            "🔑 Cambiar contraseña",
            "🚪 Cerrar sesión"
        )

        val adapter = ArrayAdapter(this, R.layout.spinner_perfil_item, opciones)
        acPerfil.setAdapter(adapter)

        acPerfil.setOnClickListener {
            acPerfil.showDropDown()
        }

        acPerfil.setOnItemClickListener { parent, _, position, _ ->
            val seleccion = parent.getItemAtPosition(position).toString()

            when (seleccion) {
                "👤 Mi Perfil" -> {
                    val intent = Intent(this, MiPerfilActivity::class.java)
                    intent.putExtra("ROL_USUARIO", "PACIENTE")
                    startActivity(intent)
                }
                "🔑 Cambiar contraseña" -> {
                    val intent = Intent(this, CambiarPasswordInternoActivity::class.java)
                    intent.putExtra("ROL_USUARIO", "PACIENTE")
                    startActivity(intent)
                }

                "🚪 Cerrar sesión" -> {
                    AlertDialog.Builder(this)
                        .setTitle("Cerrar Sesión")
                        .setMessage("¿Está seguro de que desea salir del sistema?")
                        .setPositiveButton("Sí") { _, _ ->
                            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                            prefs.edit().clear().apply()

                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
            acPerfil.setText("Mi Perfil", false)
        }
    }
}