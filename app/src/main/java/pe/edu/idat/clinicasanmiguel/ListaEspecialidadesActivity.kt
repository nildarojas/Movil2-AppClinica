package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository

class ListaEspecialidadesActivity : AppCompatActivity() {

    private lateinit var rvEspecialidades: RecyclerView
    private lateinit var adminRepository: AdminRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_especialidades)

        rvEspecialidades =
            findViewById(R.id.rvEspecialidades)

        rvEspecialidades.layoutManager =
            LinearLayoutManager(this)

        adminRepository =
            AdminRepository(this)

        findViewById<FloatingActionButton>(
            R.id.fabNuevaEspecialidad
        ).setOnClickListener {

            val intent = Intent(
                this,
                RegistrarEspecialidadActivity::class.java
            )

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarEspecialidades()
    }

    private fun cargarEspecialidades() {

        val especialidadesReales =
            adminRepository.obtenerEspecialidades()

        val especialidadesParaMostrar =
            especialidadesReales.map { especialidad ->

                EspecialidadMock(
                    especialidad.nombre,
                    "Área no asignada",
                    "ACTIVO"
                )
            }

        rvEspecialidades.adapter =
            EspecialidadAdminAdapter(
                especialidadesParaMostrar,
                true
            )
    }
}