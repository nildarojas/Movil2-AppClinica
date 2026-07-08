package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.NotificacionesAdapter
import pe.edu.idat.clinicasanmiguel.adapter.NotificacionMock

class NotificacionesFragment : Fragment(R.layout.activity_notificaciones) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvNotificaciones)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val listaNotificaciones = listOf(
            NotificacionMock(1, "Registrado: su cita de La Dra. Abigail ha sido Registrado correctamente", "2026-06-24" ),
            NotificacionMock(2, "Reprogramado: La Dra. Abigail ha sido reporgramado correctamente", "2026-06-22")
        )

        rv.adapter = NotificacionesAdapter(listaNotificaciones)
    }
}