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
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.RecetasAdapter
import pe.edu.idat.clinicasanmiguel.network.RecetaApiResponse
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.RecetaRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaRecetasApi
import pe.edu.idat.clinicasanmiguel.utils.ConnectionDialogFragment
import pe.edu.idat.clinicasanmiguel.utils.LoadingController
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor
import retrofit2.Call

class RecetasFragment :
    Fragment(R.layout.activity_recetas) {

    private lateinit var rvRecetas:
            RecyclerView

    private lateinit var tvEstado:
            TextView

    private lateinit var adapter:
            RecetasAdapter

    private lateinit var recetaRepository:
            RecetaRepository

    private lateinit var loadingController:
            LoadingController

    private val listaRecetas =
        mutableListOf<RecetaApiResponse>()

    private var llamadaRecetas:
            Call<List<RecetaApiResponse>>? = null

    private var timeoutRecetasJob:
            Job? = null

    private var cargandoRecetas =
        false

    private var mostrandoCache =
        true

    private var idSolicitudRecetas =
        0L

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        recetaRepository =
            RecetaRepository(
                requireContext()
            )

        loadingController =
            LoadingController(
                fragmentManager =
                    parentFragmentManager,
                coroutineScope =
                    viewLifecycleOwner.lifecycleScope
            )

        rvRecetas =
            view.findViewById(
                R.id.rvRecetas
            )

        tvEstado =
            view.findViewById(
                R.id.tvEstadoRecetas
            )

        rvRecetas.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        adapter =
            RecetasAdapter(
                lista =
                    listaRecetas,
                onSeleccionarReceta = {
                        receta ->

                    mostrarDetalleReceta(
                        receta
                    )
                }
            )

        rvRecetas.adapter =
            adapter

        mostrarEstado(
            "Cargando recetas médicas..."
        )

        configurarReintento()
        observarConexion()
    }

    private fun mostrarDetalleReceta(
        receta: RecetaApiResponse
    ) {
        if (!vistaDisponible()) {
            return
        }

        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(
                R.id.flContenedor,
                DetalleRecetaFragment
                    .nuevaInstancia(
                        receta
                    )
            )
            .addToBackStack(
                "detalle_receta"
            )
            .commit()
    }

    override fun onResume() {
        super.onResume()

        cargarRecetasDesdeApi()
    }

    override fun onDestroyView() {
        cancelarCargaEnCurso()

        super.onDestroyView()
    }

    private fun configurarReintento() {
        parentFragmentManager
            .setFragmentResultListener(
                REQUEST_REINTENTAR_RECETAS,
                viewLifecycleOwner
            ) { _, bundle ->

                val reintentar =
                    bundle.getBoolean(
                        ConnectionDialogFragment
                            .RESULTADO_REINTENTAR,
                        false
                    )

                if (reintentar) {
                    cargarRecetasDesdeApi()
                }
            }
    }

    private fun observarConexion() {
        NetworkMonitor
            .estadoConexion
            .observe(
                viewLifecycleOwner
            ) { conectado ->

                if (!conectado) {
                    cancelarCargaEnCurso()

                    mostrarRecetasGuardadas(
                        titulo =
                            "Sin conexión a Internet",
                        detalle =
                            "No fue posible actualizar tus recetas médicas desde la API."
                    )

                    return@observe
                }

                ConnectionDialogFragment.ocultar(
                    parentFragmentManager
                )

                if (
                    mostrandoCache &&
                    !cargandoRecetas
                ) {
                    cargarRecetasDesdeApi()
                }
            }
    }

    private fun cargarRecetasDesdeApi() {
        if (
            cargandoRecetas ||
            !vistaDisponible()
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarRecetasGuardadas(
                titulo =
                    "Sin conexión a Internet",
                detalle =
                    "No fue posible actualizar tus recetas médicas desde la API."
            )

            return
        }

        cargandoRecetas =
            true

        val tokenCarga =
            loadingController.show(
                message =
                    "Consultando tus recetas médicas..."
            )

        val solicitudId =
            ++idSolicitudRecetas

        val llamada =
            recetaRepository.listarRecetasApi {
                    resultado ->

                if (
                    !solicitudVigente(
                        solicitudId
                    )
                ) {
                    return@listarRecetasApi
                }

                finalizarSolicitud(
                    solicitudId =
                        solicitudId,
                    tokenCarga =
                        tokenCarga
                ) callback@{

                    if (!vistaDisponible()) {
                        return@callback
                    }

                    procesarResultado(
                        resultado
                    )
                }
            }

        llamadaRecetas =
            llamada

        programarTiempoMaximo(
            solicitudId =
                solicitudId,
            tokenCarga =
                tokenCarga,
            llamada =
                llamada
        )
    }

    private fun procesarResultado(
        resultado: ResultadoCargaRecetasApi
    ) {
        when (resultado) {

            is ResultadoCargaRecetasApi.Exito -> {
                mostrandoCache =
                    false

                mostrarRecetas(
                    recetas =
                        resultado.recetas,
                    mensajeVacio =
                        "Aún no tienes recetas médicas registradas"
                )

                ConnectionDialogFragment.ocultar(
                    parentFragmentManager
                )
            }

            is ResultadoCargaRecetasApi.SesionExpirada -> {
                cerrarSesion(
                    resultado.mensaje
                )
            }

            is ResultadoCargaRecetasApi.SinPermiso -> {
                mostrarRecetasGuardadas(
                    titulo =
                        "Acceso no permitido",
                    detalle =
                        resultado.mensaje
                )
            }

            is ResultadoCargaRecetasApi.SinConexion -> {
                mostrarRecetasGuardadas(
                    titulo =
                        "Sin conexión a Internet",
                    detalle =
                        resultado.mensaje
                )
            }

            is ResultadoCargaRecetasApi.Error -> {
                mostrarRecetasGuardadas(
                    titulo =
                        "No se pudieron actualizar las recetas",
                    detalle =
                        resultado.mensaje
                )
            }
        }
    }

    private fun programarTiempoMaximo(
        solicitudId: Long,
        tokenCarga: Long,
        llamada: Call<List<RecetaApiResponse>>
    ) {
        timeoutRecetasJob?.cancel()

        timeoutRecetasJob =
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

                idSolicitudRecetas++

                timeoutRecetasJob =
                    null

                llamadaRecetas =
                    null

                cargandoRecetas =
                    false

                llamada.cancel()

                loadingController.hide(
                    requestToken =
                        tokenCarga
                ) callback@{

                    if (!vistaDisponible()) {
                        return@callback
                    }

                    mostrarRecetasGuardadas(
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
        if (
            !solicitudVigente(
                solicitudId
            )
        ) {
            return
        }

        idSolicitudRecetas++

        timeoutRecetasJob?.cancel()

        timeoutRecetasJob =
            null

        llamadaRecetas =
            null

        cargandoRecetas =
            false

        loadingController.hide(
            requestToken =
                tokenCarga
        ) callback@{

            if (!vistaDisponible()) {
                return@callback
            }

            despuesDeCerrar()
        }
    }

    private fun cancelarCargaEnCurso() {
        idSolicitudRecetas++

        timeoutRecetasJob?.cancel()

        timeoutRecetasJob =
            null

        llamadaRecetas?.cancel()

        llamadaRecetas =
            null

        cargandoRecetas =
            false

        if (
            ::loadingController.isInitialized
        ) {
            loadingController.forceHide()
        }
    }

    private fun solicitudVigente(
        solicitudId: Long
    ): Boolean {
        return solicitudId ==
                idSolicitudRecetas
    }

    private fun mostrarRecetasGuardadas(
        titulo: String,
        detalle: String
    ) {
        if (!vistaDisponible()) {
            return
        }

        mostrandoCache =
            true

        val recetasGuardadas =
            recetaRepository
                .obtenerRecetasGuardadas()

        val mensajeVacio =
            if (recetasGuardadas == null) {
                "No hay recetas guardadas en este dispositivo"
            } else {
                "Aún no tienes recetas médicas registradas"
            }

        mostrarRecetas(
            recetas =
                recetasGuardadas
                    ?: emptyList(),
            mensajeVacio =
                mensajeVacio
        )

        val informacionCache =
            when {
                recetasGuardadas == null -> {
                    "No existe un listado de recetas guardado en este dispositivo."
                }

                recetasGuardadas.isEmpty() -> {
                    "El último listado guardado no contiene recetas médicas. " +
                            "Esta información podría estar desactualizada."
                }

                else -> {
                    "Se están mostrando las últimas recetas guardadas. " +
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
                REQUEST_REINTENTAR_RECETAS,
            permitirReintento =
                true
        )
    }

    private fun mostrarRecetas(
        recetas: List<RecetaApiResponse>,
        mensajeVacio: String
    ) {
        adapter.actualizarLista(
            recetas
        )

        if (recetas.isEmpty()) {
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

        rvRecetas.visibility =
            View.VISIBLE
    }

    private fun mostrarEstado(
        mensaje: String
    ) {
        tvEstado.text =
            mensaje

        tvEstado.visibility =
            View.VISIBLE

        rvRecetas.visibility =
            View.GONE
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

        requireActivity().finish()
    }

    private fun vistaDisponible():
            Boolean {
        return isAdded &&
                view != null
    }

    companion object {

        private const val REQUEST_REINTENTAR_RECETAS =
            "REQUEST_REINTENTAR_RECETAS"

        private const val TIEMPO_MAXIMO_API_MS =
            30_000L
    }
}