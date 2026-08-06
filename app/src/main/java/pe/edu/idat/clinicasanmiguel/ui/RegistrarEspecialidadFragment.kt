package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.EspecialidadRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoRegistroEspecialidadApi
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor

class RegistrarEspecialidadFragment :
    Fragment(
        R.layout.activity_registrar_especialidad
    ) {

    private lateinit var etNombreEspecialidad:
            TextInputEditText

    private lateinit var btnGuardar:
            MaterialButton

    private lateinit var btnCancelar:
            MaterialButton

    private lateinit var especialidadRepository:
            EspecialidadRepository

    private var guardando =
        false

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

        etNombreEspecialidad =
            view.findViewById(
                R.id.etNombreEspecialidad
            )

        btnGuardar =
            view.findViewById(
                R.id.btnGuardarEspecialidad
            )

        btnCancelar =
            view.findViewById(
                R.id.btnCancelarEspecialidad
            )

        btnGuardar.setOnClickListener {
            registrarEspecialidad()
        }

        btnCancelar.setOnClickListener {
            if (!guardando) {
                regresar()
            }
        }

        observarConexion()
    }

    private fun observarConexion() {
        NetworkMonitor
            .estadoConexion
            .observe(
                viewLifecycleOwner
            ) { conectado ->

                if (guardando) {
                    return@observe
                }

                btnGuardar.isEnabled =
                    conectado

                etNombreEspecialidad.isEnabled =
                    conectado

                btnCancelar.isEnabled =
                    true
            }
    }

    private fun registrarEspecialidad() {
        if (guardando) {
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

        val nombreEspecialidad =
            etNombreEspecialidad
                .text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (nombreEspecialidad.isBlank()) {
            etNombreEspecialidad.error =
                "Ingrese el nombre de la especialidad"

            etNombreEspecialidad.requestFocus()

            return
        }

        etNombreEspecialidad.error =
            null

        mostrarCargando(
            true
        )

        especialidadRepository
            .registrarEspecialidadApi(
                nombre = nombreEspecialidad
            ) { resultado ->

                if (
                    !isAdded ||
                    view == null
                ) {
                    return@registrarEspecialidadApi
                }

                when (resultado) {
                    is ResultadoRegistroEspecialidadApi.Exito -> {
                        mostrarCargando(
                            false
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_SHORT
                        ).show()

                        regresar()
                    }

                    is ResultadoRegistroEspecialidadApi.Duplicado -> {
                        mostrarCargando(
                            false
                        )

                        etNombreEspecialidad.error =
                            resultado.mensaje

                        etNombreEspecialidad.requestFocus()
                    }

                    is ResultadoRegistroEspecialidadApi.SinConexion -> {
                        mostrarCargando(
                            false
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoRegistroEspecialidadApi.SinPermiso -> {
                        mostrarCargando(
                            false
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoRegistroEspecialidadApi.SesionExpirada -> {
                        mostrarCargando(
                            false
                        )

                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoRegistroEspecialidadApi.Error -> {
                        mostrarCargando(
                            false
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

    private fun mostrarCargando(
        cargando: Boolean
    ) {
        guardando =
            cargando

        val conectado =
            NetworkMonitor.hayInternet()

        btnGuardar.isEnabled =
            !cargando && conectado

        btnCancelar.isEnabled =
            !cargando

        etNombreEspecialidad.isEnabled =
            !cargando && conectado

        btnGuardar.text =
            if (cargando) {
                "GUARDANDO..."
            } else {
                "GUARDAR"
            }
    }

    private fun regresar() {
        parentFragmentManager
            .popBackStack()
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

    override fun onDestroyView() {
        guardando =
            false

        super.onDestroyView()
    }
}