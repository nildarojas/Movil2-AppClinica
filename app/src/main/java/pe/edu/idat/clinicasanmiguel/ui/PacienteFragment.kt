package pe.edu.idat.clinicasanmiguel.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.utils.LoadingController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException

class PacienteFragment :
    Fragment(R.layout.activity_paciente) {

    private lateinit var tvSaludoBienvenida:
            TextView

    private lateinit var layoutVacio:
            LinearLayout

    private lateinit var layoutCita:
            LinearLayout

    private lateinit var btnHorarios:
            MaterialButton

    private lateinit var btnHorariosVacio:
            MaterialButton

    private lateinit var tvHomeEspecialidad:
            TextView

    private lateinit var tvHomeMedico:
            TextView

    private lateinit var tvHomeFechaHora:
            TextView

    private lateinit var tvHomeEstado:
            TextView

    private lateinit var loadingController:
            LoadingController

    private var cargandoUltimaCita =
        false

    private var loadingToken:
            Long? = null

    private var llamadaUltimaCita:
            Call<CitaApiResponse>? = null

    private var dialogoInformativo:
            AlertDialog? = null

    private val gson =
        Gson()

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

        tvSaludoBienvenida =
            view.findViewById(
                R.id.tvSaludoBienvenida
            )

        val ivCampanaNotificacion =
            view.findViewById<ImageView>(
                R.id.ivCampanaNotificacion
            )

        btnHorarios =
            view.findViewById(
                R.id.btnHorarios
            )

        btnHorariosVacio =
            view.findViewById(
                R.id.btnHorariosVacio
            )

        val btnVerOtrasCitas =
            view.findViewById<MaterialButton>(
                R.id.btnVerOtrasCitas
            )

        layoutVacio =
            view.findViewById(
                R.id.layoutEstadoVacio
            )

        layoutCita =
            view.findViewById(
                R.id.layoutUltimaCita
            )

        tvHomeEspecialidad =
            view.findViewById(
                R.id.tvHomeEspecialidad
            )

        tvHomeMedico =
            view.findViewById(
                R.id.tvHomeMedico
            )

        tvHomeFechaHora =
            view.findViewById(
                R.id.tvHomeFechaHora
            )

        tvHomeEstado =
            view.findViewById(
                R.id.tvHomeEstado
            )

        val preferencias =
            requireContext()
                .getSharedPreferences(
                    "sesion_clinica",
                    Context.MODE_PRIVATE
                )

        val nombreUsuario =
            preferencias.getString(
                "NOMBRE_USUARIO",
                "Paciente"
            ) ?: "Paciente"

        tvSaludoBienvenida.text =
            "¡Bienvenido,\n$nombreUsuario!"

        btnHorarios.setOnClickListener {
            cambiarPantallaDesdeInicio(
                SeleccionarEspecialidadFragment()
            )
        }

        btnHorariosVacio.setOnClickListener {
            cambiarPantallaDesdeInicio(
                SeleccionarEspecialidadFragment()
            )
        }

        btnVerOtrasCitas.setOnClickListener {
            cambiarPantallaDesdeInicio(
                MisCitasFragment()
            )
        }

        ivCampanaNotificacion.setOnClickListener {
            cambiarPantallaDesdeInicio(
                NotificacionesFragment()
            )
        }

        mostrarDatosGuardadosIniciales()
    }

    override fun onResume() {
        super.onResume()

        cargarUltimaCitaDesdeApi()
    }

    private fun cargarUltimaCitaDesdeApi() {

        if (cargandoUltimaCita) {
            return
        }

        if (!vistaDisponible()) {
            return
        }

        cargandoUltimaCita = true

        loadingToken =
            loadingController.show(
                message = "Cargando datos..."
            )

        if (!hayConexionAInternet()) {

            mostrarCachePorFaltaDeConexion(
                detalleConexion =
                    "No se detectó acceso real a Internet. " +
                            "Aunque tus datos móviles o Wi-Fi estén encendidos, " +
                            "puede que no tengas megas, saldo disponible o acceso para navegar."
            )

            return
        }

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService.obtenerUltimaCita()

        llamadaUltimaCita =
            llamada

        llamada.enqueue(
            object :
                Callback<CitaApiResponse> {

                override fun onResponse(
                    call: Call<CitaApiResponse>,
                    response: Response<CitaApiResponse>
                ) {
                    llamadaUltimaCita = null

                    if (!vistaDisponible()) {
                        return
                    }

                    if (response.isSuccessful) {

                        val ultimaCita =
                            response.body()

                        if (ultimaCita == null) {

                            limpiarUltimaCitaGuardada()
                            mostrarEstadoVacio()

                        } else {

                            guardarUltimaCita(
                                ultimaCita
                            )

                            mostrarUltimaCita(
                                ultimaCita
                            )
                        }

                        finalizarCargaNormal()
                        return
                    }

                    when (response.code()) {

                        401 -> {
                            finalizarCargaNormal {
                                cerrarSesion(
                                    obtenerMensajeError(
                                        response
                                    ) ?: "Tu sesión ha vencido"
                                )
                            }
                        }

                        403 -> {

                            limpiarUltimaCitaGuardada()
                            mostrarEstadoVacio()

                            finalizarCargaConDialogo(
                                mensajeCarga =
                                    "Acceso denegado",
                                titulo =
                                    "No tienes permiso",
                                detalle =
                                    obtenerMensajeError(
                                        response
                                    ) ?: "No tienes permiso para consultar las citas.",
                                permitirReintento =
                                    false
                            )
                        }

                        404 -> {

                            limpiarUltimaCitaGuardada()
                            mostrarEstadoVacio()
                            finalizarCargaNormal()
                        }

                        else -> {

                            mostrarCachePorErrorDelServidor(
                                mensajeServidor =
                                    obtenerMensajeError(
                                        response
                                    )
                            )
                        }
                    }
                }

                override fun onFailure(
                    call: Call<CitaApiResponse>,
                    throwable: Throwable
                ) {
                    llamadaUltimaCita = null

                    if (call.isCanceled) {
                        return
                    }

                    if (!vistaDisponible()) {
                        return
                    }

                    when (throwable) {

                        is InterruptedIOException -> {

                            mostrarCachePorFaltaDeConexion(
                                detalleConexion =
                                    "La conexión tardó demasiado en responder. " +
                                            "Tus datos móviles o Wi-Fi pueden estar activos, " +
                                            "pero quizá no tengas megas disponibles, " +
                                            "la señal sea demasiado lenta o el servidor no haya respondido a tiempo."
                            )
                        }

                        is IOException -> {

                            mostrarCachePorFaltaDeConexion(
                                detalleConexion =
                                    "No fue posible acceder a Internet. " +
                                            "Verifica que tengas megas o saldo disponible " +
                                            "y que tu red permita navegar."
                            )
                        }

                        else -> {

                            if (!hayConexionAInternet()) {

                                mostrarCachePorFaltaDeConexion(
                                    detalleConexion =
                                        "No se detectó acceso real a Internet. " +
                                                "Aunque la red aparezca encendida, " +
                                                "puede que no tengas megas o saldo disponible."
                                )

                            } else {

                                mostrarCachePorErrorDelServidor()
                            }
                        }
                    }
                }
            }
        )
    }

    private fun mostrarCachePorFaltaDeConexion(
        detalleConexion: String
    ) {
        val citaGuardada =
            obtenerUltimaCitaGuardada()

        if (citaGuardada != null) {

            mostrarUltimaCita(
                citaGuardada
            )

            finalizarCargaSinConexion(
                detalle =
                    "$detalleConexion\n\n" +
                            "Se está mostrando la última cita guardada " +
                            "en este dispositivo. Esta información podría " +
                            "no corresponder a la cita más reciente."
            )

        } else {

            mostrarEstadoVacio()

            finalizarCargaSinConexion(
                detalle =
                    "$detalleConexion\n\n" +
                            "No existe una cita guardada en este dispositivo " +
                            "para mostrar sin conexión."
            )
        }
    }

    private fun mostrarCachePorErrorDelServidor(
        mensajeServidor: String? = null
    ) {
        val citaGuardada =
            obtenerUltimaCitaGuardada()

        val detalleServidor =
            mensajeServidor
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "El servidor no pudo completar la solicitud."

        if (citaGuardada != null) {

            mostrarUltimaCita(
                citaGuardada
            )

            finalizarCargaConDialogo(
                mensajeCarga =
                    "Servicio no disponible",
                titulo =
                    "No se pudieron actualizar los datos",
                detalle =
                    "$detalleServidor\n\n" +
                            "Se está mostrando la última cita guardada " +
                            "en este dispositivo. Esta información podría " +
                            "no corresponder a la cita más reciente.",
                permitirReintento =
                    true
            )

        } else {

            mostrarEstadoVacio()

            finalizarCargaConDialogo(
                mensajeCarga =
                    "Servicio no disponible",
                titulo =
                    "No se pudieron obtener los datos",
                detalle =
                    "$detalleServidor\n\n" +
                            "No existe una cita guardada en este dispositivo.",
                permitirReintento =
                    true
            )
        }
    }

    private fun finalizarCargaNormal(
        despuesDeCerrar: () -> Unit = {}
    ) {
        val token =
            loadingToken

        if (token == null) {

            cargandoUltimaCita = false
            despuesDeCerrar()
            return
        }

        loadingController.hide(
            requestToken = token
        ) {

            if (vistaDisponible()) {

                loadingToken = null
                cargandoUltimaCita = false

                despuesDeCerrar()
            }
        }
    }

    private fun finalizarCargaSinConexion(
        detalle: String
    ) {
        finalizarCargaConDialogo(
            mensajeCarga =
                "Sin conexión",
            titulo =
                "Sin conexión a Internet",
            detalle =
                detalle,
            permitirReintento =
                true
        )
    }

    private fun finalizarCargaConDialogo(
        mensajeCarga: String,
        titulo: String,
        detalle: String,
        permitirReintento: Boolean
    ) {
        val tokenMensaje =
            loadingController.show(
                message = mensajeCarga
            )

        loadingToken =
            tokenMensaje

        loadingController.hide(
            requestToken = tokenMensaje
        ) {

            if (vistaDisponible()) {

                loadingToken = null
                cargandoUltimaCita = false

                mostrarDialogoInformativo(
                    titulo = titulo,
                    detalle = detalle,
                    permitirReintento =
                        permitirReintento
                )
            }
        }
    }

    private fun mostrarDialogoInformativo(
        titulo: String,
        detalle: String,
        permitirReintento: Boolean
    ) {
        if (!vistaDisponible()) {
            return
        }

        dialogoInformativo?.dismiss()

        val constructor =
            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle(
                    titulo
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

        if (permitirReintento) {

            constructor.setPositiveButton(
                "REINTENTAR"
            ) { dialog, _ ->

                dialog.dismiss()
                cargarUltimaCitaDesdeApi()
            }
        }

        dialogoInformativo =
            constructor.create()

        dialogoInformativo
            ?.setOnDismissListener {
                dialogoInformativo = null
            }

        dialogoInformativo?.show()
    }

    private fun mostrarDatosGuardadosIniciales() {

        val citaGuardada =
            obtenerUltimaCitaGuardada()

        if (citaGuardada != null) {

            mostrarUltimaCita(
                citaGuardada
            )

        } else {

            mostrarEstadoVacio()
        }
    }

    private fun guardarUltimaCita(
        cita: CitaApiResponse
    ) {
        val preferenciasCache =
            requireContext()
                .getSharedPreferences(
                    CACHE_PREFERENCES,
                    Context.MODE_PRIVATE
                )

        val citaJson =
            gson.toJson(
                cita
            )

        preferenciasCache
            .edit()
            .putString(
                obtenerClaveCacheUsuario(),
                citaJson
            )
            .apply()
    }

    private fun obtenerUltimaCitaGuardada():
            CitaApiResponse? {

        val preferenciasCache =
            requireContext()
                .getSharedPreferences(
                    CACHE_PREFERENCES,
                    Context.MODE_PRIVATE
                )

        val clave =
            obtenerClaveCacheUsuario()

        val citaJson =
            preferenciasCache.getString(
                clave,
                null
            ) ?: return null

        return try {

            gson.fromJson(
                citaJson,
                CitaApiResponse::class.java
            )

        } catch (exception: Exception) {

            preferenciasCache
                .edit()
                .remove(
                    clave
                )
                .apply()

            null
        }
    }

    private fun limpiarUltimaCitaGuardada() {

        val preferenciasCache =
            requireContext()
                .getSharedPreferences(
                    CACHE_PREFERENCES,
                    Context.MODE_PRIVATE
                )

        preferenciasCache
            .edit()
            .remove(
                obtenerClaveCacheUsuario()
            )
            .apply()
    }

    private fun obtenerClaveCacheUsuario():
            String {

        val preferenciasSesion =
            requireContext()
                .getSharedPreferences(
                    "sesion_clinica",
                    Context.MODE_PRIVATE
                )

        val idUsuarioApi =
            preferenciasSesion.getInt(
                "ID_USUARIO_API",
                -1
            )

        if (idUsuarioApi > 0) {

            return "${CACHE_KEY_PREFIX}api_$idUsuarioApi"
        }

        val idUsuarioLocal =
            preferenciasSesion.getInt(
                "ID_USUARIO",
                -1
            )

        return "${CACHE_KEY_PREFIX}local_$idUsuarioLocal"
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
            ) && capacidades.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )

        } catch (exception: SecurityException) {

            true
        }
    }

    private fun mostrarUltimaCita(
        cita: CitaApiResponse
    ) {
        layoutVacio.visibility =
            View.GONE

        layoutCita.visibility =
            View.VISIBLE

        btnHorarios.visibility =
            View.VISIBLE

        btnHorariosVacio.visibility =
            View.GONE

        tvHomeEspecialidad.text =
            cita.especialidad

        tvHomeMedico.text =
            "Médico: ${cita.medico}"

        tvHomeFechaHora.text =
            "Horario: ${cita.fechaHora}"

        tvHomeEstado.text =
            cita.estado
    }

    private fun mostrarEstadoVacio() {

        layoutCita.visibility =
            View.GONE

        layoutVacio.visibility =
            View.VISIBLE

        btnHorarios.visibility =
            View.GONE

        btnHorariosVacio.visibility =
            View.VISIBLE

        tvHomeEspecialidad.text =
            ""

        tvHomeMedico.text =
            ""

        tvHomeFechaHora.text =
            ""

        tvHomeEstado.text =
            ""
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

    private fun cambiarPantallaDesdeInicio(
        fragment: Fragment
    ) {
        if (!isAdded) {
            return
        }

        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(
                R.id.flContenedor,
                fragment
            )
            .addToBackStack(
                fragment::class.java.simpleName
            )
            .commit()
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

        llamadaUltimaCita?.cancel()
        llamadaUltimaCita = null

        dialogoInformativo?.dismiss()
        dialogoInformativo = null

        cargandoUltimaCita = false
        loadingToken = null

        if (::loadingController.isInitialized) {

            loadingController.forceHide()
        }

        super.onDestroyView()
    }

    companion object {

        private const val CACHE_PREFERENCES =
            "cache_clinica"

        private const val CACHE_KEY_PREFIX =
            "ultima_cita_"
    }
}