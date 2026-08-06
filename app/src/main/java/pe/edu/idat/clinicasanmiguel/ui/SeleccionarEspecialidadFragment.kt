package pe.edu.idat.clinicasanmiguel.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.network.EspecialidadApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.utils.LoadingController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import pe.edu.idat.clinicasanmiguel.entity.Especialidad

class SeleccionarEspecialidadFragment :
    Fragment(R.layout.activity_seleccionar_especialidad) {

    private lateinit var rvEspecialidades:
            RecyclerView

    private lateinit var especialidadAdapter:
            EspecialidadAdminAdapter
    private lateinit var loadingController:
            LoadingController

    private var cargandoEspecialidades =
        false

    private var loadingToken:
            Long? = null

    private var llamadaEspecialidades:
            Call<List<EspecialidadApiResponse>>? = null

    private var dialogoInformativo:
            AlertDialog? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        loadingController =
            LoadingController(
                fragmentManager =
                    parentFragmentManager,
                coroutineScope =
                    viewLifecycleOwner.lifecycleScope
            )

        rvEspecialidades =
            view.findViewById(
                R.id.lvEspecialidadesReserva
            )

        rvEspecialidades.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        especialidadAdapter =
            EspecialidadAdminAdapter(
                listaEspecialidades =
                    emptyList(),
                onItemClick = click@{
                        especialidadSeleccionada ->

                    if (cargandoEspecialidades) {
                        return@click
                    }

                    if (especialidadSeleccionada.id <= 0) {
                        Toast.makeText(
                            requireContext(),
                            "No se pudo identificar la especialidad",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@click
                    }

                    val siguientePaso =
                        SeleccionarMedicoHorarioFragment()
                            .apply {
                                arguments =
                                    Bundle().apply {
                                        putInt(
                                            "ID_ESPECIALIDAD",
                                            especialidadSeleccionada.id
                                        )

                                        putString(
                                            "NOMBRE_ESPECIALIDAD",
                                            especialidadSeleccionada.nombre
                                        )
                                    }
                            }

                    parentFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.flContenedor,
                            siguientePaso
                        )
                        .addToBackStack(
                            null
                        )
                        .commit()
                }
            )

        rvEspecialidades.adapter =
            especialidadAdapter

        cargarEspecialidadesDesdeApi()
    }

    private fun cargarEspecialidadesDesdeApi() {
        if (cargandoEspecialidades) {
            return
        }

        if (!vistaDisponible()) {
            return
        }

        cargandoEspecialidades = true

        mostrarEspecialidades(
            emptyList()
        )

        loadingToken =
            loadingController.show(
                message =
                    "Cargando especialidades..."
            )

        if (!hayConexionAInternet()) {
            finalizarCargaSinConexion(
                detalle =
                    "No fue posible consultar las especialidades. " +
                            "Verifica que tengas acceso a Internet, megas disponibles " +
                            "o una conexión Wi-Fi que permita navegar."
            )

            return
        }

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService.listarEspecialidades()

        llamadaEspecialidades =
            llamada

        llamada.enqueue(
            object :
                Callback<List<EspecialidadApiResponse>> {

                override fun onResponse(
                    call: Call<List<EspecialidadApiResponse>>,
                    response: Response<List<EspecialidadApiResponse>>
                ) {
                    llamadaEspecialidades = null

                    if (!vistaDisponible()) {
                        return
                    }

                    if (response.isSuccessful) {
                        val especialidades =
                            response.body()
                                ?: emptyList()

                        mostrarEspecialidades(
                            especialidades
                        )

                        finalizarCarga callback@{
                            if (!vistaDisponible()) {
                                return@callback
                            }

                            if (especialidades.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No existen especialidades disponibles",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        return
                    }

                    when (response.code()) {
                        401 -> {
                            mostrarEspecialidades(
                                emptyList()
                            )

                            finalizarCarga {
                                if (vistaDisponible()) {
                                    cerrarSesion(
                                        obtenerMensajeError(
                                            response
                                        ) ?: "Tu sesión ha vencido"
                                    )
                                }
                            }
                        }

                        403 -> {
                            mostrarEspecialidades(
                                emptyList()
                            )

                            finalizarCargaConEstado(
                                mensaje =
                                    "Acceso denegado"
                            ) {
                                if (vistaDisponible()) {
                                    Toast.makeText(
                                        requireContext(),
                                        obtenerMensajeError(
                                            response
                                        ) ?: "No tienes permiso para consultar las especialidades",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }

                        else -> {
                            mostrarEspecialidades(
                                emptyList()
                            )

                            finalizarCargaConEstado(
                                mensaje =
                                    "Servicio no disponible"
                            ) {
                                if (vistaDisponible()) {
                                    Toast.makeText(
                                        requireContext(),
                                        obtenerMensajeError(
                                            response
                                        ) ?: "No se pudieron cargar las especialidades",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<List<EspecialidadApiResponse>>,
                    throwable: Throwable
                ) {
                    llamadaEspecialidades = null

                    if (call.isCanceled) {
                        return
                    }

                    if (!vistaDisponible()) {
                        return
                    }

                    mostrarEspecialidades(
                        emptyList()
                    )

                    if (
                        throwable is IOException ||
                        !hayConexionAInternet()
                    ) {
                        finalizarCargaSinConexion(
                            detalle =
                                "No fue posible consultar las especialidades. " +
                                        "Tus datos móviles o Wi-Fi pueden estar activos, " +
                                        "pero quizá no tengas megas disponibles, " +
                                        "la red no permita navegar o la conexión sea demasiado lenta."
                        )

                    } else {
                        finalizarCargaConEstado(
                            mensaje =
                                "Error al cargar"
                        ) {
                            if (vistaDisponible()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No se pudieron cargar las especialidades",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        )
    }

    private fun finalizarCarga(
        despuesDeCerrar: () -> Unit = {}
    ) {
        val token =
            loadingToken

        if (token == null) {
            cargandoEspecialidades = false
            despuesDeCerrar()
            return
        }

        loadingController.hide(
            requestToken =
                token
        ) callback@{

            if (!vistaDisponible()) {
                return@callback
            }

            loadingToken = null
            cargandoEspecialidades = false

            despuesDeCerrar()
        }
    }

    private fun finalizarCargaConEstado(
        mensaje: String,
        despuesDeCerrar: () -> Unit = {}
    ) {
        val tokenEstado =
            loadingController.show(
                message =
                    mensaje
            )

        loadingToken =
            tokenEstado

        loadingController.hide(
            requestToken =
                tokenEstado
        ) callback@{

            if (!vistaDisponible()) {
                return@callback
            }

            loadingToken = null
            cargandoEspecialidades = false

            despuesDeCerrar()
        }
    }

    private fun finalizarCargaSinConexion(
        detalle: String
    ) {
        val tokenEstado =
            loadingController.show(
                message =
                    "Sin conexión"
            )

        loadingToken =
            tokenEstado

        loadingController.hide(
            requestToken =
                tokenEstado
        ) callback@{

            if (!vistaDisponible()) {
                return@callback
            }

            loadingToken = null
            cargandoEspecialidades = false

            mostrarDialogoInformativo(
                detalle
            )
        }
    }

    private fun mostrarDialogoInformativo(
        detalle: String
    ) {
        if (!vistaDisponible()) {
            return
        }

        dialogoInformativo?.dismiss()

        dialogoInformativo =
            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle(
                    "Sin conexión a Internet"
                )
                .setMessage(
                    detalle
                )
                .setCancelable(
                    false
                )
                .setNegativeButton(
                    "ENTENDIDO"
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(
                    "REINTENTAR"
                ) { dialog, _ ->
                    dialog.dismiss()
                    cargarEspecialidadesDesdeApi()
                }
                .create()

        dialogoInformativo
            ?.setOnDismissListener {
                dialogoInformativo = null
            }

        dialogoInformativo?.show()
    }

    private fun mostrarEspecialidades(
        especialidades: List<EspecialidadApiResponse>
    ) {
        if (!::especialidadAdapter.isInitialized) {
            return
        }

        val lista =
            especialidades.map {
                    especialidadApi ->

                Especialidad(
                    id = especialidadApi.id,
                    nombre = especialidadApi.nombre
                )
            }

        especialidadAdapter
            .actualizarLista(
                lista
            )
    }

    private fun hayConexionAInternet():
            Boolean {
        return try {
            val connectivityManager =
                requireContext()
                    .getSystemService(
                        Context.CONNECTIVITY_SERVICE
                    ) as ConnectivityManager

            val redActiva =
                connectivityManager.activeNetwork
                    ?: return false

            val capacidades =
                connectivityManager
                    .getNetworkCapabilities(
                        redActiva
                    ) ?: return false

            capacidades.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) &&
                    capacidades.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )

        } catch (exception: SecurityException) {
            true
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

    override fun onDestroyView() {
        llamadaEspecialidades?.cancel()
        llamadaEspecialidades = null

        dialogoInformativo?.dismiss()
        dialogoInformativo = null

        cargandoEspecialidades = false
        loadingToken = null

        if (::loadingController.isInitialized) {
            loadingController.forceHide()
        }

        super.onDestroyView()
    }
}