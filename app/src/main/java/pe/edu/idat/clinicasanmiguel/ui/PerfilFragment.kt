package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.network.UsuarioLoginApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilFragment :
    Fragment(R.layout.activity_perfil) {

    private lateinit var tvTitulo:
            TextView

    private lateinit var tvNombre:
            TextView

    private lateinit var tvApellido:
            TextView

    private lateinit var tvDni:
            TextView

    private lateinit var tvTelefono:
            TextView

    private lateinit var tvCorreo:
            TextView

    private lateinit var tvFechaNacimiento:
            TextView

    private lateinit var tvGenero:
            TextView

    private lateinit var btnRegresar:
            MaterialButton

    private var cargando =
        false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        tvTitulo =
            view.findViewById(
                R.id.tvTitulo
            )

        tvNombre =
            view.findViewById(
                R.id.tvNombre
            )

        tvApellido =
            view.findViewById(
                R.id.tvApellido
            )

        tvDni =
            view.findViewById(
                R.id.tvDni
            )

        tvTelefono =
            view.findViewById(
                R.id.tvTelefono
            )

        tvCorreo =
            view.findViewById(
                R.id.tvCorreo
            )

        tvFechaNacimiento =
            view.findViewById(
                R.id.tvFechaNacimiento
            )

        tvGenero =
            view.findViewById(
                R.id.tvGenero
            )

        btnRegresar =
            view.findViewById(
                R.id.btnRegresar
            )

        btnRegresar.setOnClickListener {
            regresar()
        }

        mostrarCargando()
        cargarPerfilDesdeApi()
    }

    private fun cargarPerfilDesdeApi() {
        if (cargando) {
            return
        }

        cargando = true

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .obtenerPerfil()
            .enqueue(
                object :
                    Callback<UsuarioLoginApi> {

                    override fun onResponse(
                        call: Call<UsuarioLoginApi>,
                        response: Response<UsuarioLoginApi>
                    ) {
                        cargando = false

                        if (!isAdded) {
                            return
                        }

                        if (response.isSuccessful) {
                            val usuario =
                                response.body()

                            if (usuario == null) {
                                mostrarErrorPerfil()

                                Toast.makeText(
                                    requireContext(),
                                    "La API devolvió una respuesta incompleta",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            mostrarPerfil(
                                usuario
                            )

                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<UsuarioLoginApi>,
                        throwable: Throwable
                    ) {
                        cargando = false

                        if (!isAdded) {
                            return
                        }

                        mostrarErrorPerfil()

                        Toast.makeText(
                            requireContext(),
                            "No se pudo conectar con el servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun mostrarPerfil(
        usuario: UsuarioLoginApi
    ) {
        tvTitulo.text =
            if (
                usuario.rol.equals(
                    "ADMIN",
                    ignoreCase = true
                )
            ) {
                "Perfil de Administrador"
            } else {
                "Mi Ficha Médica"
            }

        tvNombre.text =
            "Nombre: ${usuario.nombre}"

        tvApellido.text =
            "Apellido: ${usuario.apellido}"

        tvDni.text =
            "DNI: ${usuario.dni}"

        tvTelefono.text =
            "Teléfono: ${usuario.telefono}"

        tvCorreo.text =
            "Correo: ${usuario.correo}"

        tvFechaNacimiento.text =
            "Fecha de nacimiento: ${usuario.fechaNacimiento}"

        tvGenero.text =
            "Género: ${usuario.genero}"
    }

    private fun mostrarCargando() {
        tvTitulo.text =
            "Cargando perfil..."

        tvNombre.text =
            "Nombre:"

        tvApellido.text =
            "Apellido:"

        tvDni.text =
            "DNI:"

        tvTelefono.text =
            "Teléfono:"

        tvCorreo.text =
            "Correo:"

        tvFechaNacimiento.text =
            "Fecha de nacimiento:"

        tvGenero.text =
            "Género:"
    }

    private fun mostrarErrorPerfil() {
        tvTitulo.text =
            "No se pudo cargar el perfil"

        tvNombre.text =
            "Nombre: No disponible"

        tvApellido.text =
            "Apellido: No disponible"

        tvDni.text =
            "DNI: No disponible"

        tvTelefono.text =
            "Teléfono: No disponible"

        tvCorreo.text =
            "Correo: No disponible"

        tvFechaNacimiento.text =
            "Fecha de nacimiento: No disponible"

        tvGenero.text =
            "Género: No disponible"
    }

    private fun procesarErrorRespuesta(
        response: Response<*>
    ) {
        val mensaje =
            obtenerMensajeError(
                response
            )

        when (response.code()) {
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
                mostrarErrorPerfil()

                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "El usuario no se encuentra registrado",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                mostrarErrorPerfil()

                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "No se pudo cargar el perfil. Código ${response.code()}",
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

                json.optString(
                    "mensaje"
                ).takeIf {
                    it.isNotBlank()
                }
            }
        } catch (exception: Exception) {
            null
        }
    }

    private fun regresar() {
        if (
            parentFragmentManager
                .backStackEntryCount > 0
        ) {
            parentFragmentManager
                .popBackStack()

            return
        }

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.flContenedor,
                PacienteFragment()
            )
            .commit()
    }

    private fun cerrarSesion() {
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