package pe.edu.idat.clinicasanmiguel

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository

class PerfilFragment : Fragment(R.layout.activity_perfil) {

    private lateinit var usuarioRepository: UsuarioRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usuarioRepository = UsuarioRepository(requireContext())

        val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombre)
        val tvApellido = view.findViewById<TextView>(R.id.tvApellido)
        val tvDni = view.findViewById<TextView>(R.id.tvDni)
        val tvTelefono = view.findViewById<TextView>(R.id.tvTelefono)
        val tvCorreo = view.findViewById<TextView>(R.id.tvCorreo)
        val tvFechaNacimiento = view.findViewById<TextView>(R.id.tvFechaNacimiento)
        val tvGenero = view.findViewById<TextView>(R.id.tvGenero)
        val btnRegresar = view.findViewById<MaterialButton>(R.id.btnRegresar)
        val preferencias = requireContext().getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
        val idUsuarioLogueado = preferencias.getInt("ID_USUARIO", -1)
        val rol = preferencias.getString("ROL_USUARIO", "PACIENTE") ?: "PACIENTE"

        if (idUsuarioLogueado != -1) {
            val usuarioPerfil = usuarioRepository.obtenerUsuarioPorId(idUsuarioLogueado)

            if (usuarioPerfil != null) {
                if (rol == "ADMIN") {
                    tvTitulo.text = "Perfil de Administrador"
                } else {
                    tvTitulo.text = "Mi Ficha Médica"
                }

                tvNombre.text = "Nombre: ${usuarioPerfil.nombre}"
                tvApellido.text = "Apellido: ${usuarioPerfil.apellido}"
                tvDni.text = "DNI: ${usuarioPerfil.dni}"
                tvTelefono.text = "Teléfono: ${usuarioPerfil.telefono}"
                tvCorreo.text = "Correo: ${usuarioPerfil.correo}"
                tvFechaNacimiento.text = "Fecha de nacimiento: ${usuarioPerfil.fechaNacimiento}"
                tvGenero.text = "Género: ${usuarioPerfil.genero}"
            }
        }

        btnRegresar.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.flContenedor, PacienteFragment())
                    .commit()
            }
        }
    }
}