package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SeleccionarMedicoHorarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_medico_horario)

        val acMedico = findViewById<AutoCompleteTextView>(R.id.acMedicoReserva)
        val acHorario = findViewById<AutoCompleteTextView>(R.id.acHorarioReserva)
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmarReservaFinal)

        val medicos = arrayOf("Dr. Bryant Yacila", "Dra. Abigail Valdez", "Dra. Nilda Rojas")
        val horarios = arrayOf("Lunes 08 de Junio - 08:30 AM", "Miércoles 10 de Junio - 10:15 AM", "Viernes 12 de Junio - 04:00 PM")

        acMedico.setAdapter(ArrayAdapter(this, R.layout.spinner_perfil_item, medicos))
        acHorario.setAdapter(ArrayAdapter(this, R.layout.spinner_perfil_item, horarios))

        acMedico.setOnClickListener { acMedico.showDropDown() }
        acHorario.setOnClickListener { acHorario.showDropDown() }

        btnConfirmar.setOnClickListener {
            Toast.makeText(this, "Cita registrada con éxito", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}