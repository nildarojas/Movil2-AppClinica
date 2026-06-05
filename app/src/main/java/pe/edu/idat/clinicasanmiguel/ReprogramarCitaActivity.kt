package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ReprogramarCitaActivity : AppCompatActivity() {

    private lateinit var spnMedicos: Spinner
    private lateinit var spnHorarios: Spinner
    private lateinit var btnConfirmar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reprogramar_cita)

        spnMedicos = findViewById(R.id.spnMedicosReprogramar)
        spnHorarios = findViewById(R.id.spnHorariosReprogramar)
        btnConfirmar = findViewById(R.id.btnConfirmarReprogramacion)

        cargarDatosLocales()

        btnConfirmar.setOnClickListener {
            Toast.makeText(this, "Cita modificada y reprogramada con éxito", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun cargarDatosLocales() {
        val medicos = listOf(
            "Dr. Carlos Mendoza (Cardiología)",
            "Dra. Ana Torres (Pediatría)",
            "Dr. Luis Gómez (Medicina General)"
        )

        val horarios = listOf(
            "Lunes 08 de Junio - 09:00 AM",
            "Lunes 08 de Junio - 11:30 AM",
            "Martes 09 de Junio - 04:00 PM"
        )

        val adapterMedicos = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, medicos)
        spnMedicos.adapter = adapterMedicos

        val adapterHorarios = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, horarios)
        spnHorarios.adapter = adapterHorarios
    }
}