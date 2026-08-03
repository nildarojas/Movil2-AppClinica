package pe.edu.idat.clinicasanmiguel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.LoginActivity
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadAdminAdapter
import pe.edu.idat.clinicasanmiguel.adapter.EspecialidadMock
import pe.edu.idat.clinicasanmiguel.entity.Especialidad
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.repository.EspecialidadRepository
import pe.edu.idat.clinicasanmiguel.repository.ResultadoCargaEspecialidadesApi

class SeleccionarEspecialidadFragment :
    Fragment(
        R.layout.activity_seleccionar_especialidad
    ) {

    private lateinit var especialidadRepository:
            EspecialidadRepository

    private lateinit var rvEspecialidades:
            RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        especialidadRepository =
            EspecialidadRepository(
                requireContext()
            )

        rvEspecialidades =
            view.findViewById(
                R.id.lvEspecialidadesReserva
            )

        rvEspecialidades.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

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
                    area = "",
                    estado = "",
                    id = it.id
                )
            }

        rvEspecialidades.adapter =
            EspecialidadAdminAdapter(
                lista = lista,
                esModoAdmin = false
            ) {
                    especialidadSeleccionada ->

                if (
                    especialidadSeleccionada.id <= 0
                ) {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo identificar la especialidad",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@EspecialidadAdminAdapter
                }

                val siguientePaso =
                    SeleccionarMedicoHorarioFragment()
                        .apply {
                            arguments =
                                Bundle().apply {
                                    putInt(
                                        "ID_ESPECIALIDAD",
                                        especialidadSeleccionada.id
                                    )

                                    putString(
                                        "NOMBRE_ESPECIALIDAD",
                                        especialidadSeleccionada.nombre
                                    )
                                }
                        }

                parentFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.flContenedor,
                        siguientePaso
                    )
                    .addToBackStack(null)
                    .commit()
            }
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