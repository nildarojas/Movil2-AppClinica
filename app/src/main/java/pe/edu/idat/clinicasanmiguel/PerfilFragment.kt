package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class PerfilFragment : Fragment(R.layout.activity_perfil) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombre)
        val tvApellido = view.findViewById<TextView>(R.id.tvApellido)
        val tvDni = view.findViewById<TextView>(R.id.tvDni)
        val tvTelefono = view.findViewById<TextView>(R.id.tvTelefono)
        val tvCorreo = view.findViewById<TextView>(R.id.tvCorreo)
        val tvFechaNacimiento = view.findViewById<TextView>(R.id.tvFechaNacimiento)
        val tvGenero = view.findViewById<TextView>(R.id.tvGenero)
        val btnRegresar = view.findViewById<MaterialButton>(R.id.btnRegresar)
        val rol = arguments?.getString("ROL_USUARIO") ?: "PACIENTE"

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
            parentFragmentManager.beginTransaction()
                .replace(R.id.flContenedor, PacienteFragment())
                .commit()
        }
    }
}