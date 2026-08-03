package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.RegistrarEspecialidadActivity
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock
import pe.edu.idat.clinicasanmiguel.entity.Especialidad
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.EspecialidadRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaEspecialidadesApi

class ListaEspecialidadesFragment :
    Fragment() {

    private lateinit var rvEspecialidades:
            RecyclerView

    private lateinit var especialidadRepository:
            EspecialidadRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.activity_lista_especialidades,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        rvEspecialidades =
            view.findViewById(
                R.id.rvEspecialidades
            )

        rvEspecialidades.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        especialidadRepository =
            EspecialidadRepository(
                requireContext()
            )

        view.findViewById<
                FloatingActionButton
                >(
            R.id.fabNuevaEspecialidad
        ).setOnClickListener {
            val intent =
                Intent(
                    requireContext(),
                    RegistrarEspecialidadActivity::class.java
                )

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        cargarEspecialidades()
    }

    private fun cargarEspecialidades() {
        val locales =
            especialidadRepository
                .obtenerEspecialidadesLocales()

        mostrarEspecialidades(
            locales
        )

        especialidadRepository
            .sincronizarEspecialidadesApi {
                    resultado ->

                if (!isAdded) {
                    return@sincronizarEspecialidadesApi
                }

                when (resultado) {
                    is ResultadoCargaEspecialidadesApi
                    .Exito -> {

                        mostrarEspecialidades(
                            resultado.especialidades
                        )
                    }

                    is ResultadoCargaEspecialidadesApi
                    .SinConexion -> {

                        mostrarEspecialidades(
                            resultado.especialidades
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaEspecialidadesApi
                    .Error -> {

                        mostrarEspecialidades(
                            resultado.especialidades
                        )

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is ResultadoCargaEspecialidadesApi
                    .SesionExpirada -> {

                        cerrarSesion(
                            resultado.mensaje
                        )
                    }

                    is ResultadoCargaEspecialidadesApi
                    .SinPermiso -> {

                        Toast.makeText(
                            requireContext(),
                            resultado.mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun mostrarEspecialidades(
        especialidades:
        List<Especialidad>
    ) {
        val lista =
            especialidades.map {
                EspecialidadMock(
                    nombre = it.nombre,
                    area =
                        "Área no asignada",
                    estado = "ACTIVO",
                    id = it.id
                )
            }

        rvEspecialidades.adapter =
            EspecialidadAdminAdapter(
                lista = lista,
                esModoAdmin = true
            )
    }

    private fun cerrarSesion(
        mensaje: String
    ) {
        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_LONG
        ).show()

        SessionManager(
            requireContext()
        ).limpiarSesion()

        val intent =
            Intent(
                requireContext(),
                LoginActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        requireActivity().finish()
    }
}