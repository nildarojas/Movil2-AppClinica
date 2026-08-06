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
import pe.edu.idat.clinicasanmiguel.adapter.CitasGlobalAdminAdapter
import pe.edu.idat.clinicasanmiguel.network.CitaGlobalApiResponse
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.CitaAdminRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaCitasAdminApi
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor

class CitasGlobalesFragment :
    Fragment() {

    private lateinit var rvCitasGlobales:
            RecyclerView

    private lateinit var citaAdminRepository:
            CitaAdminRepository

    private lateinit var citasAdapter:
            CitasGlobalAdminAdapter

    private var cargando =
        false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.activity_maestro_citas_admin,
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

        citaAdminRepository =
            CitaAdminRepository(
                requireContext()
            )

        rvCitasGlobales =
            view.findViewById(
                R.id.rvCitasGlobalAdmin
            )

        configurarRecyclerView()
        observarConexion()
    }

    override fun onResume() {
        super.onResume()

        if (NetworkMonitor.hayInternet()) {
            cargarCitasDesdeApi()
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
        citasAdapter =
            CitasGlobalAdminAdapter(
                emptyList()
            )

        rvCitasGlobales.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                citasAdapter

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
                    cargarCitasDesdeApi()
                }
            }
    }

    private fun cargarCitasDesdeApi() {
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

        citaAdminRepository
            .listarCitasGlobalesApi callback@{
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
                    is ResultadoCargaCitasAdminApi.Exito -> {
                        mostrarCitas(
                            resultado.citas
                        )

                        if (resultado.citas.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "No existen citas registradas.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    is ResultadoCargaCitasAdminApi.SinConexion -> {
                        mostrarCitas(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaCitasAdminApi.Error -> {
                        mostrarCitas(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaCitasAdminApi.SinPermiso -> {
                        mostrarCitas(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaCitasAdminApi.SesionExpirada -> {
                        cerrarSesion(
                            resultado.mensaje
                        )
                    }
                }
            }
    }

    private fun mostrarCitas(
        citas: List<CitaGlobalApiResponse>
    ) {
        if (!::citasAdapter.isInitialized) {
            return
        }

        citasAdapter.actualizarLista(
            citas
        )
    }

    private fun mostrarSinConexion() {
        cargando =
            false

        if (::citasAdapter.isInitialized) {
            citasAdapter.actualizarLista(
                emptyList()
            )
        }

        if (!vistaDisponible()) {
            return
        }

        Toast.makeText(
            requireContext(),
            "Necesitas conexión a Internet para consultar las citas.",
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