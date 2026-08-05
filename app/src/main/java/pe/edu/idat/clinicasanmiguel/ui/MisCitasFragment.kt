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
import pe.edu.idat.clinicasanmiguel.ReprogramarCitaActivity
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.network.CancelarCitaApiResponse
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

class MisCitasFragment :
    Fragment(R.layout.activity_mis_citas) {

    private lateinit var rvMisCitas:
            RecyclerView

    private lateinit var adapter:
            CitasAdapter

    private lateinit var cacheManager:
            CacheManager

    private lateinit var loadingController:
            LoadingController

    private val listaCitas =
        mutableListOf<CitaApiResponse>()

    private var cancelandoCita =
        false

    private var cargandoCitas =
        false

    private var mostrandoCache =
        true

    private var llamadaCitas:
            Call<List<CitaApiResponse>>? = null

    private var timeoutCitasJob:
            Job? = null

    private var loadingTokenCitas:
            Long? = null

    private var idSolicitudCitas =
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

        rvMisCitas =
            view.findViewById(
                R.id.rvMisCitas
            )

        rvMisCitas.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        adapter =
            CitasAdapter(
                lista = listaCitas,
                esHistorial = false,
                onCancelarCita = { cita ->
                    cancelarCitaDesdeApi(
                        cita.id
                    )
                },
                onReprogramarCita = { cita ->
                    abrirReprogramacion(
                        cita
                    )
                }
            )

        rvMisCitas.adapter =
            adapter

        adapter.actualizarAccionesHabilitadas(
            false
        )

        parentFragmentManager
            .setFragmentResultListener(
                REQUEST_REINTENTAR_CITAS,
                viewLifecycleOwner
            ) { _, bundle ->

                val reintentar =
                    bundle.getBoolean(
                        ConnectionDialogFragment
                            .RESULTADO_REINTENTAR,
                        false
                    )

                if (reintentar) {
                    cargarCitasDesdeApi()
                }
            }

        NetworkMonitor
            .estadoConexion
            .observe(
                viewLifecycleOwner
            ) { conectado ->

                if (!conectado) {
                    cancelarCargaCitasEnCurso()

                    mostrarCitasGuardadas(
                        titulo =
                            "Sin conexión a Internet",
                        detalle =
                            "No fue posible actualizar tus citas desde la API."
                    )

                    return@observe
                }

                ConnectionDialogFragment.ocultar(
                    parentFragmentManager
                )

                if (
                    mostrandoCache &&
                    !cargandoCitas
                ) {
                    cargarCitasDesdeApi()
                } else {
                    actualizarEstadoAcciones()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        cargarCitasDesdeApi()
    }

    override fun onDestroyView() {
        cancelarCargaCitasEnCurso()

        super.onDestroyView()
    }

    private fun cargarCitasDesdeApi() {
        if (
            cargandoCitas ||
            !vistaDisponible()
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarCitasGuardadas(
                titulo =
                    "Sin conexión a Internet",
                detalle =
                    "No fue posible actualizar tus citas desde la API."
            )

            return
        }

        cargandoCitas =
            true

        actualizarEstadoAcciones()

        val tokenCarga =
            loadingController.show(
                message =
                    "Consultando tus citas..."
            )

        loadingTokenCitas =
            tokenCarga

        val solicitudId =
            ++idSolicitudCitas

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService.listarCitasActivas()

        llamadaCitas =
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

                    finalizarSolicitudCitas(
                        solicitudId = solicitudId,
                        tokenCarga = tokenCarga
                    ) callback@{

                        if (!vistaDisponible()) {
                            return@callback
                        }

                        if (response.isSuccessful) {
                            val citasApi =
                                response.body()
                                    ?: emptyList()

                            cacheManager.guardarLista(
                                CacheManager.CITAS_ACTIVAS,
                                citasApi
                            )

                            mostrandoCache =
                                false

                            mostrarCitas(
                                citasApi
                            )

                            ConnectionDialogFragment.ocultar(
                                parentFragmentManager
                            )

                            actualizarEstadoAcciones()

                            if (listaCitas.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No tienes citas activas",
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

                        mostrarCitasGuardadas(
                            titulo =
                                "Servicio no disponible",
                            detalle =
                                mensajeServidor
                                    ?: "La API respondió con el código ${response.code()} y no se pudo actualizar el listado."
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

                    finalizarSolicitudCitas(
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
                            mostrarCitasGuardadas(
                                titulo =
                                    "Sin conexión a Internet",
                                detalle =
                                    "No fue posible comunicarse con la API. " +
                                            "La red puede estar desconectada, sin megas o sin acceso real a Internet."
                            )
                        } else {
                            mostrarCitasGuardadas(
                                titulo =
                                    "No se pudieron actualizar las citas",
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
        timeoutCitasJob?.cancel()

        timeoutCitasJob =
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

                idSolicitudCitas++

                timeoutCitasJob =
                    null

                llamadaCitas =
                    null

                cargandoCitas =
                    false

                loadingTokenCitas =
                    null

                llamada.cancel()

                loadingController.hide(
                    requestToken = tokenCarga
                ) callback@{

                    if (!vistaDisponible()) {
                        return@callback
                    }

                    mostrarCitasGuardadas(
                        titulo =
                            "Tiempo de espera agotado",
                        detalle =
                            "La API no respondió dentro de 30 segundos."
                    )
                }
            }
    }

    private fun finalizarSolicitudCitas(
        solicitudId: Long,
        tokenCarga: Long,
        despuesDeCerrar: () -> Unit
    ) {
        if (!solicitudVigente(solicitudId)) {
            return
        }

        idSolicitudCitas++

        timeoutCitasJob?.cancel()
        timeoutCitasJob =
            null

        llamadaCitas =
            null

        cargandoCitas =
            false

        loadingTokenCitas =
            null

        loadingController.hide(
            requestToken = tokenCarga
        ) callback@{

            if (!vistaDisponible()) {
                return@callback
            }

            despuesDeCerrar()
        }
    }

    private fun cancelarCargaCitasEnCurso() {
        idSolicitudCitas++

        timeoutCitasJob?.cancel()
        timeoutCitasJob =
            null

        llamadaCitas?.cancel()
        llamadaCitas =
            null

        cargandoCitas =
            false

        loadingTokenCitas =
            null

        if (::loadingController.isInitialized) {
            loadingController.forceHide()
        }

        if (::adapter.isInitialized) {
            actualizarEstadoAcciones()
        }
    }

    private fun solicitudVigente(
        solicitudId: Long
    ): Boolean {
        return solicitudId == idSolicitudCitas
    }

    private fun mostrarCitasGuardadas(
        titulo: String,
        detalle: String
    ) {
        if (!vistaDisponible()) {
            return
        }

        mostrandoCache =
            true

        val citasGuardadas =
            cacheManager.obtenerLista(
                CacheManager.CITAS_ACTIVAS,
                CitaApiResponse::class.java
            )

        mostrarCitas(
            citasGuardadas
                ?: emptyList()
        )

        actualizarEstadoAcciones()

        val informacionCache =
            when {
                citasGuardadas == null -> {
                    "No existe un listado guardado de tus citas en este dispositivo."
                }

                citasGuardadas.isEmpty() -> {
                    "El último listado guardado indica que no tienes citas activas. " +
                            "Esta información podría estar desactualizada."
                }

                else -> {
                    "Se está mostrando el último listado guardado de tus citas. " +
                            "Esta información podría estar desactualizada. " +
                            "Cancelar y reprogramar permanecerán bloqueados."
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
                REQUEST_REINTENTAR_CITAS,
            permitirReintento =
                true
        )
    }

    private fun mostrarCitas(
        citas: List<CitaApiResponse>
    ) {
        listaCitas.clear()

        listaCitas.addAll(
            citas
        )

        adapter.notifyDataSetChanged()
    }

    private fun actualizarEstadoAcciones() {
        if (!::adapter.isInitialized) {
            return
        }

        val accionesDisponibles =
            NetworkMonitor.hayInternet() &&
                    !mostrandoCache &&
                    !cargandoCitas &&
                    !cancelandoCita

        adapter.actualizarAccionesHabilitadas(
            accionesDisponibles
        )
    }

    private fun abrirReprogramacion(
        cita: CitaApiResponse
    ) {
        if (
            !NetworkMonitor.hayInternet() ||
            mostrandoCache ||
            cargandoCitas
        ) {
            mostrarOperacionRequiereInternet()
            return
        }

        if (
            cita.id <= 0 ||
            cita.idMedico <= 0
        ) {
            Toast.makeText(
                requireContext(),
                "No se pudo identificar la cita o el médico",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val intent =
            Intent(
                requireContext(),
                ReprogramarCitaActivity::class.java
            ).apply {
                putExtra(
                    "id_cita",
                    cita.id
                )

                putExtra(
                    "id_medico",
                    cita.idMedico
                )

                putExtra(
                    "especialidad",
                    cita.especialidad
                )

                putExtra(
                    "medico",
                    cita.medico
                )

                putExtra(
                    "fecha_hora",
                    cita.fechaHora
                )
            }

        startActivity(intent)
    }

    private fun cancelarCitaDesdeApi(
        idCita: Int
    ) {
        if (
            !NetworkMonitor.hayInternet() ||
            mostrandoCache ||
            cargandoCitas
        ) {
            mostrarOperacionRequiereInternet()
            return
        }

        if (
            cancelandoCita ||
            idCita <= 0
        ) {
            return
        }

        cancelandoCita =
            true

        actualizarEstadoAcciones()

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        apiService
            .cancelarCita(idCita)
            .enqueue(
                object :
                    Callback<CancelarCitaApiResponse> {

                    override fun onResponse(
                        call: Call<CancelarCitaApiResponse>,
                        response: Response<CancelarCitaApiResponse>
                    ) {
                        cancelandoCita =
                            false

                        if (!vistaDisponible()) {
                            return
                        }

                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (
                                respuesta != null &&
                                !respuesta.exito
                            ) {
                                actualizarEstadoAcciones()

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
                                    ?: "Cita cancelada correctamente",
                                Toast.LENGTH_LONG
                            ).show()

                            cargarCitasDesdeApi()
                            return
                        }

                        actualizarEstadoAcciones()

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<CancelarCitaApiResponse>,
                        throwable: Throwable
                    ) {
                        cancelandoCita =
                            false

                        if (!vistaDisponible()) {
                            return
                        }

                        actualizarEstadoAcciones()

                        if (
                            throwable is IOException ||
                            !NetworkMonitor.hayInternet()
                        ) {
                            mostrandoCache =
                                true

                            actualizarEstadoAcciones()

                            mostrarOperacionRequiereInternet()
                            return
                        }

                        Toast.makeText(
                            requireContext(),
                            "No se pudo cancelar la cita",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun mostrarOperacionRequiereInternet() {
        ConnectionDialogFragment.mostrar(
            fragmentManager =
                parentFragmentManager,
            titulo =
                "Conexión requerida",
            mensaje =
                "Esta operación necesita conexión a Internet. " +
                        "No se guardará ninguna acción pendiente para después.",
            permitirReintento =
                false
        )
    }

    private fun procesarErrorRespuesta(
        response: Response<*>
    ) {
        if (!vistaDisponible()) {
            return
        }

        val mensaje =
            obtenerMensajeError(
                response
            )

        when (response.code()) {
            400 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "La cita no puede cancelarse",
                    Toast.LENGTH_LONG
                ).show()
            }

            401 -> {
                cerrarSesion(
                    mensaje
                        ?: "Tu sesión ha vencido"
                )
            }

            403 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "No tienes permiso para realizar esta operación",
                    Toast.LENGTH_LONG
                ).show()
            }

            404 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "La cita no fue encontrada",
                    Toast.LENGTH_LONG
                ).show()
            }

            409 -> {
                Toast.makeText(
                    requireContext(),
                    mensaje
                        ?: "La cita ya cambió de estado",
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
        private const val REQUEST_REINTENTAR_CITAS =
            "REQUEST_REINTENTAR_CITAS"

        private const val TIEMPO_MAXIMO_API_MS =
            30_000L
    }
}