package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
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

class HistorialCompletoFragment :
    Fragment(R.layout.activity_historial_completo) {

    private lateinit var rvHistorial:
            RecyclerView

    private lateinit var adapter:
            CitasAdapter

    private lateinit var cacheManager:
            CacheManager

    private lateinit var loadingController:
            LoadingController

    private val listaHistorial =
        mutableListOf<CitaApiResponse>()

    private var cargandoHistorial =
        false

    private var mostrandoCache =
        true

    private var llamadaHistorial:
            Call<List<CitaApiResponse>>? = null

    private var timeoutHistorialJob:
            Job? = null

    private var idSolicitudHistorial =
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

        rvHistorial =
            view.findViewById(
                R.id.rvHistorialCompleto
            )

        rvHistorial.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        adapter =
            CitasAdapter(
                lista = listaHistorial,
                esHistorial = true
            )

        rvHistorial.adapter =
            adapter

        parentFragmentManager
            .setFragmentResultListener(
                REQUEST_REINTENTAR_HISTORIAL,
                viewLifecycleOwner
            ) { _, bundle ->

                val reintentar =
                    bundle.getBoolean(
                        ConnectionDialogFragment
                            .RESULTADO_REINTENTAR,
                        false
                    )

                if (reintentar) {
                    cargarHistorialDesdeApi()
                }
            }

        NetworkMonitor
            .estadoConexion
            .observe(
                viewLifecycleOwner
            ) { conectado ->

                if (!conectado) {
                    cancelarCargaHistorialEnCurso()

                    mostrarHistorialGuardado(
                        titulo =
                            "Sin conexión a Internet",
                        detalle =
                            "No fue posible actualizar tu historial desde la API."
                    )

                    return@observe
                }

                ConnectionDialogFragment.ocultar(
                    parentFragmentManager
                )

                if (
                    mostrandoCache &&
                    !cargandoHistorial
                ) {
                    cargarHistorialDesdeApi()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        cargarHistorialDesdeApi()
    }

    override fun onDestroyView() {
        cancelarCargaHistorialEnCurso()

        super.onDestroyView()
    }

    private fun cargarHistorialDesdeApi() {
        if (
            cargandoHistorial ||
            !vistaDisponible()
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarHistorialGuardado(
                titulo =
                    "Sin conexión a Internet",
                detalle =
                    "No fue posible actualizar tu historial desde la API."
            )

            return
        }

        cargandoHistorial =
            true

        val tokenCarga =
            loadingController.show(
                message =
                    "Consultando tu historial..."
            )

        val solicitudId =
            ++idSolicitudHistorial

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService.listarHistorialCitas()

        llamadaHistorial =
            llamada

        programarTiempoMaximo(
            solicitudId = solicitudId,
            tokenCarga = tokenCarga,
            llamada = llamada
        )

        llamada.enqueue(
            object :
                Callback<List<CitaApiResponse>> {

                override fun onResponse(
                    call: Call<List<CitaApiResponse>>,
                    response: Response<List<CitaApiResponse>>
                ) {
                    if (!solicitudVigente(solicitudId)) {
                        return
                    }

                    finalizarSolicitudHistorial(
                        solicitudId = solicitudId,
                        tokenCarga = tokenCarga
                    ) callback@{

                        if (!vistaDisponible()) {
                            return@callback
                        }

                        if (response.isSuccessful) {
                            val historialApi =
                                response.body()
                                    ?: emptyList()

                            cacheManager.guardarLista(
                                CacheManager.HISTORIAL_CITAS,
                                historialApi
                            )

                            mostrandoCache =
                                false

                            mostrarHistorial(
                                historialApi
                            )

                            ConnectionDialogFragment.ocultar(
                                parentFragmentManager
                            )

                            if (listaHistorial.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No tienes citas en el historial",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

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

                        mostrarHistorialGuardado(
                            titulo =
                                "Servicio no disponible",
                            detalle =
                                mensajeServidor
                                    ?: "La API respondió con el código ${response.code()} y no se pudo actualizar el historial."
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<CitaApiResponse>>,
                    throwable: Throwable
                ) {
                    if (!solicitudVigente(solicitudId)) {
                        return
                    }

                    finalizarSolicitudHistorial(
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
                            mostrarHistorialGuardado(
                                titulo =
                                    "Sin conexión a Internet",
                                detalle =
                                    "No fue posible comunicarse con la API. " +
                                            "La red puede estar desconectada, sin megas o sin acceso real a Internet."
                            )
                        } else {
                            mostrarHistorialGuardado(
                                titulo =
                                    "No se pudo actualizar el historial",
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
        llamada: Call<List<CitaApiResponse>>
    ) {
        timeoutHistorialJob?.cancel()

        timeoutHistorialJob =
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

                idSolicitudHistorial++

                timeoutHistorialJob =
                    null

                llamadaHistorial =
                    null

                cargandoHistorial =
                    false

                llamada.cancel()

                loadingController.hide(
                    requestToken = tokenCarga
                ) callback@{

                    if (!vistaDisponible()) {
                        return@callback
                    }

                    mostrarHistorialGuardado(
                        titulo =
                            "Tiempo de espera agotado",
                        detalle =
                            "La API no respondió dentro de 30 segundos."
                    )
                }
            }
    }

    private fun finalizarSolicitudHistorial(
        solicitudId: Long,
        tokenCarga: Long,
        despuesDeCerrar: () -> Unit
    ) {
        if (!solicitudVigente(solicitudId)) {
            return
        }

        idSolicitudHistorial++

        timeoutHistorialJob?.cancel()
        timeoutHistorialJob =
            null

        llamadaHistorial =
            null

        cargandoHistorial =
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

    private fun cancelarCargaHistorialEnCurso() {
        idSolicitudHistorial++

        timeoutHistorialJob?.cancel()
        timeoutHistorialJob =
            null

        llamadaHistorial?.cancel()
        llamadaHistorial =
            null

        cargandoHistorial =
            false

        if (::loadingController.isInitialized) {
            loadingController.forceHide()
        }
    }

    private fun solicitudVigente(
        solicitudId: Long
    ): Boolean {
        return solicitudId ==
                idSolicitudHistorial
    }

    private fun mostrarHistorialGuardado(
        titulo: String,
        detalle: String
    ) {
        if (!vistaDisponible()) {
            return
        }

        mostrandoCache =
            true

        val historialGuardado =
            cacheManager.obtenerLista(
                CacheManager.HISTORIAL_CITAS,
                CitaApiResponse::class.java
            )

        mostrarHistorial(
            historialGuardado
                ?: emptyList()
        )

        val informacionCache =
            when {
                historialGuardado == null -> {
                    "No existe un historial guardado en este dispositivo."
                }

                historialGuardado.isEmpty() -> {
                    "El último historial guardado no contiene citas. " +
                            "Esta información podría estar desactualizada."
                }

                else -> {
                    "Se está mostrando el último historial guardado. " +
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
                REQUEST_REINTENTAR_HISTORIAL,
            permitirReintento =
                true
        )
    }

    private fun mostrarHistorial(
        historial: List<CitaApiResponse>
    ) {
        listaHistorial.clear()

        listaHistorial.addAll(
            historial
        )

        adapter.notifyDataSetChanged()
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
        private const val REQUEST_REINTENTAR_HISTORIAL =
            "REQUEST_REINTENTAR_HISTORIAL"

        private const val TIEMPO_MAXIMO_API_MS =
            30_000L
    }
}