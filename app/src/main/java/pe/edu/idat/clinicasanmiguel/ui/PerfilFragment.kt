package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.network.UsuarioLoginApi
import pe.edu.idat.clinicasanmiguel.utils.CacheManager
import pe.edu.idat.clinicasanmiguel.utils.ConnectionDialogFragment
import pe.edu.idat.clinicasanmiguel.utils.LoadingController
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

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

    private lateinit var cacheManager:
            CacheManager

    private lateinit var loadingController:
            LoadingController

    private var cargando =
        false

    private var mostrandoCache =
        true

    private var llamadaPerfil:
            Call<UsuarioLoginApi>? = null

    private var timeoutPerfilJob:
            Job? = null

    private var idSolicitudPerfil =
        0L

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        cacheManager =
            CacheManager(
                requireContext()
            )

        loadingController =
            LoadingController(
                fragmentManager =
                    parentFragmentManager,
                coroutineScope =
                    viewLifecycleOwner.lifecycleScope
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

        parentFragmentManager
            .setFragmentResultListener(
                REQUEST_REINTENTAR_PERFIL,
                viewLifecycleOwner
            ) { _, bundle ->

                val reintentar =
                    bundle.getBoolean(
                        ConnectionDialogFragment
                            .RESULTADO_REINTENTAR,
                        false
                    )

                if (reintentar) {
                    cargarPerfilDesdeApi()
                }
            }

        NetworkMonitor
            .estadoConexion
            .observe(
                viewLifecycleOwner
            ) { conectado ->

                if (!conectado) {
                    cancelarCargaEnCurso()

                    mostrarPerfilGuardado(
                        titulo =
                            "Sin conexión a Internet",
                        detalle =
                            "No fue posible actualizar tus datos personales desde la API."
                    )

                    return@observe
                }

                ConnectionDialogFragment.ocultar(
                    parentFragmentManager
                )

                if (
                    mostrandoCache &&
                    !cargando
                ) {
                    cargarPerfilDesdeApi()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        cargarPerfilDesdeApi()
    }

    override fun onDestroyView() {
        cancelarCargaEnCurso()

        super.onDestroyView()
    }

    private fun cargarPerfilDesdeApi() {
        if (
            cargando ||
            !vistaDisponible()
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarPerfilGuardado(
                titulo =
                    "Sin conexión a Internet",
                detalle =
                    "No fue posible actualizar tus datos personales desde la API."
            )

            return
        }

        cargando =
            true

        val tokenCarga =
            loadingController.show(
                message =
                    "Consultando tu perfil..."
            )

        val solicitudId =
            ++idSolicitudPerfil

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService.obtenerPerfil()

        llamadaPerfil =
            llamada

        programarTiempoMaximo(
            solicitudId = solicitudId,
            tokenCarga = tokenCarga,
            llamada = llamada
        )

        llamada.enqueue(
            object :
                Callback<UsuarioLoginApi> {

                override fun onResponse(
                    call: Call<UsuarioLoginApi>,
                    response: Response<UsuarioLoginApi>
                ) {
                    if (!solicitudVigente(solicitudId)) {
                        return
                    }

                    finalizarSolicitud(
                        solicitudId = solicitudId,
                        tokenCarga = tokenCarga
                    ) callback@{

                        if (!vistaDisponible()) {
                            return@callback
                        }

                        if (response.isSuccessful) {
                            val usuario =
                                response.body()

                            if (usuario == null) {
                                mostrarPerfilGuardado(
                                    titulo =
                                        "Respuesta incompleta",
                                    detalle =
                                        "La API no devolvió los datos completos del perfil."
                                )

                                return@callback
                            }

                            cacheManager.guardarObjeto(
                                CacheManager.PERFIL_USUARIO,
                                usuario
                            )

                            mostrandoCache =
                                false

                            mostrarPerfil(
                                usuario
                            )

                            ConnectionDialogFragment.ocultar(
                                parentFragmentManager
                            )

                            return@callback
                        }

                        val mensaje =
                            obtenerMensajeError(
                                response
                            )

                        when (response.code()) {
                            401 -> {
                                mostrarMensajeYCerrarSesion(
                                    mensaje
                                        ?: "Tu sesión ha vencido"
                                )
                            }

                            403 -> {
                                mostrarErrorPerfil()

                                Toast.makeText(
                                    requireContext(),
                                    mensaje
                                        ?: "No tienes permiso para consultar este perfil",
                                    Toast.LENGTH_LONG
                                ).show()
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

                            in 400..499 -> {
                                mostrarErrorPerfil()

                                Toast.makeText(
                                    requireContext(),
                                    mensaje
                                        ?: "No se pudo consultar el perfil. Código ${response.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            else -> {
                                mostrarPerfilGuardado(
                                    titulo =
                                        "Servicio no disponible",
                                    detalle =
                                        mensaje
                                            ?: "La API respondió con el código ${response.code()} y no se pudo actualizar el perfil."
                                )
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<UsuarioLoginApi>,
                    throwable: Throwable
                ) {
                    if (!solicitudVigente(solicitudId)) {
                        return
                    }

                    finalizarSolicitud(
                        solicitudId = solicitudId,
                        tokenCarga = tokenCarga
                    ) callback@{

                        if (!vistaDisponible()) {
                            return@callback
                        }

                        val sinConexion =
                            throwable is IOException ||
                                    !NetworkMonitor.hayInternet()

                        if (sinConexion) {
                            mostrarPerfilGuardado(
                                titulo =
                                    "Sin conexión a Internet",
                                detalle =
                                    "No fue posible comunicarse con la API. " +
                                            "La red puede estar desconectada, sin megas o sin acceso real a Internet."
                            )
                        } else {
                            mostrarPerfilGuardado(
                                titulo =
                                    "No se pudo actualizar el perfil",
                                detalle =
                                    "Ocurrió un error al consultar la API."
                            )
                        }
                    }
                }
            }
        )
    }

    private fun programarTiempoMaximo(
        solicitudId: Long,
        tokenCarga: Long,
        llamada: Call<UsuarioLoginApi>
    ) {
        timeoutPerfilJob?.cancel()

        timeoutPerfilJob =
            viewLifecycleOwner.lifecycleScope.launch {
                delay(
                    TIEMPO_MAXIMO_API_MS
                )

                if (
                    !solicitudVigente(
                        solicitudId
                    ) ||
                    !vistaDisponible()
                ) {
                    return@launch
                }

                idSolicitudPerfil++

                timeoutPerfilJob =
                    null

                llamadaPerfil =
                    null

                cargando =
                    false

                llamada.cancel()

                loadingController.hide(
                    requestToken = tokenCarga
                ) callback@{

                    if (!vistaDisponible()) {
                        return@callback
                    }

                    mostrarPerfilGuardado(
                        titulo =
                            "Tiempo de espera agotado",
                        detalle =
                            "La API no respondió dentro de 30 segundos."
                    )
                }
            }
    }

    private fun finalizarSolicitud(
        solicitudId: Long,
        tokenCarga: Long,
        despuesDeCerrar: () -> Unit
    ) {
        if (!solicitudVigente(solicitudId)) {
            return
        }

        idSolicitudPerfil++

        timeoutPerfilJob?.cancel()
        timeoutPerfilJob =
            null

        llamadaPerfil =
            null

        cargando =
            false

        loadingController.hide(
            requestToken = tokenCarga
        ) callback@{

            if (!vistaDisponible()) {
                return@callback
            }

            despuesDeCerrar()
        }
    }

    private fun cancelarCargaEnCurso() {
        idSolicitudPerfil++

        timeoutPerfilJob?.cancel()
        timeoutPerfilJob =
            null

        llamadaPerfil?.cancel()
        llamadaPerfil =
            null

        cargando =
            false

        if (::loadingController.isInitialized) {
            loadingController.forceHide()
        }
    }

    private fun solicitudVigente(
        solicitudId: Long
    ): Boolean {
        return solicitudId ==
                idSolicitudPerfil
    }

    private fun mostrarPerfilGuardado(
        titulo: String,
        detalle: String
    ) {
        if (!vistaDisponible()) {
            return
        }

        mostrandoCache =
            true

        val perfilGuardado =
            cacheManager.obtenerObjeto(
                CacheManager.PERFIL_USUARIO,
                UsuarioLoginApi::class.java
            )

        if (perfilGuardado == null) {
            mostrarErrorPerfil()
        } else {
            mostrarPerfil(
                perfilGuardado
            )
        }

        val informacionCache =
            if (perfilGuardado == null) {
                "No existe un perfil guardado en este dispositivo."
            } else {
                "Se están mostrando los últimos datos personales guardados. " +
                        "Esta información podría estar desactualizada."
            }

        ConnectionDialogFragment.mostrar(
            fragmentManager =
                parentFragmentManager,
            titulo =
                titulo,
            mensaje =
                "$detalle\n\n$informacionCache",
            requestKey =
                REQUEST_REINTENTAR_PERFIL,
            permitirReintento =
                true
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
                JSONObject(
                    contenido
                )
                    .optString(
                        "mensaje"
                    )
                    .takeIf {
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

    private fun mostrarMensajeYCerrarSesion(
        mensaje: String
    ) {
        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_LONG
        ).show()

        cerrarSesion()
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

    private fun vistaDisponible(): Boolean {
        return isAdded &&
                view != null
    }

    companion object {
        private const val REQUEST_REINTENTAR_PERFIL =
            "REQUEST_REINTENTAR_PERFIL"

        private const val TIEMPO_MAXIMO_API_MS =
            30_000L
    }
}