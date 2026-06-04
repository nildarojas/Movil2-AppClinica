package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val btnEspecialidades = findViewById<Button>(R.id.btnEspecialidades)
        val btnDoctores = findViewById<Button>(R.id.btnDoctores)
        val btnHorarios = findViewById<Button>(R.id.btnHorarios)
        val btnUsuarios = findViewById<Button>(R.id.btnUsuarios)
        val btnCitas = findViewById<Button>(R.id.btnCitas)

        val acPerfilAdmin =
            findViewById<AutoCompleteTextView>(R.id.acPerfilAdmin)

        btnEspecialidades.setOnClickListener {
            Toast.makeText(
                this,
                "Gestión de Especialidades (Próxima entrega)",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnDoctores.setOnClickListener {
            Toast.makeText(
                this,
                "Gestión de Doctores (Próxima entrega)",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnHorarios.setOnClickListener {
            Toast.makeText(
                this,
                "Gestión de Horarios (Sprint 2)",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnUsuarios.setOnClickListener {
            Toast.makeText(
                this,
                "Lista de Usuarios (Sprint 2)",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnCitas.setOnClickListener {
            Toast.makeText(
                this,
                "Citas Médicas (Sprint 2)",
                Toast.LENGTH_SHORT
            ).show()
        }

        val opciones = arrayOf(
            "👤 Mi Perfil",
            "🔑 Cambiar Contraseña",
            "🚪 Cerrar Sesión"
        )

        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_perfil_item,
            opciones
        )

        acPerfilAdmin.setAdapter(adapter)

        acPerfilAdmin.setOnClickListener {
            acPerfilAdmin.showDropDown()
        }

        acPerfilAdmin.setOnItemClickListener { parent, _, position, _ ->

            val seleccion =
                parent.getItemAtPosition(position).toString()

            when (seleccion) {

                "👤 Mi Perfil" -> {

                    Toast.makeText(
                        this,
                        "Mi Perfil (Sprint 2)",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                "🔑 Cambiar Contraseña" -> {

                    Toast.makeText(
                        this,
                        "Cambiar Contraseña (Sprint 2)",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                "🚪 Cerrar Sesión" -> {

                    AlertDialog.Builder(this)
                        .setTitle("Cerrar Sesión")
                        .setMessage("¿Está seguro de que desea salir del sistema?")
                        .setPositiveButton("Sí") { _, _ ->

                            val intent =
                                Intent(this, LoginActivity::class.java)

                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                            finish()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }

            acPerfilAdmin.setText("Mi Perfil", false)
        }
    }
}