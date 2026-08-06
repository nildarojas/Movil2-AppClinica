package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.entity.Usuario
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.network.UsuarioLoginApi
import pe.edu.idat.clinicasanmiguel.repository.ResultadoActualizarPerfilApi
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository
import pe.edu.idat.clinicasanmiguel.utils.CacheManager
import pe.edu.idat.clinicasanmiguel.utils.LoadingController
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor

class EditarPerfilFragment :
    Fragment(R.layout.fragment_editar_perfil) {

    private lateinit var etDni:
            TextInputEditText

    private lateinit var etNombre:
            TextInputEditText

    private lateinit var etApellido:
            TextInputEditText

    private lateinit var etCorreo:
            TextInputEditText

    private lateinit var etTelefono:
            TextInputEditText

    private lateinit var etFechaNacimiento:
            TextInputEditText

    private lateinit var etGenero:
            TextInputEditText

    private lateinit var btnGuardar:
            MaterialButton

    private lateinit var btnCancelar:
            MaterialButton

    private lateinit var usuarioRepository:
            UsuarioRepository

    private lateinit var cacheManager:
            CacheManager

    private lateinit var sessionManager:
            SessionManager

    private lateinit var loadingController:
            LoadingController

    private lateinit var usuarioOriginal:
            UsuarioLoginApi

    private var guardando =
        false

    private var loadingToken:
            Long? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        usuarioRepository =
            UsuarioRepository(
                requireContext()
            )

        cacheManager =
            CacheManager(
                requireContext()
            )

        sessionManager =
            SessionManager(
                requireContext()
            )

        loadingController =
            LoadingController(
                fragmentManager =
                    parentFragmentManager,
                coroutineScope =
                    viewLifecycleOwner.lifecycleScope
            )

        etDni =
            view.findViewById(
                R.id.etEditarDni
            )

        etNombre =
            view.findViewById(
                R.id.etEditarNombre
            )

        etApellido =
            view.findViewById(
                R.id.etEditarApellido
            )

        etCorreo =
            view.findViewById(
                R.id.etEditarCorreo
            )

        etTelefono =
            view.findViewById(
                R.id.etEditarTelefono
            )

        etFechaNacimiento =
            view.findViewById(
                R.id.etEditarFechaNacimiento
            )

        etGenero =
            view.findViewById(
                R.id.etEditarGenero
            )

        btnGuardar =
            view.findViewById(
                R.id.btnGuardarPerfil
            )

        btnCancelar =
            view.findViewById(
                R.id.btnCancelarEdicion
            )

        val usuario =
            obtenerUsuarioArgumentos()

        if (usuario == null) {
            Toast.makeText(
                requireContext(),
                "No se recibieron los datos del perfil.",
                Toast.LENGTH_LONG
            ).show()

            parentFragmentManager
                .popBackStack()

            return
        }

        usuarioOriginal =
            usuario

        mostrarDatos(
            usuario
        )

        btnGuardar.setOnClickListener {
            guardarCambios()
        }

        btnCancelar.setOnClickListener {
            if (!guardando) {
                parentFragmentManager
                    .popBackStack()
            }
        }
    }

    private fun mostrarDatos(
        usuario: UsuarioLoginApi
    ) {
        etDni.setText(
            usuario.dni
        )

        etNombre.setText(
            usuario.nombre
        )

        etApellido.setText(
            usuario.apellido
        )

        etCorreo.setText(
            usuario.correo
        )

        etTelefono.setText(
            usuario.telefono
        )

        etFechaNacimiento.setText(
            usuario.fechaNacimiento
        )

        etGenero.setText(
            usuario.genero
        )
    }

    private fun guardarCambios() {
        if (guardando) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            Toast.makeText(
                requireContext(),
                "Editar el perfil requiere conexión a Internet.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val dni =
            obtenerTexto(etDni)

        val nombre =
            obtenerTexto(etNombre)

        val apellido =
            obtenerTexto(etApellido)

        val correo =
            obtenerTexto(etCorreo)
                .lowercase()

        val telefono =
            obtenerTexto(etTelefono)

        val fechaNacimiento =
            obtenerTexto(
                etFechaNacimiento
            )

        val genero =
            obtenerTexto(etGenero)

        if (
            dni.isBlank() ||
            nombre.isBlank() ||
            apellido.isBlank() ||
            correo.isBlank() ||
            telefono.isBlank() ||
            fechaNacimiento.isBlank() ||
            genero.isBlank()
        ) {
            Toast.makeText(
                requireContext(),
                "Todos los campos son obligatorios.",
                Toast.LENGTH_LONG
            ).show()

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

        val idUsuarioLocal =
            sessionManager
                .obtenerIdUsuarioLocal()

        if (idUsuarioLocal == null) {
            Toast.makeText(
                requireContext(),
                "No se encontró la sesión local del usuario.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val usuarioEditado =
            Usuario(
                id = idUsuarioLocal,
                dni = dni,
                nombre = nombre,
                apellido = apellido,
                correo = correo,
                password = "",
                telefono = telefono,
                fechaNacimiento =
                    fechaNacimiento,
                genero = genero,
                rol = usuarioOriginal.rol
            )

        iniciarGuardado()

        usuarioRepository
            .actualizarPerfilApi(
                usuarioEditado
            ) { resultado ->

                activity
                    ?.runOnUiThread {

                        if (
                            !isAdded ||
                            view == null
                        ) {
                            return@runOnUiThread
                        }

                        finalizarGuardado(
                            resultado
                        )
                    }
            }
    }

    private fun iniciarGuardado() {
        guardando =
            true

        habilitarFormulario(
            false
        )

        loadingToken =
            loadingController.show(
                message =
                    "Actualizando perfil..."
            )
    }

    private fun finalizarGuardado(
        resultado: ResultadoActualizarPerfilApi
    ) {
        val token =
            loadingToken

        loadingToken =
            null

        if (token == null) {
            procesarResultado(
                resultado
            )

            return
        }

        loadingController.hide(
            requestToken = token
        ) callback@{

            if (
                !isAdded ||
                view == null
            ) {
                return@callback
            }

            procesarResultado(
                resultado
            )
        }
    }

    private fun procesarResultado(
        resultado: ResultadoActualizarPerfilApi
    ) {
        guardando =
            false

        habilitarFormulario(
            true
        )

        when (resultado) {
            is ResultadoActualizarPerfilApi.Exito -> {
                val usuarioLocal =
                    resultado.usuario

                val usuarioActualizadoApi =
                    UsuarioLoginApi(
                        id = usuarioOriginal.id,
                        dni = usuarioLocal.dni,
                        nombre = usuarioLocal.nombre,
                        apellido = usuarioLocal.apellido,
                        correo = usuarioLocal.correo,
                        telefono =
                            usuarioLocal.telefono,
                        fechaNacimiento =
                            usuarioLocal.fechaNacimiento,
                        genero = usuarioLocal.genero,
                        rol = usuarioLocal.rol
                    )

                cacheManager.guardarObjeto(
                    CacheManager.PERFIL_USUARIO,
                    usuarioActualizadoApi
                )

                sessionManager.actualizarNombreUsuario(
                    nombre = usuarioLocal.nombre,
                    apellido = usuarioLocal.apellido
                )

                Toast.makeText(
                    requireContext(),
                    resultado.mensaje,
                    Toast.LENGTH_LONG
                ).show()

                parentFragmentManager
                    .popBackStack()
            }

            is ResultadoActualizarPerfilApi.DatosDuplicados -> {
                Toast.makeText(
                    requireContext(),
                    resultado.mensaje,
                    Toast.LENGTH_LONG
                ).show()
            }

            is ResultadoActualizarPerfilApi.SinConexion -> {
                Toast.makeText(
                    requireContext(),
                    resultado.mensaje,
                    Toast.LENGTH_LONG
                ).show()
            }

            is ResultadoActualizarPerfilApi.SesionExpirada -> {
                Toast.makeText(
                    requireContext(),
                    resultado.mensaje,
                    Toast.LENGTH_LONG
                ).show()

                cerrarSesion()
            }

            is ResultadoActualizarPerfilApi.Error -> {
                Toast.makeText(
                    requireContext(),
                    resultado.mensaje,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun habilitarFormulario(
        habilitado: Boolean
    ) {
        etDni.isEnabled =
            habilitado

        etNombre.isEnabled =
            habilitado

        etApellido.isEnabled =
            habilitado

        etCorreo.isEnabled =
            habilitado

        etTelefono.isEnabled =
            habilitado

        etFechaNacimiento.isEnabled =
            habilitado

        etGenero.isEnabled =
            habilitado

        btnGuardar.isEnabled =
            habilitado

        btnCancelar.isEnabled =
            habilitado
    }

    private fun obtenerTexto(
        campo: TextInputEditText
    ): String {
        return campo.text
            ?.toString()
            ?.trim()
            .orEmpty()
    }

    private fun cerrarSesion() {
        sessionManager
            .limpiarSesion()

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

        requireActivity()
            .finish()
    }

    private fun obtenerUsuarioArgumentos():
            UsuarioLoginApi? {
        val argumentos =
            arguments
                ?: return null

        val id =
            argumentos.getInt(
                ARG_ID,
                -1
            )

        if (id <= 0) {
            return null
        }

        return UsuarioLoginApi(
            id = id,
            dni = argumentos
                .getString(ARG_DNI)
                .orEmpty(),
            nombre = argumentos
                .getString(ARG_NOMBRE)
                .orEmpty(),
            apellido = argumentos
                .getString(ARG_APELLIDO)
                .orEmpty(),
            correo = argumentos
                .getString(ARG_CORREO)
                .orEmpty(),
            telefono = argumentos
                .getString(ARG_TELEFONO)
                .orEmpty(),
            fechaNacimiento = argumentos
                .getString(ARG_FECHA_NACIMIENTO)
                .orEmpty(),
            genero = argumentos
                .getString(ARG_GENERO)
                .orEmpty(),
            rol = argumentos
                .getString(ARG_ROL)
                .orEmpty()
        )
    }

    override fun onDestroyView() {
        if (::loadingController.isInitialized) {
            loadingController.forceHide()
        }

        super.onDestroyView()
    }

    companion object {
        private const val ARG_ID =
            "ARG_ID"

        private const val ARG_DNI =
            "ARG_DNI"

        private const val ARG_NOMBRE =
            "ARG_NOMBRE"

        private const val ARG_APELLIDO =
            "ARG_APELLIDO"

        private const val ARG_CORREO =
            "ARG_CORREO"

        private const val ARG_TELEFONO =
            "ARG_TELEFONO"

        private const val ARG_FECHA_NACIMIENTO =
            "ARG_FECHA_NACIMIENTO"

        private const val ARG_GENERO =
            "ARG_GENERO"

        private const val ARG_ROL =
            "ARG_ROL"

        fun nuevaInstancia(
            usuario: UsuarioLoginApi
        ): EditarPerfilFragment {
            return EditarPerfilFragment()
                .apply {
                    arguments =
                        Bundle().apply {
                            putInt(
                                ARG_ID,
                                usuario.id
                            )

                            putString(
                                ARG_DNI,
                                usuario.dni
                            )

                            putString(
                                ARG_NOMBRE,
                                usuario.nombre
                            )

                            putString(
                                ARG_APELLIDO,
                                usuario.apellido
                            )

                            putString(
                                ARG_CORREO,
                                usuario.correo
                            )

                            putString(
                                ARG_TELEFONO,
                                usuario.telefono
                            )

                            putString(
                                ARG_FECHA_NACIMIENTO,
                                usuario.fechaNacimiento
                            )

                            putString(
                                ARG_GENERO,
                                usuario.genero
                            )

                            putString(
                                ARG_ROL,
                                usuario.rol
                            )
                        }
                }
        }
    }
}