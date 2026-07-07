package pe.edu.idat.clinicasanmiguel

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository

class CambiarPasswordInternoFragment : Fragment(R.layout.activity_cambiar_password_interno) {

    private lateinit var usuarioRepository: UsuarioRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        usuarioRepository = UsuarioRepository(requireContext())

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloCambioInterno)
        val etActual = view.findViewById<TextInputEditText>(R.id.etPasswordActual)
        val etNueva1 = view.findViewById<TextInputEditText>(R.id.etPasswordNueva1)
        val etNueva2 = view.findViewById<TextInputEditText>(R.id.etPasswordNueva2)
        val btnActualizar = view.findViewById<Button>(R.id.btnActualizarPasswordInterno)
        val preferencias = requireContext().getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
        val idUsuarioLogueado = preferencias.getInt("ID_USUARIO", -1)
        val rol = preferencias.getString("ROL_USUARIO", "PACIENTE") ?: "PACIENTE"

        if (rol == "ADMIN") {
            tvTitulo.text = "Seguridad: Admin Hub"
        } else {
            tvTitulo.text = "Seguridad de la Cuenta"
        }

        btnActualizar.setOnClickListener {
            val txtActual = etActual.text.toString().trim()
            val txtNueva1 = etNueva1.text.toString().trim()
            val txtNueva2 = etNueva2.text.toString().trim()
            if (txtActual.isEmpty() || txtNueva1.isEmpty() || txtNueva2.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (txtNueva1 != txtNueva2) {
                Toast.makeText(requireContext(), "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (txtNueva1.length < 6) {
                Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (idUsuarioLogueado != -1) {
                val miPerfil = usuarioRepository.obtenerUsuarioPorId(idUsuarioLogueado)

                if (miPerfil != null && miPerfil.password == txtActual) {
                    val filasAfectadas = usuarioRepository.actualizarPassword(idUsuarioLogueado, txtNueva1)

                    if (filasAfectadas > 0) {
                        Toast.makeText(requireContext(), "Contraseña de $rol actualizada en SQLite correctamente", Toast.LENGTH_LONG).show()
                        etActual.text?.clear()
                        etNueva1.text?.clear()
                        etNueva2.text?.clear()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Error: No se pudo actualizar en el almacenamiento", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Error de sesión: ID inválido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}