package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MiPerfilActivity : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvApellido: TextView
    private lateinit var tvDni: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvFechaNacimiento: TextView
    private lateinit var tvGenero: TextView
    private lateinit var btnRegresar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        tvNombre = findViewById(R.id.tvNombre)
        tvApellido = findViewById(R.id.tvApellido)
        tvDni = findViewById(R.id.tvDni)
        tvTelefono = findViewById(R.id.tvTelefono)
        tvCorreo = findViewById(R.id.tvCorreo)
        tvFechaNacimiento = findViewById(R.id.tvFechaNacimiento)
        tvGenero = findViewById(R.id.tvGenero)
        btnRegresar = findViewById(R.id.btnRegresar)

        mostrarDatosPerfilLocal()

        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun mostrarDatosPerfilLocal() {
        tvNombre.text = "Nombre: Franklin Elias"
        tvApellido.text = "Apellido: Canchanya Sullca"
        tvDni.text = "DNI: 74839201"
        tvTelefono.text = "Teléfono: 987654321"
        tvCorreo.text = "Correo: paciente@idat.com"
        tvFechaNacimiento.text = "Fecha de nacimiento: 15/08/2000"
        tvGenero.text = "Género: Masculino"
    }
}