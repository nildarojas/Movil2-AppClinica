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
import pe.edu.idat.clinicasanmiguel.adapter.DoctorAdminAdapter
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository

class ListaDoctoresAdminFragment : Fragment() {

    private lateinit var rvDoctores: RecyclerView
    private lateinit var fabAddDoctor: FloatingActionButton
    private lateinit var adminRepository: AdminRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_lista_doctores_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvDoctores = view.findViewById(R.id.rvDoctoresAdmin)
        fabAddDoctor = view.findViewById(R.id.fabAddDoctor)

        rvDoctores.layoutManager = LinearLayoutManager(requireContext())
        adminRepository = AdminRepository(requireContext())

        fabAddDoctor.setOnClickListener {
            val intent = Intent(
                requireContext(),
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
        val medicosReales = adminRepository.obtenerMedicos()
        rvDoctores.adapter = DoctorAdminAdapter(medicosReales)
    }
}