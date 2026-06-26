package pe.edu.idat.clinicasanmiguel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RegistrarHorarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_horario)

        val acMedico = findViewById<android.widget.AutoCompleteTextView>(R.id.acMedico)
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etHoraInicio = findViewById<EditText>(R.id.etHoraInicio)
        val etHoraFin = findViewById<EditText>(R.id.etHoraFin)
        val btnGuardar = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGuardarHorario)
        val btnCancelar = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelarHorario)

        val medicosSimulados = listOf("Dr. Carlos Mendoza (Cardiología)", "Dra. Ana López (Pediatría)", "Dr. Luis Gómez (Gastroenterología)")
        val adapterMedico = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, medicosSimulados)
        acMedico.setAdapter(adapterMedico)
        acMedico.setOnClickListener { acMedico.showDropDown() }

        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->

                val fechaFormateada = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                etFecha.setText(fechaFormateada)
            }, year, month, day)

            datePicker.show()
        }

        etHoraInicio.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val horaFormateada = String.format("%02d:%02d", selectedHour, selectedMinute)
                etHoraInicio.setText(horaFormateada)
            }, hour, minute, true)

            timePicker.show()
        }

        etHoraFin.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val horaFormateada = String.format("%02d:%02d", selectedHour, selectedMinute)
                etHoraFin.setText(horaFormateada)
            }, hour, minute, true)

            timePicker.show()
        }

        btnGuardar.setOnClickListener {
            if (acMedico.text.toString().isEmpty() || etFecha.text.toString().isEmpty() ||
                etHoraInicio.text.toString().isEmpty() || etHoraFin.text.toString().isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            btnGuardar.text = "Registrando turno..."

            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(this, "Horario registrado exitosamente en caliente", Toast.LENGTH_LONG).show()
                finish()
            }, 1500)
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}