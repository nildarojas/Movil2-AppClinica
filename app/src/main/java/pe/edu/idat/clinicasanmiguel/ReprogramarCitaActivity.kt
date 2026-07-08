package pe.edu.idat.clinicasanmiguel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class ReprogramarCitaActivity : AppCompatActivity() {

    private lateinit var citaRepository: CitaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reprogramar_cita)

        citaRepository = CitaRepository(this)

        val tvEspecialidadAnt = findViewById<TextView>(R.id.tvEspecialidadAnterior)
        val tvMedicoAnt = findViewById<TextView>(R.id.tvMedicoAnterior)
        val tvHorarioAnt = findViewById<TextView>(R.id.tvHorarioAnterior)
        val acHorario = findViewById<AutoCompleteTextView>(R.id.acNuevoHorario)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarReprogramacion)

        val idCita = intent.getIntExtra("id_cita", -1)
        val posicion = intent.getIntExtra("posicion", -1)
        val especialidadOriginal = intent.getStringExtra("especialidad") ?: ""
        val medicoOriginal = intent.getStringExtra("medico") ?: ""
        val horarioOriginal = intent.getStringExtra("fecha_hora") ?: ""

        tvEspecialidadAnt.text = "Especialidad: $especialidadOriginal"
        tvMedicoAnt.text = "Médico: $medicoOriginal"
        tvHorarioAnt.text = "Horario actual: $horarioOriginal"

        val preferencias = getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
        val idPacienteLogueado = preferencias.getInt("ID_USUARIO", -1)
        val idMedicoAsignado = citaRepository.obtenerMedicoPorCita(idCita)

        if (idPacienteLogueado != -1 && idMedicoAsignado != -1) {
            val horariosConEstado = citaRepository.obtenerHorariosConEstado(idPacienteLogueado, idMedicoAsignado, horarioOriginal)
            acHorario.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, horariosConEstado))
        }

        btnConfirmar.setOnClickListener {
            val horarioSeleccionadoRaw = acHorario.text.toString()

            if (horarioSeleccionadoRaw.isEmpty()) {
                Toast.makeText(this, "Por favor, seleccione la nueva fecha y horario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (horarioSeleccionadoRaw.contains("(Ocupado por ti)")) {
                Toast.makeText(this, "No puedes seleccionar un horario donde ya tienes otra cita", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (horarioSeleccionadoRaw.contains("(Médico ocupado en este horario)")) {
                Toast.makeText(this, "Este horario no está disponible para el médico seleccionado", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (idCita != -1) {
                AlertDialog.Builder(this)
                    .setTitle("¿Desea reprogramar esta cita?")
                    .setMessage("Su cita actual será marcada como REPROGRAMADA y se generará una nueva cita PENDIENTE con la fecha y horario seleccionados.")
                    .setPositiveButton("Confirmar") { _, _ ->
                        val exitoTransaccion = citaRepository.reprogramarCitaTransaccional(idCita, horarioSeleccionadoRaw)

                        if (exitoTransaccion) {
                            val resultIntent = Intent()
                            resultIntent.putExtra("posicion", posicion)
                            setResult(Activity.RESULT_OK, resultIntent)
                            Toast.makeText(this, "Cita archivada. Nueva cita registrada como PENDIENTE", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error al procesar la transacción en SQLite", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }
}