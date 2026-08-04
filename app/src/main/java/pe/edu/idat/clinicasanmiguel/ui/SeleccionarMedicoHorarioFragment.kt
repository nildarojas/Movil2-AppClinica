package pe.edu.idat.clinicasanmiguel.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
import pe.edu.idat.clinicasanmiguel.network.CrearCitaApiRequest
import pe.edu.idat.clinicasanmiguel.network.MedicoApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.utils.LoadingController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class SeleccionarMedicoHorarioFragment :
    Fragment(R.layout.activity_seleccionar_medico_horario) {

    private lateinit var acMedico:
            AutoCompleteTextView

    private lateinit var acHorario:
            AutoCompleteTextView

    private lateinit var btnConfirmar:
            MaterialButton

    private lateinit var loadingController:
            LoadingController

    private var medicos =
        emptyList<MedicoApiResponse>()

    private var horarios =
        emptyList<String>()

    private var idMedicoSeleccionado =
        -1

    private var horarioSeleccionado =
        ""

    private var cargandoMedicos =
        false

    private var cargandoHorarios =
        false

    private var reservandoCita =
        false

    private var loadingToken:
            Long? = null

    private var llamadaMedicos:
            Call<List<MedicoApiResponse>>? = null

    private var llamadaHorarios:
            Call<List<String>>? = null

    private var llamadaReserva:
            Call<CitaApiResponse>? = null

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

        acMedico =
            view.findViewById(
                R.id.acMedicoReserva
            )

        acHorario =
            view.findViewById(
                R.id.acHorarioReserva
            )

        btnConfirmar =
            view.findViewById(
                R.id.btnConfirmarReservaFinal
            )

        val idEspecialidad =
            arguments?.getInt(
                "ID_ESPECIALIDAD",
                -1
            ) ?: -1

        val nombreEspecialidad =
            arguments?.getString(
                "NOMBRE_ESPECIALIDAD"
            ).orEmpty()

        if (idEspecialidad <= 0) {
            Toast.makeText(
                requireContext(),
                "La especialidad seleccionada no es válida",
                Toast.LENGTH_LONG
            ).show()

            parentFragmentManager.popBackStack()
            return
        }

        acMedico.setOnClickListener {
            if (
                acMedico.isEnabled &&
                !hayOperacionEnCurso()
            ) {
                acMedico.showDropDown()
            }
        }

        acHorario.setOnClickListener {
            if (
                acHorario.isEnabled &&
                !hayOperacionEnCurso()
            ) {
                acHorario.showDropDown()
            }
        }

        acMedico.setOnItemClickListener {
                _,
                _,
                position,
                _ ->

            if (hayOperacionEnCurso()) {
                return@setOnItemClickListener
            }

            if (position !in medicos.indices) {
                idMedicoSeleccionado = -1
                limpiarHorarios()
                actualizarEstadoControles()
                return@setOnItemClickListener
            }

            val medico =
                medicos[position]

            idMedicoSeleccionado =
                medico.id

            limpiarHorarios()
            actualizarEstadoControles()

            cargarHorariosDesdeApi(
                medico.id
            )
        }

        acHorario.setOnItemClickListener {
                _,
                _,
                position,
                _ ->

            if (hayOperacionEnCurso()) {
                return@setOnItemClickListener
            }

            if (position !in horarios.indices) {
                horarioSeleccionado = ""
                actualizarEstadoControles()
                return@setOnItemClickListener
            }

            horarioSeleccionado =
                horarios[position]

            actualizarEstadoControles()
        }

        btnConfirmar.setOnClickListener {
            reservarCita(
                nombreEspecialidad
            )
        }

        limpiarMedicosYHorarios()

        cargarMedicosDesdeApi(
            idEspecialidad
        )
    }

    private fun cargarMedicosDesdeApi(
        idEspecialidad: Int
    ) {
        if (hayOperacionEnCurso()) {
            return
        }

        if (!vistaDisponible()) {
            return
        }

        cargandoMedicos = true

        limpiarMedicosYHorarios()
        actualizarEstadoControles()

        loadingToken =
            loadingController.show(
                message =
                    "Cargando médicos..."
            )

        if (!hayConexionAInternet()) {
            finalizarCargaSinConexion(
                detalle =
                    "No fue posible consultar los médicos. " +
                            "Verifica que tengas acceso a Internet, megas disponibles " +
                            "o una conexión Wi-Fi que permita navegar.",
                alReintentar = {
                    cargarMedicosDesdeApi(
                        idEspecialidad
                    )
                }
            ) {
                cargandoMedicos = false
                actualizarEstadoControles()
            }

            return
        }

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService
                .listarMedicosPorEspecialidad(
                    idEspecialidad
                )

        llamadaMedicos =
            llamada

        llamada.enqueue(
            object :
                Callback<List<MedicoApiResponse>> {

                override fun onResponse(
                    call: Call<List<MedicoApiResponse>>,
                    response: Response<List<MedicoApiResponse>>
                ) {
                    llamadaMedicos = null

                    if (!vistaDisponible()) {
                        return
                    }

                    if (response.isSuccessful) {
                        medicos =
                            response.body()
                                ?: emptyList()

                        val nombres =
                            medicos.map {
                                it.nombre
                            }

                        acMedico.setAdapter(
                            ArrayAdapter(
                                requireContext(),
                                R.layout.spinner_perfil_item,
                                nombres
                            )
                        )

                        finalizarCarga callback@{
                            cargandoMedicos = false
                            actualizarEstadoControles()

                            if (!vistaDisponible()) {
                                return@callback
                            }

                            if (medicos.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "No existen médicos registrados para esta especialidad",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        return
                    }

                    limpiarMedicosYHorarios()

                    finalizarErrorRespuesta(
                        response = response,
                        mensajePredeterminado =
                            "No se pudieron cargar los médicos"
                    ) {
                        cargandoMedicos = false
                        actualizarEstadoControles()
                    }
                }

                override fun onFailure(
                    call: Call<List<MedicoApiResponse>>,
                    throwable: Throwable
                ) {
                    llamadaMedicos = null

                    if (call.isCanceled) {
                        return
                    }

                    if (!vistaDisponible()) {
                        return
                    }

                    limpiarMedicosYHorarios()

                    finalizarErrorConexion(
                        throwable = throwable,
                        detalle =
                            "No fue posible consultar los médicos. " +
                                    "Tus datos móviles o Wi-Fi pueden estar activos, " +
                                    "pero quizá no tengas megas disponibles, " +
                                    "la red no permita navegar o la conexión sea demasiado lenta.",
                        alReintentar = {
                            cargarMedicosDesdeApi(
                                idEspecialidad
                            )
                        }
                    ) {
                        cargandoMedicos = false
                        actualizarEstadoControles()
                    }
                }
            }
        )
    }

    private fun cargarHorariosDesdeApi(
        idMedico: Int
    ) {
        if (hayOperacionEnCurso()) {
            return
        }

        if (!vistaDisponible()) {
            return
        }

        cargandoHorarios = true

        limpiarHorarios()
        actualizarEstadoControles()

        loadingToken =
            loadingController.show(
                message =
                    "Cargando horarios..."
            )

        if (!hayConexionAInternet()) {
            finalizarCargaSinConexion(
                detalle =
                    "No fue posible consultar los horarios disponibles. " +
                            "Verifica que tengas acceso a Internet, megas disponibles " +
                            "o una conexión Wi-Fi que permita navegar.",
                alReintentar = {
                    cargarHorariosDesdeApi(
                        idMedico
                    )
                }
            ) {
                cargandoHorarios = false
                actualizarEstadoControles()
            }

            return
        }

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService.listarHorariosConEstado(
                idMedico =
                    idMedico
            )

        llamadaHorarios =
            llamada

        llamada.enqueue(
            object :
                Callback<List<String>> {

                override fun onResponse(
                    call: Call<List<String>>,
                    response: Response<List<String>>
                ) {
                    llamadaHorarios = null

                    if (!vistaDisponible()) {
                        return
                    }

                    if (response.isSuccessful) {
                        horarios =
                            response.body()
                                ?: emptyList()

                        acHorario.setAdapter(
                            ArrayAdapter(
                                requireContext(),
                                R.layout.spinner_perfil_item,
                                horarios
                            )
                        )

                        finalizarCarga callback@{
                            cargandoHorarios = false
                            actualizarEstadoControles()

                            if (!vistaDisponible()) {
                                return@callback
                            }

                            if (horarios.isEmpty()) {
                                Toast.makeText(
                                    requireContext(),
                                    "Este médico no tiene horarios disponibles",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                acHorario.showDropDown()
                            }
                        }

                        return
                    }

                    limpiarHorarios()

                    finalizarErrorRespuesta(
                        response = response,
                        mensajePredeterminado =
                            "No se pudieron cargar los horarios"
                    ) {
                        cargandoHorarios = false
                        actualizarEstadoControles()
                    }
                }

                override fun onFailure(
                    call: Call<List<String>>,
                    throwable: Throwable
                ) {
                    llamadaHorarios = null

                    if (call.isCanceled) {
                        return
                    }

                    if (!vistaDisponible()) {
                        return
                    }

                    limpiarHorarios()

                    finalizarErrorConexion(
                        throwable = throwable,
                        detalle =
                            "No fue posible consultar los horarios. " +
                                    "Tus datos móviles o Wi-Fi pueden estar activos, " +
                                    "pero quizá no tengas megas disponibles, " +
                                    "la red no permita navegar o la conexión sea demasiado lenta.",
                        alReintentar = {
                            cargarHorariosDesdeApi(
                                idMedico
                            )
                        }
                    ) {
                        cargandoHorarios = false
                        actualizarEstadoControles()
                    }
                }
            }
        )
    }

    private fun reservarCita(
        nombreEspecialidad: String
    ) {
        if (hayOperacionEnCurso()) {
            return
        }

        val medicoSeleccionado =
            acMedico.text
                .toString()
                .trim()

        val horarioEscrito =
            acHorario.text
                .toString()
                .trim()

        if (
            idMedicoSeleccionado <= 0 ||
            medicoSeleccionado.isEmpty() ||
            horarioSeleccionado.isEmpty() ||
            horarioEscrito != horarioSeleccionado
        ) {
            Toast.makeText(
                requireContext(),
                "Selecciona un médico y un horario",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (
            horarioSeleccionado.contains(
                "(Ocupado por ti)"
            )
        ) {
            Toast.makeText(
                requireContext(),
                "Ya tienes una cita registrada en ese horario",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (
            horarioSeleccionado.contains(
                "(Médico ocupado en este horario)"
            )
        ) {
            Toast.makeText(
                requireContext(),
                "El médico ya tiene una cita en ese horario",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        reservandoCita = true
        actualizarEstadoControles()

        loadingToken =
            loadingController.show(
                message =
                    "Registrando cita..."
            )

        if (!hayConexionAInternet()) {
            finalizarCargaSinConexion(
                detalle =
                    "No fue posible registrar la cita. " +
                            "La operación no fue confirmada por el servidor. " +
                            "Verifica tu conexión antes de intentarlo nuevamente.",
                alReintentar = {
                    reservarCita(
                        nombreEspecialidad
                    )
                }
            ) {
                reservandoCita = false
                actualizarEstadoControles()
            }

            return
        }

        val request =
            CrearCitaApiRequest(
                idMedico =
                    idMedicoSeleccionado,
                fechaHora =
                    horarioSeleccionado
            )

        val apiService =
            RetrofitClient.obtenerApiService(
                requireContext()
            )

        val llamada =
            apiService.reservarCita(
                request
            )

        llamadaReserva =
            llamada

        llamada.enqueue(
            object :
                Callback<CitaApiResponse> {

                override fun onResponse(
                    call: Call<CitaApiResponse>,
                    response: Response<CitaApiResponse>
                ) {
                    llamadaReserva = null

                    if (!vistaDisponible()) {
                        return
                    }

                    if (response.isSuccessful) {
                        val cita =
                            response.body()

                        if (cita == null) {
                            finalizarCargaConEstado(
                                mensaje =
                                    "Error al registrar"
                            ) {
                                reservandoCita = false
                                actualizarEstadoControles()

                                if (vistaDisponible()) {
                                    Toast.makeText(
                                        requireContext(),
                                        "La API devolvió una respuesta incompleta",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            return
                        }

                        val especialidad =
                            nombreEspecialidad
                                .ifBlank {
                                    cita.especialidad
                                }

                        finalizarCarga callback@{
                            reservandoCita = false

                            if (!vistaDisponible()) {
                                return@callback
                            }

                            Toast.makeText(
                                requireContext(),
                                "Cita N° ${cita.id} en $especialidad registrada correctamente",
                                Toast.LENGTH_LONG
                            ).show()

                            abrirMisCitas()
                        }

                        return
                    }

                    finalizarErrorRespuesta(
                        response = response,
                        mensajePredeterminado =
                            "No se pudo registrar la cita"
                    ) {
                        reservandoCita = false
                        actualizarEstadoControles()
                    }
                }

                override fun onFailure(
                    call: Call<CitaApiResponse>,
                    throwable: Throwable
                ) {
                    llamadaReserva = null

                    if (call.isCanceled) {
                        return
                    }

                    if (!vistaDisponible()) {
                        return
                    }

                    finalizarErrorConexion(
                        throwable = throwable,
                        detalle =
                            "No fue posible registrar la cita. " +
                                    "La operación no fue confirmada por el servidor. " +
                                    "Tus datos móviles o Wi-Fi pueden estar activos, " +
                                    "pero la conexión no permitió completar la solicitud.",
                        alReintentar = {
                            reservarCita(
                                nombreEspecialidad
                            )
                        }
                    ) {
                        reservandoCita = false
                        actualizarEstadoControles()
                    }
                }
            }
        )
    }

    private fun finalizarErrorRespuesta(
        response: Response<*>,
        mensajePredeterminado: String,
        despuesDeCerrar: () -> Unit
    ) {
        val mensaje =
            obtenerMensajeError(
                response
            )

        if (response.code() == 401) {
            finalizarCarga {
                despuesDeCerrar()

                if (vistaDisponible()) {
                    cerrarSesion(
                        mensaje
                            ?: "Tu sesión ha vencido"
                    )
                }
            }

            return
        }

        val mensajeEstado =
            when (response.code()) {
                403 ->
                    "Acceso denegado"

                404 ->
                    "Sin resultados"

                409 ->
                    "Horario no disponible"

                else ->
                    "Servicio no disponible"
            }

        val mensajeFinal =
            when (response.code()) {
                403 ->
                    mensaje
                        ?: "Esta acción requiere una cuenta de paciente"

                404 ->
                    mensaje
                        ?: "No se encontró la información solicitada"

                409 ->
                    mensaje
                        ?: "El horario seleccionado ya no está disponible"

                else ->
                    mensaje
                        ?: mensajePredeterminado
            }

        finalizarCargaConEstado(
            mensaje =
                mensajeEstado
        ) {
            despuesDeCerrar()

            if (vistaDisponible()) {
                Toast.makeText(
                    requireContext(),
                    mensajeFinal,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun finalizarErrorConexion(
        throwable: Throwable,
        detalle: String,
        alReintentar: () -> Unit,
        despuesDeCerrar: () -> Unit
    ) {
        if (
            throwable is IOException ||
            !hayConexionAInternet()
        ) {
            finalizarCargaSinConexion(
                detalle =
                    detalle,
                alReintentar =
                    alReintentar,
                despuesDeCerrar =
                    despuesDeCerrar
            )

            return
        }

        finalizarCargaConEstado(
            mensaje =
                "Error al cargar"
        ) {
            despuesDeCerrar()

            if (vistaDisponible()) {
                Toast.makeText(
                    requireContext(),
                    detalle,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun finalizarCarga(
        despuesDeCerrar: () -> Unit = {}
    ) {
        val token =
            loadingToken

        if (token == null) {
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
            despuesDeCerrar()
        }
    }

    private fun finalizarCargaSinConexion(
        detalle: String,
        alReintentar: () -> Unit,
        despuesDeCerrar: () -> Unit = {}
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

            despuesDeCerrar()

            mostrarDialogoInformativo(
                detalle =
                    detalle,
                alReintentar =
                    alReintentar
            )
        }
    }

    private fun mostrarDialogoInformativo(
        detalle: String,
        alReintentar: () -> Unit
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
                    alReintentar()
                }
                .create()

        dialogoInformativo
            ?.setOnDismissListener {
                dialogoInformativo = null
            }

        dialogoInformativo?.show()
    }

    private fun limpiarMedicosYHorarios() {
        medicos =
            emptyList()

        idMedicoSeleccionado =
            -1

        acMedico.setText(
            "",
            false
        )

        acMedico.setAdapter(
            null
        )

        limpiarHorarios()
    }

    private fun limpiarHorarios() {
        horarios =
            emptyList()

        horarioSeleccionado =
            ""

        acHorario.setText(
            "",
            false
        )

        acHorario.setAdapter(
            null
        )
    }

    private fun actualizarEstadoControles() {
        val bloqueado =
            hayOperacionEnCurso()

        acMedico.isEnabled =
            !bloqueado &&
                    medicos.isNotEmpty()

        acHorario.isEnabled =
            !bloqueado &&
                    idMedicoSeleccionado > 0 &&
                    horarios.isNotEmpty()

        btnConfirmar.isEnabled =
            !bloqueado &&
                    idMedicoSeleccionado > 0 &&
                    horarioSeleccionado.isNotBlank() &&
                    acHorario.text
                        .toString()
                        .trim() == horarioSeleccionado
    }

    private fun hayOperacionEnCurso():
            Boolean {
        return cargandoMedicos ||
                cargandoHorarios ||
                reservandoCita
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

    private fun abrirMisCitas() {
        if (!isAdded) {
            return
        }

        val fragmentManager =
            parentFragmentManager

        fragmentManager.popBackStackImmediate(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        fragmentManager
            .beginTransaction()
            .replace(
                R.id.flContenedor,
                MisCitasFragment()
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
        llamadaMedicos?.cancel()
        llamadaMedicos = null

        llamadaHorarios?.cancel()
        llamadaHorarios = null

        llamadaReserva?.cancel()
        llamadaReserva = null

        dialogoInformativo?.dismiss()
        dialogoInformativo = null

        cargandoMedicos = false
        cargandoHorarios = false
        reservandoCita = false
        loadingToken = null

        if (::loadingController.isInitialized) {
            loadingController.forceHide()
        }

        super.onDestroyView()
    }
}