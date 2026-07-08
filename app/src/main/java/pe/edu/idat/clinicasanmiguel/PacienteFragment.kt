package pe.edu.idat.clinicasanmiguel

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

class PacienteFragment : Fragment(R.layout.activity_paciente) {

    private lateinit var tvSaludoBienvenida: TextView
    private lateinit var citaRepository: CitaRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        citaRepository = CitaRepository(requireContext())

        tvSaludoBienvenida = view.findViewById(R.id.tvSaludoBienvenida)
        val ivCampanaNotificacion = view.findViewById<ImageView>(R.id.ivCampanaNotificacion)

        // Mapeo de ambos botones
        val btnHorarios = view.findViewById<MaterialButton>(R.id.btnHorarios)
        val btnHorariosVacio = view.findViewById<MaterialButton>(R.id.btnHorariosVacio)
        val btnVerOtrasCitas = view.findViewById<MaterialButton>(R.id.btnVerOtrasCitas)

        val layoutVacio = view.findViewById<LinearLayout>(R.id.layoutEstadoVacio)
        val layoutCita = view.findViewById<LinearLayout>(R.id.layoutUltimaCita)

        val tvHomeEspecialidad = view.findViewById<TextView>(R.id.tvHomeEspecialidad)
        val tvHomeMedico = view.findViewById<TextView>(R.id.tvHomeMedico)
        val tvHomeFechaHora = view.findViewById<TextView>(R.id.tvHomeFechaHora)
        val tvHomeEstado = view.findViewById<TextView>(R.id.tvHomeEstado)

        val preferencias = requireContext().getSharedPreferences("sesion_clinica", Context.MODE_PRIVATE)
        val idPacienteLogueado = preferencias.getInt("ID_USUARIO", -1)
        val nombreUsuario = preferencias.getString("NOMBRE_USUARIO", "Paciente")

        tvSaludoBienvenida.text = "¡Bienvenido,\n$nombreUsuario!"

        if (idPacienteLogueado != -1) {
            val ultimaCita = citaRepository.obtenerUltimaCitaPorPaciente(idPacienteLogueado)

            if (ultimaCita != null) {
                layoutVacio.visibility = View.GONE
                layoutCita.visibility = View.VISIBLE
                btnHorarios.visibility = View.VISIBLE
                btnHorariosVacio.visibility = View.GONE
                tvHomeEspecialidad.text = ultimaCita.especialidad
                tvHomeMedico.text = "Médico: " + ultimaCita.medico
                tvHomeFechaHora.text = "Horario: " + ultimaCita.fechaHora
                tvHomeEstado.text = ultimaCita.estado
            } else {
                layoutCita.visibility = View.GONE
                layoutVacio.visibility = View.VISIBLE

                btnHorarios.visibility = View.GONE
                btnHorariosVacio.visibility = View.VISIBLE
            }
        }

        btnHorarios.setOnClickListener {
            cambiarPantallaDesdeInicio(SeleccionarEspecialidadFragment())
        }

        btnHorariosVacio.setOnClickListener {
            cambiarPantallaDesdeInicio(SeleccionarEspecialidadFragment())
        }

        btnVerOtrasCitas.setOnClickListener {
            cambiarPantallaDesdeInicio(MisCitasFragment())
        }

        ivCampanaNotificacion.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Notificaciones: Próxima entrega con SqLite",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cambiarPantallaDesdeInicio(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.flContenedor, fragment)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .addToBackStack(fragment::class.java.simpleName)
            .commit()
    }
}