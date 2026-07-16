package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.adapter.DoctorAdminAdapter
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository

class ListaDoctoresAdminActivity : AppCompatActivity() {

    private lateinit var rvDoctores: RecyclerView
    private lateinit var fabAddDoctor: FloatingActionButton
    private lateinit var adminRepository: AdminRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_doctores_admin)

        rvDoctores =
            findViewById(R.id.rvDoctoresAdmin)

        fabAddDoctor =
            findViewById(R.id.fabAddDoctor)

        rvDoctores.layoutManager =
            LinearLayoutManager(this)

        adminRepository =
            AdminRepository(this)

        fabAddDoctor.setOnClickListener {

            val intent = Intent(
                this,
                RegistrarDoctorActivity::class.java
            )

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDoctoresDesdeSQLite()
    }

    private fun cargarDoctoresDesdeSQLite() {

        val medicosReales =
            adminRepository.obtenerMedicos()

        val doctoresParaMostrar =
            medicosReales.map { medico ->

                val nombreSeparado =
                    separarNombreCompleto(medico.nombre)

                DoctorMock(
                    nombre = nombreSeparado.first,
                    apellido = nombreSeparado.second,
                    especialidad = medico.especialidad,
                    estado = true
                )
            }

        rvDoctores.adapter =
            DoctorAdminAdapter(doctoresParaMostrar)
    }

    private fun separarNombreCompleto(
        nombreCompleto: String
    ): Pair<String, String> {

        val nombreLimpio =
            nombreCompleto
                .trim()
                .replace(Regex("\\s+"), " ")

        val partes =
            nombreLimpio.split(" ")

        if (partes.size <= 1) {
            return Pair(
                nombreLimpio,
                ""
            )
        }

        val apellido =
            partes.last()

        val nombre =
            partes
                .dropLast(1)
                .joinToString(" ")

        return Pair(
            nombre,
            apellido
        )
    }
}