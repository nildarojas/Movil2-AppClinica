package pe.edu.idat.clinicasanmiguel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.CitasAdapter
import pe.edu.idat.clinicasanmiguel.adapter.CitaPacienteMock

class MisCitasActivity : AppCompatActivity() {

    private lateinit var rvMisCitas: RecyclerView
    private val listaCitasLocales = mutableListOf<CitaPacienteMock>()
    private lateinit var adapter: CitasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas)

        rvMisCitas = findViewById(R.id.rvMisCitas)
        rvMisCitas.layoutManager = LinearLayoutManager(this)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val posicion = data?.getIntExtra("posicion", -1) ?: -1
            if (posicion != -1) {
                listaCitasLocales[posicion].estado = "REPROGRAMADA"
                adapter.notifyItemChanged(posicion)
            }
        }
    }
}