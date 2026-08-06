package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.entity.Especialidad
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.EspecialidadRepository
import pe.edu.idat.clinicasanmiguel.repository.MedicoAdminRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaEspecialidadesApi
import pe.edu.idat.clinicasanmiguel.repository.ResultadoRegistroMedicoAdminApi
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor

class RegistrarDoctorFragment :
    Fragment(
        R.layout.activity_registro_doctor
    ) {

    private lateinit var spnEspecialidades:
            Spinner

    private lateinit var etNombre:
            TextInputEditText

    private lateinit var etApellido:
            TextInputEditText

    private lateinit var etCorreo:
            TextInputEditText

    private lateinit var etPassword:
            TextInputEditText

    private lateinit var etConfirmarPassword:
            TextInputEditText

    private lateinit var btnGuardar:
            MaterialButton

    private lateinit var pbCargando:
            ProgressBar

    private lateinit var especialidadRepository:
            EspecialidadRepository

    private lateinit var medicoRepository:
            MedicoAdminRepository

    private var listaEspecialidades:
            List<Especialidad> =
        emptyList()

    private var cargandoEspecialidades =
        false

    private var registrando =
        false
    private var idSolicitudEspecialidades =
        0L

    private var idSolicitudRegistro =
        0L

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

        medicoRepository =
            MedicoAdminRepository(
                requireContext()
            )

        spnEspecialidades =
            view.findViewById(
                R.id.spnEspecialidades
            )

        etNombre =
            view.findViewById(
                R.id.etNombreDoc
            )

        etApellido =
            view.findViewById(
                R.id.etApellidoDoc
            )

        etCorreo =
            view.findViewById(
                R.id.etCorreoDoc
            )

        etPassword =
            view.findViewById(
                R.id.etPasswordDoc
            )

        etConfirmarPassword =
            view.findViewById(
                R.id.etConfirmarPasswordDoc
            )

        btnGuardar =
            view.findViewById(
                R.id.btnGuardarDoc
            )

        pbCargando =
            view.findViewById(
                R.id.pbCargandoDoc
            )

        btnGuardar.setOnClickListener {
            registrarDoctor()
        }

        configurarSpinnerVacio(
            "Cargando especialidades..."
        )

        observarConexion()

        if (NetworkMonitor.hayInternet()) {
            cargarEspecialidadesDesdeApi()
        } else {
            mostrarSinConexion()
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

                if (
                    !cargandoEspecialidades &&
                    !registrando &&
                    listaEspecialidades.isEmpty()
                ) {
                    cargarEspecialidadesDesdeApi()
                } else {
                    actualizarEstadoFormulario()
                }
            }
    }

    private fun cargarEspecialidadesDesdeApi() {
        if (
            cargandoEspecialidades ||
            registrando ||
            !vistaDisponible()
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarSinConexion()
            return
        }

        cargandoEspecialidades =
            true

        listaEspecialidades =
            emptyList()

        val solicitudActual =
            ++idSolicitudEspecialidades

        mostrarCargando(
            cargando = true,
            mensajeBoton = "CARGANDO..."
        )

        configurarSpinnerVacio(
            "Cargando especialidades..."
        )

        especialidadRepository
            .listarEspecialidadesApi callback@{
                    resultado ->

                if (
                    solicitudActual != idSolicitudEspecialidades ||
                    !vistaDisponible()
                ) {
                    return@callback
                }

                cargandoEspecialidades =
                    false

                when (resultado) {
                    is ResultadoCargaEspecialidadesApi.Exito -> {
                        listaEspecialidades =
                            resultado.especialidades

                        if (listaEspecialidades.isEmpty()) {
                            configurarSpinnerVacio(
                                "No hay especialidades registradas"
                            )

                            mostrarCargando(
                                cargando = false
                            )

                            Toast.makeText(
                                requireContext(),
                                "Primero debe registrar una especialidad.",
                                Toast.LENGTH_LONG
                            ).show()

                            return@callback
                        }

                        cargarSpinnerEspecialidades()

                        mostrarCargando(
                            cargando = false
                        )
                    }

                    is ResultadoCargaEspecialidadesApi.SinConexion -> {
                        mostrarCargando(
                            cargando = false
                        )

                        mostrarSinConexion()
                    }

                    is ResultadoCargaEspecialidadesApi.SesionExpirada -> {
                        mostrarCargando(
                            cargando = false
                        )

                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoCargaEspecialidadesApi.SinPermiso -> {
                        listaEspecialidades =
                            emptyList()

                        configurarSpinnerVacio(
                            "Acceso denegado"
                        )

                        mostrarCargando(
                            cargando = false
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaEspecialidadesApi.Error -> {
                        listaEspecialidades =
                            emptyList()

                        configurarSpinnerVacio(
                            "No se pudieron cargar las especialidades"
                        )

                        mostrarCargando(
                            cargando = false
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

    private fun cargarSpinnerEspecialidades() {
        val opciones =
            mutableListOf(
                "Seleccione una especialidad"
            ).apply {
                addAll(
                    listaEspecialidades.map {
                        it.nombre
                    }
                )
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                opciones
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spnEspecialidades.adapter =
            adapter

        spnEspecialidades.setSelection(
            0
        )
    }

    private fun configurarSpinnerVacio(
        mensaje: String
    ) {
        if (!vistaDisponible()) {
            return
        }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf(mensaje)
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spnEspecialidades.adapter =
            adapter

        spnEspecialidades.isEnabled =
            false
    }

    private fun registrarDoctor() {
        if (
            registrando ||
            cargandoEspecialidades
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarSinConexion()
            return
        }

        limpiarErrores()

        val nombre =
            obtenerTexto(
                etNombre
            )

        val apellido =
            obtenerTexto(
                etApellido
            )

        val correo =
            obtenerTexto(
                etCorreo
            ).lowercase()

        val password =
            etPassword.text
                ?.toString()
                .orEmpty()

        val confirmarPassword =
            etConfirmarPassword.text
                ?.toString()
                .orEmpty()

        if (nombre.isBlank()) {
            etNombre.error =
                "Ingrese los nombres del médico"

            etNombre.requestFocus()
            return
        }

        if (apellido.isBlank()) {
            etApellido.error =
                "Ingrese los apellidos del médico"

            etApellido.requestFocus()
            return
        }

        if (correo.isBlank()) {
            etCorreo.error =
                "Ingrese el correo del médico"

            etCorreo.requestFocus()
            return
        }

        if (
            !Patterns.EMAIL_ADDRESS
                .matcher(correo)
                .matches()
        ) {
            etCorreo.error =
                "Ingrese un correo válido"

            etCorreo.requestFocus()
            return
        }

        if (password.isBlank()) {
            etPassword.error =
                "Ingrese una contraseña"

            etPassword.requestFocus()
            return
        }

        if (confirmarPassword.isBlank()) {
            etConfirmarPassword.error =
                "Confirme la contraseña"

            etConfirmarPassword.requestFocus()
            return
        }

        if (password != confirmarPassword) {
            etConfirmarPassword.error =
                "Las contraseñas no coinciden"

            etConfirmarPassword.requestFocus()
            return
        }

        if (listaEspecialidades.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Primero debe registrar una especialidad.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val posicionSeleccionada =
            spnEspecialidades.selectedItemPosition

        if (posicionSeleccionada <= 0) {
            Toast.makeText(
                requireContext(),
                "Seleccione una especialidad.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val indiceEspecialidad =
            posicionSeleccionada - 1

        if (
            indiceEspecialidad !in
            listaEspecialidades.indices
        ) {
            Toast.makeText(
                requireContext(),
                "No se pudo identificar la especialidad.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val especialidadSeleccionada =
            listaEspecialidades[
                indiceEspecialidad
            ]

        val nombreCompleto =
            "$nombre $apellido"
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        registrando =
            true

        val solicitudActual =
            ++idSolicitudRegistro

        mostrarCargando(
            cargando = true,
            mensajeBoton = "REGISTRANDO..."
        )

        medicoRepository
            .registrarMedicoApi(
                nombre = nombreCompleto,
                idEspecialidad =
                    especialidadSeleccionada.id,
                correo = correo,
                password = password
            ) callback@{
                    resultado ->

                if (
                    solicitudActual != idSolicitudRegistro ||
                    !vistaDisponible()
                ) {
                    return@callback
                }

                registrando =
                    false

                mostrarCargando(
                    cargando = false
                )

                when (resultado) {
                    is ResultadoRegistroMedicoAdminApi.Exito -> {
                        Toast.makeText(
                            requireContext(),
                            "${resultado.mensaje} en ${resultado.medico.especialidad}",
                            Toast.LENGTH_LONG
                        ).show()

                        /*
                         * Regresa a ListaDoctoresAdminFragment.
                         * Su onResume consultará nuevamente la API.
                         */
                        parentFragmentManager
                            .popBackStack()
                    }

                    is ResultadoRegistroMedicoAdminApi.Duplicado -> {
                        etCorreo.error =
                            resultado.mensaje

                        etCorreo.requestFocus()
                    }

                    is ResultadoRegistroMedicoAdminApi.SinConexion -> {
                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()

                        mostrarSinConexion()
                    }

                    is ResultadoRegistroMedicoAdminApi.SinPermiso -> {
                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoRegistroMedicoAdminApi.SesionExpirada -> {
                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoRegistroMedicoAdminApi.Error -> {
                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun mostrarCargando(
        cargando: Boolean,
        mensajeBoton: String =
            "REGISTRAR DOCTOR"
    ) {
        if (!vistaDisponible()) {
            return
        }

        pbCargando.visibility =
            if (cargando) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnGuardar.text =
            mensajeBoton

        actualizarEstadoFormulario()
    }

    private fun actualizarEstadoFormulario() {
        if (!vistaDisponible()) {
            return
        }

        val habilitado =
            NetworkMonitor.hayInternet() &&
                    !cargandoEspecialidades &&
                    !registrando &&
                    listaEspecialidades.isNotEmpty()

        etNombre.isEnabled =
            habilitado

        etApellido.isEnabled =
            habilitado

        etCorreo.isEnabled =
            habilitado

        etPassword.isEnabled =
            habilitado

        etConfirmarPassword.isEnabled =
            habilitado

        spnEspecialidades.isEnabled =
            habilitado

        btnGuardar.isEnabled =
            habilitado

        if (
            !cargandoEspecialidades &&
            !registrando
        ) {
            btnGuardar.text =
                "REGISTRAR DOCTOR"
        }
    }

    private fun mostrarSinConexion() {
        idSolicitudEspecialidades++

        cargandoEspecialidades =
            false

        listaEspecialidades =
            emptyList()

        if (!vistaDisponible()) {
            return
        }

        pbCargando.visibility =
            View.GONE

        configurarSpinnerVacio(
            "Sin conexión a Internet"
        )

        actualizarEstadoFormulario()

        Toast.makeText(
            requireContext(),
            "Necesitas conexión a Internet para registrar médicos.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun limpiarErrores() {
        etNombre.error =
            null

        etApellido.error =
            null

        etCorreo.error =
            null

        etPassword.error =
            null

        etConfirmarPassword.error =
            null
    }

    private fun obtenerTexto(
        campo: TextInputEditText
    ): String {
        return campo.text
            ?.toString()
            ?.trim()
            .orEmpty()
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

    override fun onDestroyView() {
        idSolicitudEspecialidades++
        idSolicitudRegistro++

        cargandoEspecialidades =
            false

        registrando =
            false

        super.onDestroyView()
    }
}