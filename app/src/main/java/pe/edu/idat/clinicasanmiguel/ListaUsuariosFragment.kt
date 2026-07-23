package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.UsuariosAdminAdapter
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository

class ListaUsuariosFragment : Fragment() {

    private lateinit var rvUsuarios: RecyclerView
    private lateinit var usuarioRepository: UsuarioRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_lista_usuarios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvUsuarios = view.findViewById(R.id.rvUsuariosAdmin)
        rvUsuarios.layoutManager = LinearLayoutManager(requireContext())

        usuarioRepository = UsuarioRepository(requireContext())
    }

    override fun onResume() {
        super.onResume()
        cargarUsuariosDesdeSQLite()
    }

    private fun cargarUsuariosDesdeSQLite() {
        val usuariosReales = usuarioRepository.obtenerTodosLosUsuarios()
        rvUsuarios.adapter = UsuariosAdminAdapter(usuariosReales)
    }
}