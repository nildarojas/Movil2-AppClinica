package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.repository.AdminRepository

class RegistrarEspecialidadActivity : AppCompatActivity() {

    private lateinit var etNombreEspecialidad: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private lateinit var adminRepository: AdminRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_especialidad)

        etNombreEspecialidad =
            findViewById(R.id.etNombreEspecialidad)

        btnGuardar =
            findViewById(R.id.btnGuardarEspecialidad)

        btnCancelar =
            findViewById(R.id.btnCancelarEspecialidad)

        adminRepository = AdminRepository(this)

        btnGuardar.setOnClickListener {

            val nombreEspecialidad =
                etNombreEspecialidad
                    .text
                    .toString()
                    .trim()

            if (nombreEspecialidad.isEmpty()) {

                etNombreEspecialidad.error =
                    "Ingrese el nombre de la especialidad"

                etNombreEspecialidad.requestFocus()

                return@setOnClickListener
            }

            val resultado =
                adminRepository.registrarEspecialidad(
                    nombreEspecialidad
                )

            when {

                resultado > 0 -> {

                    Toast.makeText(
                        this,
                        "Especialidad registrada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(RESULT_OK)
                    finish()
                }

                resultado == AdminRepository.RESULTADO_DUPLICADO -> {

                    etNombreEspecialidad.error =
                        "Esta especialidad ya está registrada"

                    etNombreEspecialidad.requestFocus()
                }

                else -> {

                    Toast.makeText(
                        this,
                        "No se pudo registrar la especialidad",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }
}