package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.UsuariosAdminAdapter
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.network.UsuarioListadoApiResponse
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaUsuariosAdminApi
import pe.edu.idat.clinicasanmiguel.repository.UsuarioAdminRepository
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor

class ListaUsuariosFragment :
    Fragment() {

    private lateinit var rvUsuarios:
            RecyclerView

    private lateinit var usuarioAdminRepository:
            UsuarioAdminRepository

    private lateinit var usuariosAdapter:
            UsuariosAdminAdapter

    private var cargando =
        false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.activity_lista_usuarios,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        usuarioAdminRepository =
            UsuarioAdminRepository(
                requireContext()
            )

        rvUsuarios =
            view.findViewById(
                R.id.rvUsuariosAdmin
            )

        configurarRecyclerView()
        observarConexion()
    }

    override fun onResume() {
        super.onResume()

        if (NetworkMonitor.hayInternet()) {
            cargarUsuariosDesdeApi()
        } else {
            mostrarSinConexion()
        }
    }

    override fun onDestroyView() {
        cargando =
            false

        super.onDestroyView()
    }

    private fun configurarRecyclerView() {
        usuariosAdapter =
            UsuariosAdminAdapter(
                emptyList()
            )

        rvUsuarios.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                usuariosAdapter

            setHasFixedSize(
                true
            )
        }
    }

    private fun observarConexion() {
        NetworkMonitor
            .estadoConexion
            .observe(
                viewLifecycleOwner
            ) { conectado ->

                if (!conectado) {
                    mostrarSinConexion()

                    return@observe
                }

                if (!cargando) {
                    cargarUsuariosDesdeApi()
                }
            }
    }

    private fun cargarUsuariosDesdeApi() {
        if (
            cargando ||
            !vistaDisponible()
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarSinConexion()

            return
        }

        cargando =
            true

        usuarioAdminRepository
            .listarUsuariosApi callback@{
                    resultado ->

                if (!vistaDisponible()) {
                    return@callback
                }

                cargando =
                    false

                if (!NetworkMonitor.hayInternet()) {
                    mostrarSinConexion()

                    return@callback
                }

                when (resultado) {
                    is ResultadoCargaUsuariosAdminApi.Exito -> {
                        mostrarUsuarios(
                            resultado.usuarios
                        )

                        if (resultado.usuarios.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "No existen usuarios registrados.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    is ResultadoCargaUsuariosAdminApi.SinConexion -> {
                        mostrarUsuarios(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaUsuariosAdminApi.Error -> {
                        mostrarUsuarios(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaUsuariosAdminApi.SinPermiso -> {
                        mostrarUsuarios(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaUsuariosAdminApi.SesionExpirada -> {
                        cerrarSesion(
                            resultado.mensaje
                        )
                    }
                }
            }
    }

    private fun mostrarUsuarios(
        usuarios: List<UsuarioListadoApiResponse>
    ) {
        if (!::usuariosAdapter.isInitialized) {
            return
        }

        usuariosAdapter.actualizarLista(
            usuarios
        )
    }

    private fun mostrarSinConexion() {
        cargando =
            false

        if (::usuariosAdapter.isInitialized) {
            usuariosAdapter.actualizarLista(
                emptyList()
            )
        }

        if (!vistaDisponible()) {
            return
        }

        Toast.makeText(
            requireContext(),
            "Necesitas conexión a Internet para consultar usuarios.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun cerrarSesion(
        mensaje: String
    ) {
        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_LONG
        ).show()

        SessionManager(
            requireContext()
        ).limpiarSesion()

        val intent =
            Intent(
                requireContext(),
                LoginActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(
            intent
        )

        requireActivity()
            .finish()
    }

    private fun vistaDisponible(): Boolean {
        return isAdded &&
                view != null
    }
}