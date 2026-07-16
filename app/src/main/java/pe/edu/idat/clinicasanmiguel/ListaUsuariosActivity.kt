package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.adapter.UsuarioMock
import pe.edu.idat.clinicasanmiguel.adapter.UsuariosAdminAdapter
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository

class ListaUsuariosActivity : AppCompatActivity() {

    private lateinit var rvUsuarios: RecyclerView
    private lateinit var usuarioRepository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_usuarios)

        rvUsuarios = findViewById(R.id.rvUsuariosAdmin)

        rvUsuarios.layoutManager =
            LinearLayoutManager(this)

        usuarioRepository =
            UsuarioRepository(this)
    }

    override fun onResume() {
        super.onResume()

        cargarUsuariosDesdeSQLite()
    }

    private fun cargarUsuariosDesdeSQLite() {
        val usuariosReales =
            usuarioRepository.obtenerTodosLosUsuarios()
        val usuariosParaMostrar =
            usuariosReales.map { usuario ->

                UsuarioMock(
                    usuario.nombre,
                    usuario.apellido,
                    usuario.correo,
                    usuario.rol
                )
            }

        rvUsuarios.adapter =
            UsuariosAdminAdapter(
                usuariosParaMostrar
            )
    }
}