package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegistrarDoctorActivity : AppCompatActivity() {

    private var listaEspecialidadesLocal: List<String> = listOf()

    private lateinit var spnEspecialidades: Spinner
    private lateinit var btnGuardar: MaterialButton
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var pb: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_doctor)

        spnEspecialidades = findViewById(R.id.spnEspecialidades)
        btnGuardar = findViewById(R.id.btnGuardarDoc)
        etNombre = findViewById(R.id.etNombreDoc)
        etApellido = findViewById(R.id.etApellidoDoc)
        pb = findViewById(R.id.pbCargandoDoc)

        cargarEspecialidadesLocales()

        btnGuardar.setOnClickListener {
            ejecutarRegistroDoctorLocal()
        }
    }

    private fun cargarEspecialidadesLocales() {
        listaEspecialidadesLocal = listOf(
            "Cardiología",
            "Pediatría",
            "Dermatología",
            "Neurología",
            "Medicina General"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listaEspecialidadesLocal
        )
        spnEspecialidades.adapter = adapter
    }

    private fun ejecutarRegistroDoctorLocal() {
        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()

        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val pos = spnEspecialidades.selectedItemPosition
        if (pos < 0 || listaEspecialidadesLocal.isEmpty()) {
            Toast.makeText(this, "Seleccione una especialidad", Toast.LENGTH_SHORT).show()
            return
        }

        pb.visibility = View.VISIBLE
        btnGuardar.isEnabled = false
        btnGuardar.text = "Registrando..."

        pb.postDelayed({
            pb.visibility = View.GONE
            btnGuardar.isEnabled = true
            btnGuardar.text = "REGISTRAR DOCTOR"

            Toast.makeText(this, "Médico registrado correctamente de forma local", Toast.LENGTH_LONG).show()
            finish()
        }, 1500)
    }
}