package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.NotificacionesAdapter
import pe.edu.idat.clinicasanmiguel.network.NotificacionApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.utils.CacheManager
import pe.edu.idat.clinicasanmiguel.utils.ConnectionDialogFragment
import pe.edu.idat.clinicasanmiguel.utils.LoadingController
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class NotificacionesFragment :
    Fragment(R.layout.activity_notificaciones) {

    private lateinit var rvNotificaciones:
            RecyclerView

    private lateinit var tvEstado:
            TextView

    private lateinit var adapter:
            NotificacionesAdapter

    private lateinit var cacheManager:
            CacheManager

    private lateinit var loadingController:
            LoadingController

    private val listaNotificaciones =
        mutableListOf<NotificacionApiResponse>()

    private var cargando =
        false

    private var mostrandoCache =
        true

    private var llamadaNotificaciones:
            Call<List<NotificacionApiResponse>>? = null

    private var timeoutNotificacionesJob:
            Job? = null

    private var idSolicitudNotificaciones =
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

        rvNotificaciones =
            view.findViewById(
                R.id.rvNotificaciones
            )

        tvEstado =
            view.findViewById(
                R.id.tvEstadoNotificaciones
            )

        rvNotificaciones.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        adapter =
            NotificacionesAdapter(
                listaNotificaciones
            )

        rvNotificaciones.adapter =
            adapter

        mostrarEstado(
            "Cargando notificaciones..."
        )

        parentFragmentManager
            .setFragmentResultListener(
                REQUEST_REINTENTAR_NOTIFICACIONES,
                viewLifecycleOwner
            ) { _, bundle ->

                val reintentar =
                    bundle.getBoolean(
                        ConnectionDialogFragment
                            .RESULTADO_REINTENTAR,
                        false
                    )

                if (reintentar) {
                    cargarNotificacionesDesdeApi()
                }
            }

        NetworkMonitor
            .estadoConexion
            .observe(
                viewLifecycleOwner
            ) { conectado ->

                if (!conectado) {
                    cancelarCargaEnCurso()

                    mostrarNotificacionesGuardadas(
                        titulo =
                            "Sin conexión a Internet",
                        detalle =
                            "No fue posible actualizar tus notificaciones desde la API."
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
                    cargarNotificacionesDesdeApi()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        cargarNotificacionesDesdeApi()
    }

    override fun onDestroyView() {
        cancelarCargaEnCurso()

        super.onDestroyView()
    }

    private fun cargarNotificacionesDesdeApi() {
        if (
            cargando ||
            !vistaDisponible()
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarNotificacionesGuardadas(
                titulo =
                    "Sin conexión a Internet",
                detalle =
                    "No fue posible actualizar tus notificaciones desde la API."
            )

            return
        }

        cargando =
            true

        val tokenCarga =
            loadingController.show(
                message =
                    "Consultando tus notificaciones..."
            )

        val solicitudId =
            ++idSolicitudNotificaciones

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService.listarMisNotificaciones()

        llamadaNotificaciones =
            llamada

        programarTiempoMaximo(
            solicitudId = solicitudId,
            tokenCarga = tokenCarga,
            llamada = llamada
        )

        llamada.enqueue(
            object :
                Callback<List<NotificacionApiResponse>> {

                override fun onResponse(
                    call: Call<List<NotificacionApiResponse>>,
                    response: Response<List<NotificacionApiResponse>>
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
                            val notificaciones =
                                response.body()
                                    ?: emptyList()

                            cacheManager.guardarLista(
                                CacheManager.NOTIFICACIONES,
                                notificaciones
                            )

                            mostrandoCache =
                                false

                            mostrarNotificaciones(
                                notificaciones,
                                mensajeVacio =
                                    "Aún no tienes notificaciones"
                            )

                            ConnectionDialogFragment.ocultar(
                                parentFragmentManager
                            )

                            return@callback
                        }

                        if (response.code() == 401) {
                            cerrarSesion(
                                obtenerMensajeError(
                                    response
                                ) ?: "Tu sesión ha vencido"
                            )

                            return@callback
                        }

                        val mensajeServidor =
                            obtenerMensajeError(
                                response
                            )

                        mostrarNotificacionesGuardadas(
                            titulo =
                                if (response.code() == 403) {
                                    "Acceso no permitido"
                                } else {
                                    "Servicio no disponible"
                                },
                            detalle =
                                mensajeServidor
                                    ?: "La API respondió con el código ${response.code()} y no se pudieron actualizar las notificaciones."
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<NotificacionApiResponse>>,
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
                            mostrarNotificacionesGuardadas(
                                titulo =
                                    "Sin conexión a Internet",
                                detalle =
                                    "No fue posible comunicarse con la API. " +
                                            "La red puede estar desconectada, sin megas o sin acceso real a Internet."
                            )
                        } else {
                            mostrarNotificacionesGuardadas(
                                titulo =
                                    "No se pudieron actualizar las notificaciones",
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
        llamada: Call<List<NotificacionApiResponse>>
    ) {
        timeoutNotificacionesJob?.cancel()

        timeoutNotificacionesJob =
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

                idSolicitudNotificaciones++

                timeoutNotificacionesJob =
                    null

                llamadaNotificaciones =
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

                    mostrarNotificacionesGuardadas(
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

        idSolicitudNotificaciones++

        timeoutNotificacionesJob?.cancel()
        timeoutNotificacionesJob =
            null

        llamadaNotificaciones =
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
        idSolicitudNotificaciones++

        timeoutNotificacionesJob?.cancel()
        timeoutNotificacionesJob =
            null

        llamadaNotificaciones?.cancel()
        llamadaNotificaciones =
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
                idSolicitudNotificaciones
    }

    private fun mostrarNotificacionesGuardadas(
        titulo: String,
        detalle: String
    ) {
        if (!vistaDisponible()) {
            return
        }

        mostrandoCache =
            true

        val notificacionesGuardadas =
            cacheManager.obtenerLista(
                CacheManager.NOTIFICACIONES,
                NotificacionApiResponse::class.java
            )

        val mensajeVacio =
            if (notificacionesGuardadas == null) {
                "No hay notificaciones guardadas en este dispositivo"
            } else {
                "Aún no tienes notificaciones"
            }

        mostrarNotificaciones(
            notificacionesGuardadas
                ?: emptyList(),
            mensajeVacio =
                mensajeVacio
        )

        val informacionCache =
            when {
                notificacionesGuardadas == null -> {
                    "No existe un listado de notificaciones guardado en este dispositivo."
                }

                notificacionesGuardadas.isEmpty() -> {
                    "El último listado guardado no contiene notificaciones. " +
                            "Esta información podría estar desactualizada."
                }

                else -> {
                    "Se están mostrando las últimas notificaciones guardadas. " +
                            "Esta información podría estar desactualizada."
                }
            }

        ConnectionDialogFragment.mostrar(
            fragmentManager =
                parentFragmentManager,
            titulo =
                titulo,
            mensaje =
                "$detalle\n\n$informacionCache",
            requestKey =
                REQUEST_REINTENTAR_NOTIFICACIONES,
            permitirReintento =
                true
        )
    }

    private fun mostrarNotificaciones(
        notificaciones: List<NotificacionApiResponse>,
        mensajeVacio: String
    ) {
        listaNotificaciones.clear()

        listaNotificaciones.addAll(
            notificaciones
        )

        adapter.notifyDataSetChanged()

        if (listaNotificaciones.isEmpty()) {
            mostrarEstado(
                mensajeVacio
            )
        } else {
            mostrarLista()
        }
    }

    private fun mostrarLista() {
        tvEstado.visibility =
            View.GONE

        rvNotificaciones.visibility =
            View.VISIBLE
    }

    private fun mostrarEstado(
        mensaje: String
    ) {
        tvEstado.text =
            mensaje

        tvEstado.visibility =
            View.VISIBLE

        rvNotificaciones.visibility =
            View.GONE
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
                JSONObject(contenido)
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

        startActivity(intent)

        requireActivity().finish()
    }

    private fun vistaDisponible(): Boolean {
        return isAdded &&
                view != null
    }

    companion object {
        private const val REQUEST_REINTENTAR_NOTIFICACIONES =
            "REQUEST_REINTENTAR_NOTIFICACIONES"

        private const val TIEMPO_MAXIMO_API_MS =
            30_000L
    }
}