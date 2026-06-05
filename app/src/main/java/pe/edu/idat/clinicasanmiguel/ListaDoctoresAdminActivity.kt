package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.adapter.DoctorAdminAdapter

class ListaDoctoresAdminActivity : AppCompatActivity() {

    private lateinit var rvDoctores: RecyclerView
    private lateinit var adapter: DoctorAdminAdapter
    private lateinit var fabAddDoctor: FloatingActionButton
    private val listaDoctoresLocales = mutableListOf<DoctorMock>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_doctores_admin)

        rvDoctores = findViewById(R.id.rvDoctoresAdmin)
        fabAddDoctor = findViewById(R.id.fabAddDoctor)

        rvDoctores.layoutManager = LinearLayoutManager(this)

        if (listaDoctoresLocales.isEmpty()) {
            inicializarDoctoresLocales()
        }

        adapter = DoctorAdminAdapter(listaDoctoresLocales)
        rvDoctores.adapter = adapter

        fabAddDoctor.setOnClickListener {
            startActivity(Intent(this, RegistrarDoctorActivity::class.java))
        }
    }

    private fun inicializarDoctoresLocales() {
        listaDoctoresLocales.add(DoctorMock("Bryant", "Yacila", "Cardiología", true))
        listaDoctoresLocales.add(DoctorMock("Abigail", "Valdez", "Pediatría", true))
        listaDoctoresLocales.add(DoctorMock("Nilda", "Rojas", "Medicina General", false))
    }
}