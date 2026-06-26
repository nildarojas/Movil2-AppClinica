package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class PacienteFragment : Fragment(R.layout.activity_paciente) {

    private lateinit var tvSaludoBienvenida: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvSaludoBienvenida = view.findViewById(R.id.tvSaludoBienvenida)
        val btnVerCitas = view.findViewById<Button>(R.id.btnVerCitas)
        val btnHorarios = view.findViewById<Button>(R.id.btnHorarios)
        val btnHistorial = view.findViewById<Button>(R.id.btnHistorial)
        val btnNotificaciones = view.findViewById<Button>(R.id.btnNotificaciones)
        val acPerfil = view.findViewById<AutoCompleteTextView>(R.id.acPerfil)

        tvSaludoBienvenida.text = "¡Bienvenido, Franklin Elias!"

        btnVerCitas.setOnClickListener {

            startActivity(Intent(requireContext(), MisCitasActivity::class.java))
        }
        btnHorarios.setOnClickListener {
            startActivity(Intent(requireContext(), SeleccionarEspecialidadActivity::class.java))
        }
        btnHistorial.setOnClickListener {
            startActivity(Intent(requireContext(), HistorialCompletoActivity::class.java))
        }
        btnNotificaciones.setOnClickListener {
            startActivity(Intent(requireContext(), NotificacionesActivity::class.java))
        }

        val opciones = arrayOf("👤 Mi Perfil", "🔑 Cambiar contraseña", "🚪 Cerrar sesión")


        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_perfil_item, opciones)
        acPerfil.setAdapter(adapter)

        acPerfil.setOnClickListener { acPerfil.showDropDown() }

        acPerfil.setOnItemClickListener { parent, _, position, _ ->
            val seleccion = parent.getItemAtPosition(position).toString()
            when (seleccion) {
                "👤 Mi Perfil" -> {
                    startActivity(Intent(requireContext(), MiPerfilActivity::class.java).putExtra("ROL_USUARIO", "PACIENTE"))
                }
                "🔑 Cambiar contraseña" -> {
                    startActivity(Intent(requireContext(), CambiarPasswordInternoActivity::class.java).putExtra("ROL_USUARIO", "PACIENTE"))
                }
                "🚪 Cerrar sesión" -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Cerrar Sesión")
                        .setMessage("¿Está seguro de que desea salir del sistema?")
                        .setPositiveButton("Sí") { _, _ ->
                            val prefs = requireActivity().getSharedPreferences("auth", AppCompatActivity.MODE_PRIVATE)
                            prefs.edit().clear().apply()
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
            acPerfil.setText("Mi Perfil", false)
        }
    }
}