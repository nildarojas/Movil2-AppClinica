package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.HorarioAdminAdapter
import pe.edu.idat.clinicasanmiguel.entity.HorarioAdmin
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.HorarioAdminRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaHorariosAdminApi
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor

class ListaHorariosAdminFragment :
    Fragment() {

    private lateinit var rvHorarios:
            RecyclerView

    private lateinit var tvDescripcion:
            TextView

    private lateinit var fabNuevoHorario:
            FloatingActionButton

    private lateinit var horarioRepository:
            HorarioAdminRepository

    private lateinit var horarioAdapter:
            HorarioAdminAdapter

    private var cargando =
        false
    private var idSolicitud =
        0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.activity_lista_horarios_admin,
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

        horarioRepository =
            HorarioAdminRepository(
                requireContext()
            )

        rvHorarios =
            view.findViewById(
                R.id.rvHorarios
            )

        tvDescripcion =
            view.findViewById(
                R.id.tvDescripcion
            )

        fabNuevoHorario =
            view.findViewById(
                R.id.fabNuevoHorario
            )

        configurarRecyclerView()

        fabNuevoHorario.setOnClickListener {
            abrirRegistroHorario()
        }

        observarConexion()
    }

    override fun onResume() {
        super.onResume()
        if (NetworkMonitor.hayInternet()) {
            cargarHorariosDesdeApi()
        } else {
            mostrarSinConexion()
        }
    }

    override fun onDestroyView() {
        idSolicitud++

        cargando =
            false

        super.onDestroyView()
    }

    private fun configurarRecyclerView() {
        horarioAdapter =
            HorarioAdminAdapter(
                emptyList()
            )

        rvHorarios.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                horarioAdapter

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

                fabNuevoHorario.isEnabled =
                    !cargando
                if (
                    !cargando &&
                    horarioAdapter.itemCount == 0
                ) {
                    cargarHorariosDesdeApi()
                }
            }
    }

    private fun cargarHorariosDesdeApi() {
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

        val solicitudActual =
            ++idSolicitud

        mostrarCargando()

        horarioRepository
            .listarHorariosApi callback@{
                    resultado ->

                if (
                    solicitudActual != idSolicitud ||
                    !vistaDisponible()
                ) {
                    return@callback
                }

                cargando =
                    false
                if (!NetworkMonitor.hayInternet()) {
                    mostrarSinConexion()

                    return@callback
                }

                fabNuevoHorario.isEnabled =
                    true

                when (resultado) {
                    is ResultadoCargaHorariosAdminApi.Exito -> {
                        mostrarHorarios(
                            resultado.horarios
                        )
                    }

                    is ResultadoCargaHorariosAdminApi.SinConexion -> {
                        mostrarErrorLista(
                            resultado.mensaje
                        )
                    }

                    is ResultadoCargaHorariosAdminApi.SinPermiso -> {
                        mostrarErrorLista(
                            resultado.mensaje
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaHorariosAdminApi.SesionExpirada -> {
                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoCargaHorariosAdminApi.Error -> {
                        mostrarErrorLista(
                            resultado.mensaje
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun mostrarCargando() {
        horarioAdapter.actualizarLista(
            emptyList()
        )

        rvHorarios.visibility =
            View.GONE

        tvDescripcion.text =
            "Cargando horarios desde el servidor..."

        tvDescripcion.visibility =
            View.VISIBLE

        fabNuevoHorario.isEnabled =
            false
    }

    private fun mostrarHorarios(
        horarios: List<HorarioAdmin>
    ) {
        horarioAdapter.actualizarLista(
            horarios
        )

        if (horarios.isEmpty()) {
            rvHorarios.visibility =
                View.GONE

            tvDescripcion.text =
                "No existen horarios registrados."

            tvDescripcion.visibility =
                View.VISIBLE
        } else {
            rvHorarios.visibility =
                View.VISIBLE

            tvDescripcion.text =
                "Horarios registrados: ${horarios.size}"

            tvDescripcion.visibility =
                View.VISIBLE
        }
    }

    private fun mostrarSinConexion() {
        idSolicitud++

        cargando =
            false

        if (::horarioAdapter.isInitialized) {
            horarioAdapter.actualizarLista(
                emptyList()
            )
        }

        if (!vistaDisponible()) {
            return
        }

        rvHorarios.visibility =
            View.GONE

        fabNuevoHorario.isEnabled =
            false

        tvDescripcion.text =
            "Necesitas conexión a Internet para consultar y administrar horarios."

        tvDescripcion.visibility =
            View.VISIBLE
    }

    private fun mostrarErrorLista(
        mensaje: String
    ) {
        horarioAdapter.actualizarLista(
            emptyList()
        )

        rvHorarios.visibility =
            View.GONE

        tvDescripcion.text =
            mensaje

        tvDescripcion.visibility =
            View.VISIBLE
    }

    private fun abrirRegistroHorario() {
        if (cargando) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            Toast.makeText(
                requireContext(),
                "Necesitas conexión a Internet para registrar un horario.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.flContenedor,
                RegistrarHorarioFragment()
            )
            .addToBackStack(
                "LISTA_HORARIOS"
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