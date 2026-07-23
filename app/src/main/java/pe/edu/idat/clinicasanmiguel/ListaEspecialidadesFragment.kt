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
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository

class ListaEspecialidadesFragment : Fragment() {

    private lateinit var rvEspecialidades: RecyclerView
    private lateinit var adminRepository: AdminRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_lista_especialidades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvEspecialidades = view.findViewById(R.id.rvEspecialidades)
        rvEspecialidades.layoutManager = LinearLayoutManager(requireContext())

        adminRepository = AdminRepository(requireContext())

        view.findViewById<FloatingActionButton>(R.id.fabNuevaEspecialidad).setOnClickListener {
            val intent = Intent(
                requireContext(),
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
        val especialidadesReales = adminRepository.obtenerEspecialidades()

        val especialidadesParaMostrar = especialidadesReales.map { especialidad ->
            EspecialidadMock(
                especialidad.nombre,
                "Área no asignada",
                "ACTIVO"
            )
        }

        rvEspecialidades.adapter = EspecialidadAdminAdapter(
            especialidadesParaMostrar,
            true
        )
    }
}