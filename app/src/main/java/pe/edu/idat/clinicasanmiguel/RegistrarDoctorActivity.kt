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
import pe.edu.idat.clinicasanmiguel.entity.Especialidad
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository

class RegistrarDoctorActivity : AppCompatActivity() {

    private lateinit var spnEspecialidades: Spinner
    private lateinit var btnGuardar: MaterialButton
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var pbCargando: ProgressBar

    private lateinit var adminRepository: AdminRepository
    private var listaEspecialidades: List<Especialidad> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_doctor)

        spnEspecialidades = findViewById(R.id.spnEspecialidades)
        btnGuardar = findViewById(R.id.btnGuardarDoc)
        etNombre = findViewById(R.id.etNombreDoc)
        etApellido = findViewById(R.id.etApellidoDoc)
        pbCargando = findViewById(R.id.pbCargandoDoc)

        adminRepository = AdminRepository(this)

        cargarEspecialidadesDesdeSQLite()

        btnGuardar.setOnClickListener {
            registrarDoctor()
        }
    }

    private fun cargarEspecialidadesDesdeSQLite() {

        listaEspecialidades =
            adminRepository.obtenerEspecialidades()

        if (listaEspecialidades.isEmpty()) {

            btnGuardar.isEnabled = false

            Toast.makeText(
                this,
                "No existen especialidades registradas",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val nombresEspecialidades =
            listaEspecialidades.map { especialidad ->
                especialidad.nombre
            }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombresEspecialidades
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spnEspecialidades.adapter = adapter
        btnGuardar.isEnabled = true
    }

    private fun registrarDoctor() {

        val nombre =
            etNombre.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val apellido =
            etApellido.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (nombre.isEmpty()) {

            etNombre.error = "Ingrese los nombres del médico"
            etNombre.requestFocus()

            return
        }

        if (apellido.isEmpty()) {

            etApellido.error = "Ingrese los apellidos del médico"
            etApellido.requestFocus()

            return
        }

        if (listaEspecialidades.isEmpty()) {

            Toast.makeText(
                this,
                "Primero debe registrar una especialidad",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val posicionSeleccionada =
            spnEspecialidades.selectedItemPosition

        if (
            posicionSeleccionada < 0 ||
            posicionSeleccionada >= listaEspecialidades.size
        ) {

            Toast.makeText(
                this,
                "Seleccione una especialidad",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val especialidadSeleccionada =
            listaEspecialidades[posicionSeleccionada]

        val nombreCompleto =
            "$nombre $apellido"
                .trim()
                .replace(Regex("\\s+"), " ")

        mostrarCargando(true)

        val resultado =
            adminRepository.registrarMedico(
                nombreCompleto = nombreCompleto,
                idEspecialidad = especialidadSeleccionada.id
            )

        mostrarCargando(false)

        if (resultado > 0) {

            Toast.makeText(
                this,
                "Médico registrado en ${especialidadSeleccionada.nombre}",
                Toast.LENGTH_LONG
            ).show()
            setResult(RESULT_OK)

            finish()

        } else {

            Toast.makeText(
                this,
                "No se pudo registrar el médico",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun mostrarCargando(cargando: Boolean) {

        pbCargando.visibility =
            if (cargando) View.VISIBLE else View.GONE

        btnGuardar.isEnabled = !cargando

        btnGuardar.text =
            if (cargando) {
                "REGISTRANDO..."
            } else {
                "REGISTRAR DOCTOR"
            }
    }
}