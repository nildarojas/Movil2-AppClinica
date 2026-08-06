package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.entity.Especialidad
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.EspecialidadRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaEspecialidadesApi
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor

class ListaEspecialidadesFragment :
    Fragment() {

    private lateinit var rvEspecialidades:
            RecyclerView

    private lateinit var etBuscarEspecialidad:
            TextInputEditText

    private lateinit var tvEstadoEspecialidades:
            TextView

    private lateinit var pbEspecialidades:
            ProgressBar

    private lateinit var fabNuevaEspecialidad:
            FloatingActionButton

    private lateinit var especialidadRepository:
            EspecialidadRepository

    private lateinit var especialidadAdapter:
            EspecialidadAdminAdapter
    private var especialidadesCompletas:
            List<Especialidad> =
        emptyList()

    private var cargando =
        false

    private var cargaCompletada =
        false
    private var idSolicitud =
        0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.activity_lista_especialidades,
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

        especialidadRepository =
            EspecialidadRepository(
                requireContext()
            )

        rvEspecialidades =
            view.findViewById(
                R.id.rvEspecialidades
            )

        etBuscarEspecialidad =
            view.findViewById(
                R.id.etBuscarEspecialidad
            )

        tvEstadoEspecialidades =
            view.findViewById(
                R.id.tvEstadoEspecialidades
            )

        pbEspecialidades =
            view.findViewById(
                R.id.pbEspecialidades
            )

        fabNuevaEspecialidad =
            view.findViewById(
                R.id.fabNuevaEspecialidad
            )

        configurarRecyclerView()

        configurarBusqueda()

        fabNuevaEspecialidad
            .setOnClickListener {
                abrirRegistroEspecialidad()
            }

        observarConexion()
    }

    override fun onResume() {
        super.onResume()
        if (NetworkMonitor.hayInternet()) {
            cargarEspecialidades()
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
        especialidadAdapter =
            EspecialidadAdminAdapter(
                listaEspecialidades =
                    emptyList()
            )

        rvEspecialidades.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                especialidadAdapter

            setHasFixedSize(
                true
            )
        }
    }

    private fun configurarBusqueda() {
        etBuscarEspecialidad
            .addTextChangedListener(
                object : TextWatcher {

                    override fun beforeTextChanged(
                        texto: CharSequence?,
                        inicio: Int,
                        cantidad: Int,
                        despues: Int
                    ) {
                    }

                    override fun onTextChanged(
                        texto: CharSequence?,
                        inicio: Int,
                        antes: Int,
                        cantidad: Int
                    ) {
                        filtrarEspecialidades(
                            texto
                                ?.toString()
                                .orEmpty()
                        )
                    }

                    override fun afterTextChanged(
                        texto: Editable?
                    ) {
                    }
                }
            )
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

                fabNuevaEspecialidad.isEnabled =
                    !cargando

                if (
                    !cargando &&
                    !cargaCompletada
                ) {
                    cargarEspecialidades()
                }
            }
    }

    private fun cargarEspecialidades() {
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

        cargaCompletada =
            false

        val solicitudActual =
            ++idSolicitud

        mostrarCargando()

        especialidadRepository
            .listarEspecialidadesApi {
                    resultado ->

                if (
                    !solicitudVigente(
                        solicitudActual
                    )
                ) {
                    return@listarEspecialidadesApi
                }

                cargando =
                    false

                pbEspecialidades.visibility =
                    View.GONE

                fabNuevaEspecialidad.isEnabled =
                    NetworkMonitor.hayInternet()

                if (!NetworkMonitor.hayInternet()) {
                    mostrarSinConexion()

                    return@listarEspecialidadesApi
                }

                when (resultado) {
                    is ResultadoCargaEspecialidadesApi.Exito -> {
                        cargaCompletada =
                            true

                        especialidadesCompletas =
                            resultado
                                .especialidades
                                .sortedBy {
                                        especialidad ->

                                    especialidad
                                        .nombre
                                        .lowercase()
                                }

                        etBuscarEspecialidad.isEnabled =
                            true

                        filtrarEspecialidades(
                            etBuscarEspecialidad
                                .text
                                ?.toString()
                                .orEmpty()
                        )
                    }

                    is ResultadoCargaEspecialidadesApi.SinConexion -> {
                        mostrarErrorLista(
                            resultado.mensaje
                        )
                    }

                    is ResultadoCargaEspecialidadesApi.Error -> {
                        mostrarErrorLista(
                            resultado.mensaje
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaEspecialidadesApi.SesionExpirada -> {
                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoCargaEspecialidadesApi.SinPermiso -> {
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

    private fun filtrarEspecialidades(
        texto: String
    ) {
        if (
            !::especialidadAdapter.isInitialized ||
            !vistaDisponible()
        ) {
            return
        }

        val consulta =
            texto.trim()

        val especialidadesFiltradas =
            if (consulta.isBlank()) {
                especialidadesCompletas
            } else {
                especialidadesCompletas.filter {
                        especialidad ->

                    especialidad.nombre.contains(
                        consulta,
                        ignoreCase = true
                    )
                }
            }

        especialidadAdapter
            .actualizarLista(
                especialidadesFiltradas
            )

        when {
            !NetworkMonitor.hayInternet() -> {
                mostrarMensajeEstado(
                    "Necesitas conexión a Internet para consultar las especialidades."
                )
            }

            !cargaCompletada -> {
            }

            especialidadesCompletas.isEmpty() -> {
                mostrarMensajeEstado(
                    "No hay especialidades registradas."
                )
            }

            especialidadesFiltradas.isEmpty() -> {
                mostrarMensajeEstado(
                    "No se encontraron especialidades con ese nombre."
                )
            }

            else -> {
                tvEstadoEspecialidades.visibility =
                    View.GONE

                rvEspecialidades.visibility =
                    View.VISIBLE
            }
        }
    }

    private fun mostrarCargando() {
        pbEspecialidades.visibility =
            View.VISIBLE

        tvEstadoEspecialidades.visibility =
            View.GONE

        fabNuevaEspecialidad.isEnabled =
            false

        etBuscarEspecialidad.isEnabled =
            false

        if (especialidadesCompletas.isEmpty()) {
            rvEspecialidades.visibility =
                View.GONE
        }
    }

    private fun mostrarSinConexion() {
        idSolicitud++

        cargando =
            false

        cargaCompletada =
            false

        especialidadesCompletas =
            emptyList()

        if (::especialidadAdapter.isInitialized) {
            especialidadAdapter
                .actualizarLista(
                    emptyList()
                )
        }

        if (!vistaDisponible()) {
            return
        }

        pbEspecialidades.visibility =
            View.GONE

        rvEspecialidades.visibility =
            View.GONE

        etBuscarEspecialidad.isEnabled =
            false

        fabNuevaEspecialidad.isEnabled =
            false

        mostrarMensajeEstado(
            "Necesitas conexión a Internet para consultar y administrar especialidades."
        )
    }

    private fun mostrarErrorLista(
        mensaje: String
    ) {
        cargaCompletada =
            false

        especialidadesCompletas =
            emptyList()

        especialidadAdapter
            .actualizarLista(
                emptyList()
            )

        pbEspecialidades.visibility =
            View.GONE

        rvEspecialidades.visibility =
            View.GONE

        etBuscarEspecialidad.isEnabled =
            false

        mostrarMensajeEstado(
            mensaje
        )
    }

    private fun mostrarMensajeEstado(
        mensaje: String
    ) {
        tvEstadoEspecialidades.text =
            mensaje

        tvEstadoEspecialidades.visibility =
            View.VISIBLE

        rvEspecialidades.visibility =
            View.GONE
    }

    private fun abrirRegistroEspecialidad() {
        if (cargando) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            Toast.makeText(
                requireContext(),
                "Necesitas conexión a Internet para registrar una especialidad.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.flContenedor,
                RegistrarEspecialidadFragment()
            )
            .addToBackStack(
                "LISTA_ESPECIALIDADES"
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

    private fun solicitudVigente(
        solicitudId: Long
    ): Boolean {
        return solicitudId ==
                idSolicitud &&
                vistaDisponible()
    }

    private fun vistaDisponible(): Boolean {
        return isAdded &&
                view != null
    }
}