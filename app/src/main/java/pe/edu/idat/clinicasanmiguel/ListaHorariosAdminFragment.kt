package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.adapter.HorarioAdminAdapter
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository

class ListaHorariosAdminFragment : Fragment() {

    private lateinit var rvHorarios: RecyclerView
    private lateinit var fabNuevoHorario: FloatingActionButton
    private lateinit var adminRepository: AdminRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_lista_horarios_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHorarios = view.findViewById(R.id.rvHorarios)
        fabNuevoHorario = view.findViewById(R.id.fabNuevoHorario)

        rvHorarios.layoutManager = LinearLayoutManager(requireContext())
        adminRepository = AdminRepository(requireContext())

        fabNuevoHorario.setOnClickListener {
            val intent = Intent(
                requireContext(),
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
        val horariosReales = adminRepository.obtenerHorarios()
        rvHorarios.adapter = HorarioAdminAdapter(horariosReales)
    }
}