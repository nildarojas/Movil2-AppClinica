package pe.edu.idat.clinicasanmiguel.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.entity.MedicoAdmin
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.HorarioAdminRepository
import pe.edu.idat.clinicasanmiguel.repository.MedicoAdminRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaMedicosAdminApi
import pe.edu.idat.clinicasanmiguel.repository.ResultadoRegistroHorarioAdminApi
import pe.edu.idat.clinicasanmiguel.utils.NetworkMonitor
import java.util.Calendar
import java.util.Locale

class RegistrarHorarioFragment :
    Fragment(
        R.layout.activity_registrar_horario
    ) {

    private lateinit var acMedico:
            AutoCompleteTextView

    private lateinit var etFecha:
            EditText

    private lateinit var etHoraInicio:
            EditText

    private lateinit var etHoraFin:
            EditText

    private lateinit var btnGuardar:
            MaterialButton

    private lateinit var btnCancelar:
            MaterialButton

    private lateinit var medicoRepository:
            MedicoAdminRepository

    private lateinit var horarioRepository:
            HorarioAdminRepository

    private var listaMedicos:
            List<MedicoAdmin> =
        emptyList()

    private var medicoSeleccionado:
            MedicoAdmin? = null

    private var fechaSeleccionadaMillis:
            Long? = null

    private var cargandoMedicos =
        false

    private var registrando =
        false

    private var idSolicitudMedicos =
        0L

    private var idSolicitudRegistro =
        0L

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        medicoRepository =
            MedicoAdminRepository(
                requireContext()
            )

        horarioRepository =
            HorarioAdminRepository(
                requireContext()
            )

        acMedico =
            view.findViewById(
                R.id.acMedico
            )

        etFecha =
            view.findViewById(
                R.id.etFecha
            )

        etHoraInicio =
            view.findViewById(
                R.id.etHoraInicio
            )

        etHoraFin =
            view.findViewById(
                R.id.etHoraFin
            )

        btnGuardar =
            view.findViewById(
                R.id.btnGuardarHorario
            )

        btnCancelar =
            view.findViewById(
                R.id.btnCancelarHorario
            )

        configurarSelectorFecha()
        configurarSelectorHoraInicio()
        configurarSelectorHoraFin()

        btnGuardar.setOnClickListener {
            registrarHorario()
        }

        btnCancelar.setOnClickListener {
            if (!registrando) {
                regresar()
            }
        }

        configurarMedicosVacios(
            "Cargando médicos..."
        )

        observarConexion()

        if (NetworkMonitor.hayInternet()) {
            cargarMedicosDesdeApi()
        } else {
            mostrarSinConexion()
        }
    }

    private fun observarConexion() {
        NetworkMonitor
            .estadoConexion
            .observe(
                viewLifecycleOwner
            ) { conectado ->

                if (!conectado) {
                    mostrarSinConexion()
                    return@observe
                }

                if (
                    !cargandoMedicos &&
                    !registrando &&
                    listaMedicos.isEmpty()
                ) {
                    cargarMedicosDesdeApi()
                } else {
                    actualizarEstadoFormulario()
                }
            }
    }

    private fun cargarMedicosDesdeApi() {
        if (
            cargandoMedicos ||
            registrando ||
            !vistaDisponible()
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarSinConexion()
            return
        }

        cargandoMedicos =
            true

        listaMedicos =
            emptyList()

        medicoSeleccionado =
            null

        val solicitudActual =
            ++idSolicitudMedicos

        configurarMedicosVacios(
            "Cargando médicos..."
        )

        actualizarEstadoFormulario()

        medicoRepository.listarMedicosApi(
            callback = callback@{ resultado ->

                if (
                    solicitudActual != idSolicitudMedicos ||
                    !vistaDisponible()
                ) {
                    return@callback
                }

                cargandoMedicos =
                    false

                when (resultado) {
                    is ResultadoCargaMedicosAdminApi.Exito -> {
                        listaMedicos =
                            resultado.medicos

                        if (listaMedicos.isEmpty()) {
                            configurarMedicosVacios(
                                "No hay médicos registrados"
                            )

                            actualizarEstadoFormulario()

                            Toast.makeText(
                                requireContext(),
                                "Primero debe registrar un médico.",
                                Toast.LENGTH_LONG
                            ).show()

                            return@callback
                        }

                        configurarListaMedicos()

                        actualizarEstadoFormulario()
                    }

                    is ResultadoCargaMedicosAdminApi.SinConexion -> {
                        mostrarSinConexion()

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaMedicosAdminApi.SesionExpirada -> {
                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoCargaMedicosAdminApi.SinPermiso -> {
                        listaMedicos =
                            emptyList()

                        configurarMedicosVacios(
                            "Acceso denegado"
                        )

                        actualizarEstadoFormulario()

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaMedicosAdminApi.Error -> {
                        listaMedicos =
                            emptyList()

                        configurarMedicosVacios(
                            "No se pudieron cargar los médicos"
                        )

                        actualizarEstadoFormulario()

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    private fun configurarListaMedicos() {
        medicoSeleccionado =
            null

        acMedico.setText(
            "",
            false
        )

        val medicosParaMostrar =
            listaMedicos.map {
                    medico ->

                obtenerTextoMedico(
                    medico
                )
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                medicosParaMostrar
            )

        acMedico.setAdapter(
            adapter
        )

        acMedico.threshold =
            0

        acMedico.setOnClickListener {
            if (acMedico.isEnabled) {
                acMedico.showDropDown()
            }
        }

        acMedico.setOnFocusChangeListener {
                _, tieneFoco ->

            if (
                tieneFoco &&
                acMedico.isEnabled
            ) {
                acMedico.showDropDown()
            }
        }

        acMedico.setOnItemClickListener {
                _, _, posicion, _ ->

            medicoSeleccionado =
                listaMedicos
                    .getOrNull(
                        posicion
                    )
        }
    }

    private fun configurarMedicosVacios(
        mensaje: String
    ) {
        if (!vistaDisponible()) {
            return
        }

        medicoSeleccionado =
            null

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                listOf(mensaje)
            )

        acMedico.setAdapter(
            adapter
        )

        acMedico.setText(
            mensaje,
            false
        )

        acMedico.isEnabled =
            false
    }

    private fun configurarSelectorFecha() {
        etFecha.setOnClickListener {
            if (!etFecha.isEnabled) {
                return@setOnClickListener
            }

            val calendarioActual =
                Calendar.getInstance()

            val fechaInicial =
                fechaSeleccionadaMillis
                    ?.let {
                        Calendar.getInstance()
                            .apply {
                                timeInMillis =
                                    it
                            }
                    }
                    ?: calendarioActual

            val datePicker =
                DatePickerDialog(
                    requireContext(),
                    {
                            _, anio, mes, dia ->

                        val fechaSeleccionada =
                            Calendar.getInstance()
                                .apply {
                                    set(
                                        Calendar.YEAR,
                                        anio
                                    )

                                    set(
                                        Calendar.MONTH,
                                        mes
                                    )

                                    set(
                                        Calendar.DAY_OF_MONTH,
                                        dia
                                    )

                                    set(
                                        Calendar.HOUR_OF_DAY,
                                        0
                                    )

                                    set(
                                        Calendar.MINUTE,
                                        0
                                    )

                                    set(
                                        Calendar.SECOND,
                                        0
                                    )

                                    set(
                                        Calendar.MILLISECOND,
                                        0
                                    )
                                }

                        fechaSeleccionadaMillis =
                            fechaSeleccionada.timeInMillis

                        val fechaFormateada =
                            String.format(
                                Locale.getDefault(),
                                "%02d/%02d/%04d",
                                dia,
                                mes + 1,
                                anio
                            )

                        etFecha.setText(
                            fechaFormateada
                        )
                    },
                    fechaInicial.get(
                        Calendar.YEAR
                    ),
                    fechaInicial.get(
                        Calendar.MONTH
                    ),
                    fechaInicial.get(
                        Calendar.DAY_OF_MONTH
                    )
                )

            val inicioHoy =
                Calendar.getInstance()
                    .apply {
                        set(
                            Calendar.HOUR_OF_DAY,
                            0
                        )

                        set(
                            Calendar.MINUTE,
                            0
                        )

                        set(
                            Calendar.SECOND,
                            0
                        )

                        set(
                            Calendar.MILLISECOND,
                            0
                        )
                    }

            datePicker.datePicker.minDate =
                inicioHoy.timeInMillis

            datePicker.show()
        }
    }

    private fun configurarSelectorHoraInicio() {
        etHoraInicio.setOnClickListener {
            if (!etHoraInicio.isEnabled) {
                return@setOnClickListener
            }

            mostrarSelectorHora {
                    horaSeleccionada ->

                etHoraInicio.setText(
                    horaSeleccionada
                )
            }
        }
    }

    private fun configurarSelectorHoraFin() {
        etHoraFin.setOnClickListener {
            if (!etHoraFin.isEnabled) {
                return@setOnClickListener
            }

            mostrarSelectorHora {
                    horaSeleccionada ->

                etHoraFin.setText(
                    horaSeleccionada
                )
            }
        }
    }

    private fun mostrarSelectorHora(
        alSeleccionar: (String) -> Unit
    ) {
        val calendar =
            Calendar.getInstance()

        val timePicker =
            TimePickerDialog(
                requireContext(),
                {
                        _, hora, minuto ->

                    val horaFormateada =
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hora,
                            minuto
                        )

                    alSeleccionar(
                        horaFormateada
                    )
                },
                calendar.get(
                    Calendar.HOUR_OF_DAY
                ),
                calendar.get(
                    Calendar.MINUTE
                ),
                true
            )

        timePicker.show()
    }

    private fun registrarHorario() {
        if (
            registrando ||
            cargandoMedicos
        ) {
            return
        }

        if (!NetworkMonitor.hayInternet()) {
            mostrarSinConexion()

            Toast.makeText(
                requireContext(),
                "Necesitas conexión a Internet para registrar un horario.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val medico =
            medicoSeleccionado

        val fecha =
            etFecha.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val horaInicio =
            etHoraInicio.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val horaFin =
            etHoraFin.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (medico == null) {
            Toast.makeText(
                requireContext(),
                "Seleccione un médico de la lista.",
                Toast.LENGTH_SHORT
            ).show()

            acMedico.requestFocus()
            acMedico.showDropDown()

            return
        }

        if (
            acMedico.text
                ?.toString()
                ?.trim()
                .orEmpty() !=
            obtenerTextoMedico(medico)
        ) {
            medicoSeleccionado =
                null

            Toast.makeText(
                requireContext(),
                "Seleccione nuevamente un médico válido.",
                Toast.LENGTH_SHORT
            ).show()

            acMedico.requestFocus()
            acMedico.showDropDown()

            return
        }

        if (
            fecha.isBlank() ||
            horaInicio.isBlank() ||
            horaFin.isBlank()
        ) {
            Toast.makeText(
                requireContext(),
                "Por favor, complete todos los campos.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val fechaMillis =
            fechaSeleccionadaMillis

        if (fechaMillis == null) {
            Toast.makeText(
                requireContext(),
                "Seleccione nuevamente una fecha válida.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val minutosInicio =
            convertirHoraAMinutos(
                horaInicio
            )

        val minutosFin =
            convertirHoraAMinutos(
                horaFin
            )

        if (
            minutosInicio < 0 ||
            minutosFin < 0
        ) {
            Toast.makeText(
                requireContext(),
                "Seleccione horas válidas.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (minutosFin <= minutosInicio) {
            Toast.makeText(
                requireContext(),
                "La hora final debe ser posterior a la hora inicial.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (
            esFechaHoraPasada(
                fechaMillis = fechaMillis,
                minutosInicio = minutosInicio
            )
        ) {
            Toast.makeText(
                requireContext(),
                "La fecha y hora inicial deben ser posteriores al momento actual.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val fechaHoraTexto =
            "$fecha - $horaInicio a $horaFin"

        registrando =
            true

        val solicitudActual =
            ++idSolicitudRegistro

        actualizarEstadoFormulario()

        horarioRepository.registrarHorarioApi(
            idMedico = medico.id,
            fechaHoraTexto =
                fechaHoraTexto,
            callback = callback@{
                    resultado ->

                if (
                    solicitudActual != idSolicitudRegistro ||
                    !vistaDisponible()
                ) {
                    return@callback
                }

                registrando =
                    false

                actualizarEstadoFormulario()

                when (resultado) {
                    is ResultadoRegistroHorarioAdminApi.Exito -> {
                        Toast.makeText(
                            requireContext(),
                            "${resultado.mensaje} para ${medico.nombre}",
                            Toast.LENGTH_LONG
                        ).show()

                        regresar()
                    }

                    is ResultadoRegistroHorarioAdminApi.Duplicado -> {
                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoRegistroHorarioAdminApi.SinConexion -> {
                        mostrarSinConexion()

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoRegistroHorarioAdminApi.SinPermiso -> {
                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoRegistroHorarioAdminApi.SesionExpirada -> {
                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoRegistroHorarioAdminApi.Error -> {
                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    private fun actualizarEstadoFormulario() {
        if (!vistaDisponible()) {
            return
        }

        val habilitado =
            NetworkMonitor.hayInternet() &&
                    !cargandoMedicos &&
                    !registrando &&
                    listaMedicos.isNotEmpty()

        acMedico.isEnabled =
            habilitado

        etFecha.isEnabled =
            habilitado

        etHoraInicio.isEnabled =
            habilitado

        etHoraFin.isEnabled =
            habilitado

        btnGuardar.isEnabled =
            habilitado

        btnCancelar.isEnabled =
            !registrando

        btnGuardar.text =
            when {
                registrando ->
                    "REGISTRANDO TURNO..."

                cargandoMedicos ->
                    "CARGANDO MÉDICOS..."

                else ->
                    "GUARDAR HORARIO"
            }
    }

    private fun mostrarSinConexion() {
        idSolicitudMedicos++

        cargandoMedicos =
            false

        listaMedicos =
            emptyList()

        medicoSeleccionado =
            null

        if (!vistaDisponible()) {
            return
        }

        configurarMedicosVacios(
            "Sin conexión a Internet"
        )

        actualizarEstadoFormulario()
    }

    private fun esFechaHoraPasada(
        fechaMillis: Long,
        minutosInicio: Int
    ): Boolean {
        val fechaHoraInicio =
            Calendar.getInstance()
                .apply {
                    timeInMillis =
                        fechaMillis

                    set(
                        Calendar.HOUR_OF_DAY,
                        minutosInicio / 60
                    )

                    set(
                        Calendar.MINUTE,
                        minutosInicio % 60
                    )

                    set(
                        Calendar.SECOND,
                        0
                    )

                    set(
                        Calendar.MILLISECOND,
                        0
                    )
                }

        return fechaHoraInicio.timeInMillis <=
                System.currentTimeMillis()
    }

    private fun obtenerTextoMedico(
        medico: MedicoAdmin
    ): String {
        return "${medico.nombre} (${medico.especialidad})"
    }

    private fun convertirHoraAMinutos(
        hora: String
    ): Int {
        val partes =
            hora.split(":")

        if (partes.size != 2) {
            return -1
        }

        val horas =
            partes[0].toIntOrNull()
                ?: return -1

        val minutos =
            partes[1].toIntOrNull()
                ?: return -1

        if (
            horas !in 0..23 ||
            minutos !in 0..59
        ) {
            return -1
        }

        return horas * 60 +
                minutos
    }

    private fun regresar() {
        parentFragmentManager
            .popBackStack()
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

        requireActivity()
            .finish()
    }

    private fun vistaDisponible(): Boolean {
        return isAdded &&
                view != null
    }

    override fun onDestroyView() {

        idSolicitudMedicos++
        idSolicitudRegistro++

        cargandoMedicos =
            false

        registrando =
            false

        super.onDestroyView()
    }
}