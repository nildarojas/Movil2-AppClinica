package pe.edu.idat.clinicasanmiguel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class ReprogramarCitaActivity : AppCompatActivity() {

    private lateinit var citaRepository: CitaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reprogramar_cita)

        citaRepository = CitaRepository(this)

        val acMedico = findViewById<AutoCompleteTextView>(R.id.acNuevoMedico)
        val acHorario = findViewById<AutoCompleteTextView>(R.id.acNuevoHorario)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarReprogramacion)
        val medicos = listOf("Dr. Bryant Yacila (Cardiología)", "Dra. Abigail Valdez (Pediatría)")
        val horarios = listOf("Lunes 08 de Junio - 08:30 AM", "Miércoles 10 de Junio - 10:15 AM", "Viernes 12 de Junio - 04:00 PM")

        acMedico.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, medicos))
        acHorario.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, horarios))
        val idCita = intent.getIntExtra("id_cita", -1)
        val posicion = intent.getIntExtra("posicion", -1)

        btnConfirmar.setOnClickListener {
            val medicoSeleccionado = acMedico.text.toString()
            val horarioSeleccionado = acHorario.text.toString()

            if (medicoSeleccionado.isEmpty() || horarioSeleccionado.isEmpty()) {
                Toast.makeText(this, "Por favor, seleccione médico y horario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idCita != -1) {
                val filasAfectadas = citaRepository.reprogramarCita(idCita, horarioSeleccionado)

                if (filasAfectadas > 0) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("posicion", posicion)
                    setResult(Activity.RESULT_OK, resultIntent)

                    Toast.makeText(this, "Cita N° $idCita reprogramada en SQLite con éxito", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar la persistencia de datos", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error: Identificador de cita inválido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}