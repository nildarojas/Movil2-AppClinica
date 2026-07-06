package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.adapter.CitaPacienteMock

class MisCitasFragment : Fragment(R.layout.activity_mis_citas) {

    private lateinit var rvMisCitas: RecyclerView
    private val listaCitasLocales = mutableListOf<CitaPacienteMock>()
    private lateinit var adapter: CitasAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMisCitas = view.findViewById(R.id.rvMisCitas)
        rvMisCitas.layoutManager = LinearLayoutManager(requireContext())

        if (listaCitasLocales.isEmpty()) {
            inicializarDatosEstaticos()
        }

        adapter = CitasAdapter(listaCitasLocales)
        rvMisCitas.adapter = adapter
    }

    private fun inicializarDatosEstaticos() {
        listaCitasLocales.add(CitaPacienteMock(1, "Cardiología", "Dr. Bryant Yacila", "2026-06-25 | 09:30 AM", "CONFIRMADA"))
        listaCitasLocales.add(CitaPacienteMock(2, "Pediatría", "Dra. Abigail Valdez", "2026-06-28 | 11:00 AM", "PENDIENTE"))
        listaCitasLocales.add(CitaPacienteMock(3, "Ginecología", "Dra. Nilda Rojas", "2026-06-15 | 03:00 PM", "CANCELADA"))
    }
}