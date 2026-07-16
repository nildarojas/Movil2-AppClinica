package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class AdminFragment : Fragment(R.layout.activity_admin) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val btnEspecialidades =
            view.findViewById<Button>(
                R.id.btnEspecialidades
            )

        val btnDoctores =
            view.findViewById<Button>(
                R.id.btnDoctores
            )

        val btnHorarios =
            view.findViewById<Button>(
                R.id.btnHorarios
            )

        val btnUsuarios =
            view.findViewById<Button>(
                R.id.btnUsuarios
            )

        val btnCitas =
            view.findViewById<Button>(
                R.id.btnCitas
            )
        view.findViewById<View>(
            R.id.tvOpcionesCuenta
        ).visibility = View.GONE

        view.findViewById<View>(
            R.id.tilPerfilAdmin
        ).visibility = View.GONE

        btnEspecialidades.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    ListaEspecialidadesActivity::class.java
                )
            )
        }

        btnDoctores.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    ListaDoctoresAdminActivity::class.java
                )
            )
        }

        btnHorarios.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    ListaHorariosAdminActivity::class.java
                )
            )
        }

        btnUsuarios.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    ListaUsuariosActivity::class.java
                )
            )
        }

        btnCitas.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    MaestroCitasAdminActivity::class.java
                )
            )
        }
    }
}