package pe.edu.idat.clinicasanmiguel
import pe.edu.idat.clinicasanmiguel.adapter.UsuariosAdminAdapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.UsuarioMock

class ListaUsuariosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_usuarios)

        val rv = findViewById<RecyclerView>(R.id.rvUsuariosAdmin)
        rv.layoutManager = LinearLayoutManager(this)

        val datosSimulados = listOf(
            UsuarioMock("Franklin Elias", "Canchanya Sullca", "elias@idat.edu.pe", "PACIENTE"),
            UsuarioMock("Nilda", "Rojas Campos", "nilda@idat.edu.pe", "PACIENTE"),
            UsuarioMock("Abigail", "Valdez Ramos", "abigail@idat.edu.pe", "ADMIN")
        )

        rv.adapter = UsuariosAdminAdapter(datosSimulados)
    }
}