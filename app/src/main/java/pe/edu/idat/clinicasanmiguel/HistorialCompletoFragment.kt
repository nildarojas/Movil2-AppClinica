package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.adapter.CitaPacienteMock

class HistorialCompletoFragment : Fragment(R.layout.activity_historial_completo) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvHistorialCompleto)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val datosPasados = listOf(
            CitaPacienteMock(101, "Pediatría", "Dra. Abigail Valdez", "2026-05-10 | 10:00 AM", "CONFIRMADA"),
            CitaPacienteMock(102, "Cardiología", "Dr. Bryant Yacila", "2026-06-01 | 04:30 PM", "CANCELADA")
        )

        rv.adapter = CitasAdapter(datosPasados, true)
    }
}