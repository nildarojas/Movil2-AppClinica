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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.DoctorAdminAdapter
import pe.edu.idat.clinicasanmiguel.entity.MedicoAdmin
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.MedicoAdminRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaMedicosAdminApi
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor

class ListaDoctoresAdminFragment :
    Fragment() {

    private lateinit var rvDoctores:
            RecyclerView

    private lateinit var fabAddDoctor:
            FloatingActionButton

    private lateinit var medicoRepository:
            MedicoAdminRepository

    private lateinit var doctorAdapter:
            DoctorAdminAdapter

    private var cargando =
        false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.activity_lista_doctores_admin,
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

        medicoRepository =
            MedicoAdminRepository(
                requireContext()
            )

        rvDoctores =
            view.findViewById(
                R.id.rvDoctoresAdmin
            )

        fabAddDoctor =
            view.findViewById(
                R.id.fabAddDoctor
            )

        configurarRecyclerView()

        fabAddDoctor.setOnClickListener {
            abrirRegistroDoctor()
        }

        observarConexion()
    }

    override fun onResume() {
        super.onResume()
        if (NetworkMonitor.hayInternet()) {
            cargarDoctoresDesdeApi()
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
        doctorAdapter =
            DoctorAdminAdapter(
                emptyList()
            )

        rvDoctores.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                doctorAdapter

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

                fabAddDoctor.isEnabled =
                    !cargando

                if (!cargando) {
                    cargarDoctoresDesdeApi()
                }
            }
    }

    private fun cargarDoctoresDesdeApi() {
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

        fabAddDoctor.isEnabled =
            false

        medicoRepository
            .listarMedicosApi callback@{
                    resultado ->

                if (!vistaDisponible()) {
                    return@callback
                }

                cargando =
                    false

                fabAddDoctor.isEnabled =
                    NetworkMonitor.hayInternet()

                if (!NetworkMonitor.hayInternet()) {
                    mostrarSinConexion()

                    return@callback
                }

                when (resultado) {
                    is ResultadoCargaMedicosAdminApi.Exito -> {
                        mostrarDoctores(
                            resultado.medicos
                        )

                        if (resultado.medicos.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                "No existen médicos registrados.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    is ResultadoCargaMedicosAdminApi.SinConexion -> {
                        mostrarDoctores(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaMedicosAdminApi.Error -> {
                        mostrarDoctores(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaMedicosAdminApi.SinPermiso -> {
                        mostrarDoctores(
                            emptyList()
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaMedicosAdminApi.SesionExpirada -> {
                        cerrarSesion(
                            resultado.mensaje
                        )
                    }
                }
            }
    }

    private fun mostrarDoctores(
        medicos: List<MedicoAdmin>
    ) {
        if (!::doctorAdapter.isInitialized) {
            return
        }

        doctorAdapter.actualizarLista(
            medicos
        )
    }

    private fun mostrarSinConexion() {
        cargando =
            false

        if (::doctorAdapter.isInitialized) {
            doctorAdapter.actualizarLista(
                emptyList()
            )
        }

        if (!vistaDisponible()) {
            return
        }

        fabAddDoctor.isEnabled =
            false

        Toast.makeText(
            requireContext(),
            "Necesitas conexión a Internet para consultar y administrar médicos.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun abrirRegistroDoctor() {
        if (cargando) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            Toast.makeText(
                requireContext(),
                "Necesitas conexión a Internet para registrar un médico.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.flContenedor,
                RegistrarDoctorFragment()
            )
            .addToBackStack(
                "LISTA_DOCTORES"
            )
            .commit()
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