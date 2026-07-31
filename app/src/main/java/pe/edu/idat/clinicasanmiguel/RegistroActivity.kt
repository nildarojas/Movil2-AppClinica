package pe.edu.idat.clinicasanmiguel

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.entity.Usuario
import pe.edu.idat.clinicasanmiguel.repository.ResultadoRegistroApi
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository
import java.util.Calendar

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellidos: EditText
    private lateinit var etDni: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etFechaNacimiento: EditText
    private lateinit var rgGenero: RadioGroup
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPassword2: EditText
    private lateinit var cbTerminos: CheckBox
    private lateinit var btnRegistrar: MaterialButton
    private lateinit var tvIrLogin: TextView
    private lateinit var usuarioRepository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        usuarioRepository = UsuarioRepository(this)

        etNombre = findViewById(R.id.etNombre)
        etApellidos = findViewById(R.id.etApellidos)
        etDni = findViewById(R.id.etDni)
        etTelefono = findViewById(R.id.etTelefono)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        rgGenero = findViewById(R.id.rgGenero)
        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        etPassword2 = findViewById(R.id.etPassword2)
        cbTerminos = findViewById(R.id.cbTerminos)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvIrLogin = findViewById(R.id.tvIrLogin)

        etFechaNacimiento.setOnClickListener {
            mostrarDatePicker()
        }

        tvIrLogin.setOnClickListener {
            abrirLogin()
        }

        btnRegistrar.setOnClickListener {
            ejecutarRegistroHibrido()
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()

        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->

                val fecha = String.format(
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )

                etFechaNacimiento.setText(fecha)
            },
            year,
            month,
            day
        )

        datePicker.show()
    }

    private fun ejecutarRegistroHibrido() {
        val nombre = etNombre.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val apellido = etApellidos.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val dni = etDni.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val telefono = etTelefono.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val fechaNacimiento = etFechaNacimiento.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val correo = etCorreo.text
            ?.toString()
            ?.trim()
            ?.lowercase()
            .orEmpty()

        val password = etPassword.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val password2 = etPassword2.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val genero = when (rgGenero.checkedRadioButtonId) {
            R.id.rbFemenino -> "Femenino"
            R.id.rbMasculino -> "Masculino"
            R.id.rbOtro -> "Otro"
            else -> ""
        }

        if (
            nombre.isEmpty() ||
            apellido.isEmpty() ||
            dni.isEmpty() ||
            telefono.isEmpty() ||
            fechaNacimiento.isEmpty() ||
            genero.isEmpty() ||
            correo.isEmpty() ||
            password.isEmpty() ||
            password2.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Completa todos los campos",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(
                this,
                "Correo inválido",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (password.length < 6) {
            Toast.makeText(
                this,
                "La contraseña debe tener al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (password != password2) {
            Toast.makeText(
                this,
                "Las contraseñas no coinciden",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (!cbTerminos.isChecked) {
            Toast.makeText(
                this,
                "Debe aceptar los términos y condiciones",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val nuevoUsuario = Usuario(
            dni = dni,
            nombre = nombre,
            apellido = apellido,
            correo = correo,
            password = password,
            telefono = telefono,
            fechaNacimiento = fechaNacimiento,
            genero = genero,
            rol = "PACIENTE"
        )

        mostrarCargando(true)

        usuarioRepository.registrarUsuarioApi(
            usuario = nuevoUsuario
        ) { resultado ->

            if (isFinishing || isDestroyed) {
                return@registrarUsuarioApi
            }

            when (resultado) {

                is ResultadoRegistroApi.Exito -> {
                    mostrarCargando(false)

                    Toast.makeText(
                        this,
                        "${resultado.mensaje}. ID Azure: ${resultado.datos.idUsuarioApi}",
                        Toast.LENGTH_LONG
                    ).show()

                    abrirLogin()
                }

                is ResultadoRegistroApi.Duplicado -> {
                    mostrarCargando(false)

                    Toast.makeText(
                        this,
                        resultado.mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }

                is ResultadoRegistroApi.RegistroLocal -> {
                    mostrarCargando(false)

                    Toast.makeText(
                        this,
                        resultado.mensaje,
                        Toast.LENGTH_LONG
                    ).show()

                    abrirLogin()
                }

                is ResultadoRegistroApi.Error -> {
                    mostrarCargando(false)

                    Toast.makeText(
                        this,
                        resultado.mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        btnRegistrar.isEnabled = !cargando
        tvIrLogin.isEnabled = !cargando

        btnRegistrar.text = if (cargando) {
            "REGISTRANDO..."
        } else {
            "REGISTRAR"
        }
    }

    private fun abrirLogin() {
        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        startActivity(intent)
        finish()
    }
}