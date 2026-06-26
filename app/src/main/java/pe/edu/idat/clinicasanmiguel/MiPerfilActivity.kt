package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MiPerfilActivity : AppCompatActivity() {

    private lateinit var tvTitulo: TextView
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

        tvTitulo = findViewById(R.id.tvTitulo)
        tvNombre = findViewById(R.id.tvNombre)
        tvApellido = findViewById(R.id.tvApellido)
        tvDni = findViewById(R.id.tvDni)
        tvTelefono = findViewById(R.id.tvTelefono)
        tvCorreo = findViewById(R.id.tvCorreo)
        tvFechaNacimiento = findViewById(R.id.tvFechaNacimiento)
        tvGenero = findViewById(R.id.tvGenero)
        btnRegresar = findViewById(R.id.btnRegresar)

        val rol = intent.getStringExtra("ROL_USUARIO") ?: "PACIENTE"

        if (rol == "ADMIN") {
            tvTitulo.text = "Perfil de Administrador"
            tvNombre.text = "Nombre: Marco Antonio"
            tvApellido.text = "Apellido: Gutierrez Luyo"
            tvDni.text = "DNI: 71234567"
            tvTelefono.text = "Teléfono: 912345678"
            tvCorreo.text = "Correo: admin.marcos@idat.com"
            tvFechaNacimiento.text = "Fecha de nacimiento: 22/04/1999"
            tvGenero.text = "Género: Masculino"
        } else {
            tvTitulo.text = "Mi Ficha Médica"
            tvNombre.text = "Nombre: Franklin Elias"
            tvApellido.text = "Apellido: Canchanya Sullca"
            tvDni.text = "DNI: 74839201"
            tvTelefono.text = "Teléfono: 987654321"
            tvCorreo.text = "Correo: paciente@idat.com"
            tvFechaNacimiento.text = "Fecha de nacimiento: 15/08/2000"
            tvGenero.text = "Género: Masculino"
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }
}