package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.EspecialidadRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoRegistroEspecialidadApi

class RegistrarEspecialidadActivity :
    AppCompatActivity() {

    private lateinit var etNombreEspecialidad:
            TextInputEditText

    private lateinit var btnGuardar:
            Button

    private lateinit var btnCancelar:
            Button

    private lateinit var especialidadRepository:
            EspecialidadRepository

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_registrar_especialidad
        )

        etNombreEspecialidad =
            findViewById(
                R.id.etNombreEspecialidad
            )

        btnGuardar =
            findViewById(
                R.id.btnGuardarEspecialidad
            )

        btnCancelar =
            findViewById(
                R.id.btnCancelarEspecialidad
            )

        especialidadRepository =
            EspecialidadRepository(this)

        btnGuardar.setOnClickListener {
            registrarEspecialidad()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun registrarEspecialidad() {
        val nombreEspecialidad =
            etNombreEspecialidad
                .text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (nombreEspecialidad.isEmpty()) {
            etNombreEspecialidad.error =
                "Ingrese el nombre de la especialidad"

            etNombreEspecialidad.requestFocus()

            return
        }

        mostrarCargando(true)

        especialidadRepository
            .registrarEspecialidadApi(
                nombre = nombreEspecialidad
            ) {
                    resultado ->

                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@registrarEspecialidadApi
                }

                when (resultado) {
                    is ResultadoRegistroEspecialidadApi
                    .Exito -> {

                        mostrarCargando(false)

                        Toast.makeText(
                            this,
                            resultado.mensaje,
                            Toast.LENGTH_SHORT
                        ).show()

                        setResult(
                            RESULT_OK
                        )

                        finish()
                    }

                    is ResultadoRegistroEspecialidadApi
                    .Duplicado -> {

                        mostrarCargando(false)

                        etNombreEspecialidad.error =
                            resultado.mensaje

                        etNombreEspecialidad
                            .requestFocus()
                    }

                    is ResultadoRegistroEspecialidadApi
                    .SinConexion -> {

                        mostrarCargando(false)

                        Toast.makeText(
                            this,
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoRegistroEspecialidadApi
                    .SinPermiso -> {

                        mostrarCargando(false)

                        Toast.makeText(
                            this,
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoRegistroEspecialidadApi
                    .SesionExpirada -> {

                        mostrarCargando(false)

                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoRegistroEspecialidadApi
                    .Error -> {

                        mostrarCargando(false)

                        Toast.makeText(
                            this,
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
        btnGuardar.isEnabled =
            !cargando

        btnCancelar.isEnabled =
            !cargando

        etNombreEspecialidad.isEnabled =
            !cargando

        btnGuardar.text =
            if (cargando) {
                "GUARDANDO..."
            } else {
                "GUARDAR"
            }
    }

    private fun cerrarSesion(
        mensaje: String
    ) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()

        SessionManager(
            this
        ).limpiarSesion()

        val intent =
            Intent(
                this,
                LoginActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }
}