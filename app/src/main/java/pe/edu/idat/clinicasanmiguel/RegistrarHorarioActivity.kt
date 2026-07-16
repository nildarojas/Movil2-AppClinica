package pe.edu.idat.clinicasanmiguel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import pe.edu.idat.clinicasanmiguel.entity.MedicoAdmin
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository
import java.util.Calendar
import java.util.Locale

class RegistrarHorarioActivity : AppCompatActivity() {

    private lateinit var acMedico: AutoCompleteTextView
    private lateinit var etFecha: EditText
    private lateinit var etHoraInicio: EditText
    private lateinit var etHoraFin: EditText
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private lateinit var adminRepository: AdminRepository
    private var listaMedicos: List<MedicoAdmin> = emptyList()
    private var medicoSeleccionado: MedicoAdmin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_horario)

        acMedico = findViewById(R.id.acMedico)
        etFecha = findViewById(R.id.etFecha)
        etHoraInicio = findViewById(R.id.etHoraInicio)
        etHoraFin = findViewById(R.id.etHoraFin)
        btnGuardar = findViewById(R.id.btnGuardarHorario)
        btnCancelar = findViewById(R.id.btnCancelarHorario)

        adminRepository = AdminRepository(this)

        cargarMedicosDesdeSQLite()
        configurarSelectorFecha()
        configurarSelectorHoraInicio()
        configurarSelectorHoraFin()

        btnGuardar.setOnClickListener {
            registrarHorario()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun cargarMedicosDesdeSQLite() {

        listaMedicos =
            adminRepository.obtenerMedicos()

        if (listaMedicos.isEmpty()) {

            btnGuardar.isEnabled = false

            Toast.makeText(
                this,
                "No existen médicos registrados",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val medicosParaMostrar =
            listaMedicos.map { medico ->
                obtenerTextoMedico(medico)
            }

        val adapterMedico = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            medicosParaMostrar
        )

        acMedico.setAdapter(adapterMedico)
        acMedico.threshold = 0

        acMedico.setOnClickListener {
            acMedico.showDropDown()
        }

        acMedico.setOnFocusChangeListener { _, tieneFoco ->
            if (tieneFoco) {
                acMedico.showDropDown()
            }
        }

        acMedico.setOnItemClickListener { _, _, posicion, _ ->

            medicoSeleccionado =
                listaMedicos[posicion]
        }

        btnGuardar.isEnabled = true
    }

    private fun configurarSelectorFecha() {

        etFecha.setOnClickListener {

            val calendar = Calendar.getInstance()

            val year =
                calendar.get(Calendar.YEAR)

            val month =
                calendar.get(Calendar.MONTH)

            val day =
                calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->

                    val fechaFormateada =
                        String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                        )

                    etFecha.setText(fechaFormateada)
                },
                year,
                month,
                day
            )

            datePicker.show()
        }
    }

    private fun configurarSelectorHoraInicio() {

        etHoraInicio.setOnClickListener {

            mostrarSelectorHora { horaSeleccionada ->
                etHoraInicio.setText(horaSeleccionada)
            }
        }
    }

    private fun configurarSelectorHoraFin() {

        etHoraFin.setOnClickListener {

            mostrarSelectorHora { horaSeleccionada ->
                etHoraFin.setText(horaSeleccionada)
            }
        }
    }

    private fun mostrarSelectorHora(
        alSeleccionar: (String) -> Unit
    ) {

        val calendar = Calendar.getInstance()

        val hour =
            calendar.get(Calendar.HOUR_OF_DAY)

        val minute =
            calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->

                val horaFormateada =
                    String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        selectedHour,
                        selectedMinute
                    )

                alSeleccionar(horaFormateada)
            },
            hour,
            minute,
            true
        )

        timePicker.show()
    }

    private fun registrarHorario() {

        val medico =
            medicoSeleccionado

        val fecha =
            etFecha.text
                .toString()
                .trim()

        val horaInicio =
            etHoraInicio.text
                .toString()
                .trim()

        val horaFin =
            etHoraFin.text
                .toString()
                .trim()

        if (medico == null) {

            Toast.makeText(
                this,
                "Seleccione un médico de la lista",
                Toast.LENGTH_SHORT
            ).show()

            acMedico.requestFocus()
            acMedico.showDropDown()

            return
        }
        if (
            acMedico.text
                .toString()
                .trim() != obtenerTextoMedico(medico)
        ) {

            medicoSeleccionado = null

            Toast.makeText(
                this,
                "Seleccione nuevamente un médico válido",
                Toast.LENGTH_SHORT
            ).show()

            acMedico.requestFocus()
            acMedico.showDropDown()

            return
        }

        if (
            fecha.isEmpty() ||
            horaInicio.isEmpty() ||
            horaFin.isEmpty()
        ) {

            Toast.makeText(
                this,
                "Por favor, complete todos los campos",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (
            convertirHoraAMinutos(horaFin) <=
            convertirHoraAMinutos(horaInicio)
        ) {

            Toast.makeText(
                this,
                "La hora final debe ser posterior a la hora inicial",
                Toast.LENGTH_LONG
            ).show()

            return
        }
        val fechaHoraTexto =
            "$fecha - $horaInicio a $horaFin"

        btnGuardar.isEnabled = false
        btnGuardar.text = "REGISTRANDO TURNO..."

        val resultado =
            adminRepository.registrarHorario(
                idMedico = medico.id,
                fechaHoraTexto = fechaHoraTexto
            )

        if (resultado > 0) {

            Toast.makeText(
                this,
                "Horario registrado para ${medico.nombre}",
                Toast.LENGTH_LONG
            ).show()

            setResult(RESULT_OK)

            finish()

        } else {

            btnGuardar.isEnabled = true
            btnGuardar.text = "GUARDAR HORARIO"

            Toast.makeText(
                this,
                "No se pudo registrar. Verifique que el horario no esté duplicado",
                Toast.LENGTH_LONG
            ).show()
        }
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
            return 0
        }

        val horas =
            partes[0].toIntOrNull() ?: 0

        val minutos =
            partes[1].toIntOrNull() ?: 0

        return horas * 60 + minutos
    }
}