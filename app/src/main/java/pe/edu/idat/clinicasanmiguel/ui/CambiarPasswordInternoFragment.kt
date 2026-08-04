package pe.edu.idat.clinicasanmiguel.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.CambiarPasswordApiRequest
import pe.edu.idat.clinicasanmiguel.network.CambiarPasswordApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CambiarPasswordInternoFragment :
    Fragment(R.layout.activity_cambiar_password_interno) {

    private lateinit var etActual:
            TextInputEditText

    private lateinit var etNueva1:
            TextInputEditText

    private lateinit var etNueva2:
            TextInputEditText

    private lateinit var btnActualizar:
            Button

    private var procesando =
        false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        val tvTitulo =
            view.findViewById<TextView>(
                R.id.tvTituloCambioInterno
            )

        etActual =
            view.findViewById(
                R.id.etPasswordActual
            )

        etNueva1 =
            view.findViewById(
                R.id.etPasswordNueva1
            )

        etNueva2 =
            view.findViewById(
                R.id.etPasswordNueva2
            )

        btnActualizar =
            view.findViewById(
                R.id.btnActualizarPasswordInterno
            )

        val preferencias =
            requireContext()
                .getSharedPreferences(
                    "sesion_clinica",
                    Context.MODE_PRIVATE
                )

        val rol =
            preferencias.getString(
                "ROL_USUARIO",
                "PACIENTE"
            ) ?: "PACIENTE"

        tvTitulo.text =
            if (
                rol.equals(
                    "ADMIN",
                    ignoreCase = true
                )
            ) {
                "Seguridad: Admin Hub"
            } else {
                "Seguridad de la Cuenta"
            }

        btnActualizar.setOnClickListener {
            validarFormulario()
        }
    }

    private fun validarFormulario() {
        val passwordActual =
            etActual.text
                ?.toString()
                .orEmpty()

        val passwordNueva =
            etNueva1.text
                ?.toString()
                .orEmpty()

        val confirmarPassword =
            etNueva2.text
                ?.toString()
                .orEmpty()

        if (
            passwordActual.isBlank() ||
            passwordNueva.isBlank() ||
            confirmarPassword.isBlank()
        ) {
            Toast.makeText(
                requireContext(),
                "Por favor, complete todos los campos",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (passwordNueva.length < 6) {
            Toast.makeText(
                requireContext(),
                "La contraseña debe tener al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (passwordNueva != confirmarPassword) {
            Toast.makeText(
                requireContext(),
                "Las nuevas contraseñas no coinciden",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        cambiarPasswordDesdeApi(
            passwordActual = passwordActual,
            passwordNueva = passwordNueva,
            confirmarPassword = confirmarPassword
        )
    }

    private fun cambiarPasswordDesdeApi(
        passwordActual: String,
        passwordNueva: String,
        confirmarPassword: String
    ) {
        if (procesando) {
            return
        }

        procesando = true
        mostrarCargando(true)

        val request =
            CambiarPasswordApiRequest(
                passwordActual = passwordActual,
                passwordNueva = passwordNueva,
                confirmarPassword = confirmarPassword
            )

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .cambiarPassword(request)
            .enqueue(
                object :
                    Callback<CambiarPasswordApiResponse> {

                    override fun onResponse(
                        call: Call<CambiarPasswordApiResponse>,
                        response: Response<CambiarPasswordApiResponse>
                    ) {
                        if (!isAdded) {
                            return
                        }

                        procesando = false
                        mostrarCargando(false)

                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (
                                respuesta != null &&
                                !respuesta.exito
                            ) {
                                Toast.makeText(
                                    requireContext(),
                                    respuesta.mensaje,
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            Toast.makeText(
                                requireContext(),
                                respuesta?.mensaje
                                    ?: "Contraseña actualizada correctamente",
                                Toast.LENGTH_LONG
                            ).show()

                            cerrarSesion()
                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<CambiarPasswordApiResponse>,
                        throwable: Throwable
                    ) {
                        if (!isAdded) {
                            return
                        }

                        procesando = false
                        mostrarCargando(false)

                        Toast.makeText(
                            requireContext(),
                            "No se pudo conectar con el servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun procesarErrorRespuesta(
        response: Response<*>
    ) {
        val mensaje =
            obtenerMensajeError(response)

        when (response.code()) {
            400 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "La contraseña actual es incorrecta o los datos no son válidos",
                    Toast.LENGTH_LONG
                ).show()
            }

            401 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "Tu sesión ha vencido",
                    Toast.LENGTH_LONG
                ).show()

                cerrarSesion()
            }

            404 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "El usuario no se encuentra registrado",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "El servidor respondió con el código ${response.code()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun obtenerMensajeError(
        response: Response<*>
    ): String? {
        return try {
            val contenido =
                response.errorBody()
                    ?.string()

            if (contenido.isNullOrBlank()) {
                null
            } else {
                val json =
                    JSONObject(contenido)

                val mensaje =
                    json.optString("mensaje")

                if (mensaje.isNotBlank()) {
                    mensaje
                } else {
                    val errores =
                        json.optJSONObject("errors")

                    if (errores != null) {
                        val claves =
                            errores.keys()

                        if (claves.hasNext()) {
                            val primerError =
                                errores.optJSONArray(
                                    claves.next()
                                )

                            primerError
                                ?.optString(0)
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                        } else {
                            json.optString("title")
                                .takeIf {
                                    it.isNotBlank()
                                }
                        }
                    } else {
                        json.optString("title")
                            .takeIf {
                                it.isNotBlank()
                            }
                    }
                }
            }
        } catch (exception: Exception) {
            null
        }
    }

    private fun mostrarCargando(
        cargando: Boolean
    ) {
        etActual.isEnabled =
            !cargando

        etNueva1.isEnabled =
            !cargando

        etNueva2.isEnabled =
            !cargando

        btnActualizar.isEnabled =
            !cargando

        btnActualizar.text =
            if (cargando) {
                "ACTUALIZANDO..."
            } else {
                "ACTUALIZAR CONTRASEÑA"
            }
    }

    private fun cerrarSesion() {
        etActual.text?.clear()
        etNueva1.text?.clear()
        etNueva2.text?.clear()

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

        startActivity(intent)
        requireActivity().finish()
    }
}