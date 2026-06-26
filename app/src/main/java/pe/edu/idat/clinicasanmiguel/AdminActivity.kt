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

            val intent = Intent(
                this,
                ListaEspecialidadesActivity::class.java
            )

            startActivity(intent)
        }

        btnDoctores.setOnClickListener {
            val intent = Intent(this, ListaDoctoresAdminActivity::class.java)
            startActivity(intent)
        }

        btnHorarios.setOnClickListener {
            startActivity(Intent(this, ListaHorariosAdminActivity::class.java))
        }

        findViewById<Button>(R.id.btnUsuarios).setOnClickListener {
            startActivity(Intent(this, ListaUsuariosActivity::class.java))
        }

        findViewById<Button>(R.id.btnCitas).setOnClickListener {
            startActivity(Intent(this, MaestroCitasAdminActivity::class.java))
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

                    val intent = Intent(this, MiPerfilActivity::class.java)
                    intent.putExtra("ROL_USUARIO", "ADMIN")
                    startActivity(intent)
                }

                "🔑 Cambiar Contraseña" -> {
                    val intent = Intent(this, CambiarPasswordInternoActivity::class.java)
                    intent.putExtra("ROL_USUARIO", "ADMIN")
                    startActivity(intent)
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