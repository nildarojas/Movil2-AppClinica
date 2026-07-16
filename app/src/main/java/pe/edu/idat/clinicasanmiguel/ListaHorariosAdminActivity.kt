package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.adapter.HorarioAdminAdapter
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository

class ListaHorariosAdminActivity : AppCompatActivity() {

    private lateinit var rvHorarios: RecyclerView
    private lateinit var fabNuevoHorario: FloatingActionButton
    private lateinit var adminRepository: AdminRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_horarios_admin)

        rvHorarios =
            findViewById(R.id.rvHorarios)

        fabNuevoHorario =
            findViewById(R.id.fabNuevoHorario)

        rvHorarios.layoutManager =
            LinearLayoutManager(this)

        adminRepository =
            AdminRepository(this)

        fabNuevoHorario.setOnClickListener {

            val intent = Intent(
                this,
                RegistrarHorarioActivity::class.java
            )

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarHorariosDesdeSQLite()
    }

    private fun cargarHorariosDesdeSQLite() {

        val horariosReales =
            adminRepository.obtenerHorarios()
        val horariosParaMostrar =
            horariosReales.map { horario ->

                val fechaYHora =
                    separarFechaYHorario(
                        horario.fechaHoraTexto
                    )

                HorarioMock(
                    "${horario.medico} (${horario.especialidad})",

                    fechaYHora.first,

                    fechaYHora.second
                )
            }

        rvHorarios.adapter =
            HorarioAdminAdapter(horariosParaMostrar)
    }

    private fun separarFechaYHorario(
        fechaHoraTexto: String
    ): Pair<String, String> {

        val partes = fechaHoraTexto.split(
            " - ",
            limit = 2
        )

        return if (partes.size == 2) {

            Pair(
                partes[0],
                partes[1]
            )

        } else {

            Pair(
                fechaHoraTexto,
                "Horario registrado"
            )
        }
    }
}