package pe.edu.idat.clinicasanmiguel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReprogramarCitaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reprogramar_cita)

        val acMedico = findViewById<AutoCompleteTextView>(R.id.acNuevoMedico)
        val acHorario = findViewById<AutoCompleteTextView>(R.id.acNuevoHorario)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarReprogramacion)

        val medicos = listOf("Dr. Bryant Yacila (Cardiología)", "Dra. Abigail Valdez (Pediatría)")
        val horarios = listOf("2026-06-26 | 09:00 AM", "2026-06-27 | 11:30 AM")

        acMedico.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, medicos))
        acHorario.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, horarios))

        val posicion = intent.getIntExtra("posicion", -1)

        btnConfirmar.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("posicion", posicion)
            setResult(Activity.RESULT_OK, resultIntent)
            Toast.makeText(this, "Cita reprogramada con éxito en caliente", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}