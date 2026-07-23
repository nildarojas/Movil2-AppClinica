package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import java.util.Locale

class InicioActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var nvMenu: NavigationView
    private lateinit var btnMenuCentro: LinearLayout
    private lateinit var btnInicioBottom: LinearLayout
    private lateinit var btnSalirBottom: LinearLayout

    private var rolUsuario: String = "PACIENTE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        val preferencias =
            getSharedPreferences(
                "sesion_clinica",
                MODE_PRIVATE
            )

        rolUsuario =
            (
                    intent.getStringExtra("ROL_USUARIO")
                        ?: preferencias.getString(
                            "ROL_USUARIO",
                            "PACIENTE"
                        )
                        ?: "PACIENTE"
                    ).uppercase(Locale.ROOT)

        drawerLayout =
            findViewById(R.id.MenuGeneral)

        nvMenu =
            findViewById(R.id.nvMenu)

        btnMenuCentro = findViewById(R.id.btnMenuCentro)
        btnInicioBottom = findViewById(R.id.btnInicioBottom)
        btnSalirBottom = findViewById(R.id.btnSalirBottom)

        btnMenuCentro.setOnClickListener {
            abrirMenuLateral()
        }

        btnInicioBottom.setOnClickListener {
            cargarPantallaInicial()
            nvMenu.setCheckedItem(R.id.itInicio)
        }

        btnSalirBottom.setOnClickListener {
            desplegarCierreSesion()
        }

        configurarMenuPorRol()
        configurarCabeceraPorRol()

        if (savedInstanceState == null) {

            cargarPantallaInicial()

            nvMenu.setCheckedItem(R.id.itInicio)
        }

        nvMenu.setNavigationItemSelectedListener { menuItem ->

            ejecutarRuteoMenu(menuItem)
        }

        configurarBotonAtras()
    }

    private fun configurarMenuPorRol() {

        val menu =
            nvMenu.menu

        val esAdministrador =
            rolUsuario == "ADMIN"

        menu.findItem(
            R.id.itModuloPaciente
        ).isVisible = !esAdministrador

        menu.findItem(
            R.id.itModuloAdministrador
        ).isVisible = esAdministrador

        menu.findItem(
            R.id.itModuloCuenta
        ).isVisible = true

        menu.findItem(
            R.id.itInicio
        ).title =
            if (esAdministrador) {
                "Panel Administrativo"
            } else {
                "Inicio"
            }

        menu.findItem(
            R.id.itPerfil
        ).title =
            if (esAdministrador) {
                "Perfil del Administrador"
            } else {
                "Datos Personales"
            }
    }

    private fun configurarCabeceraPorRol() {

        val preferencias =
            getSharedPreferences(
                "sesion_clinica",
                MODE_PRIVATE
            )

        val nombreUsuario =
            preferencias.getString(
                "NOMBRE_USUARIO",
                "Usuario"
            ) ?: "Usuario"

        val esAdministrador =
            rolUsuario == "ADMIN"
        val colorRol =
            if (esAdministrador) {
                Color.parseColor("#8B1E1E")
            } else {
                Color.parseColor("#458890")
            }
        val headerView =
            nvMenu.getHeaderView(0)

        val headerMenu =
            headerView.findViewById<LinearLayout>(
                R.id.headerMenu
            )

        val tvHeaderTitulo =
            headerView.findViewById<TextView>(
                R.id.tvHeaderTitulo
            )

        val tvHeaderBienvenida =
            headerView.findViewById<TextView>(
                R.id.tvHeaderBienvenida
            )

        val tvHeaderRol =
            headerView.findViewById<TextView>(
                R.id.tvHeaderRol
            )

        headerMenu.setBackgroundColor(colorRol)

        tvHeaderTitulo.text =
            "Clínica San Miguel"

        tvHeaderBienvenida.text =
            "Bienvenido, $nombreUsuario"

        tvHeaderRol.text =
            if (esAdministrador) {
                "ADMINISTRADOR"
            } else {
                "PACIENTE"
            }
        nvMenu.itemIconTintList =
            ColorStateList.valueOf(colorRol)
    }

    private fun cargarPantallaInicial() {

        val fragmentInicial: Fragment =
            if (rolUsuario == "ADMIN") {
                AdminFragment()
            } else {
                PacienteFragment()
            }

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.flContenedor,
                fragmentInicial
            )
            .commit()
    }

    private fun ejecutarRuteoMenu(
        menuItem: MenuItem
    ): Boolean {

        var fragmentSeleccionado: Fragment? = null

        when (menuItem.itemId) {

            R.id.itInicio -> {

                fragmentSeleccionado =
                    if (rolUsuario == "ADMIN") {
                        AdminFragment()
                    } else {
                        PacienteFragment()
                    }
            }
            R.id.itPerfil -> {

                fragmentSeleccionado =
                    PerfilFragment()
            }

            R.id.itSeguridad -> {

                fragmentSeleccionado =
                    CambiarPasswordInternoFragment().apply {

                        arguments =
                            Bundle().apply {

                                putString(
                                    "ROL_USUARIO",
                                    rolUsuario
                                )
                            }
                    }
            }

            R.id.itCerrarSesion -> {

                desplegarCierreSesion()
            }
            R.id.itNuevaCita -> {

                fragmentSeleccionado =
                    SeleccionarEspecialidadFragment()
            }

            R.id.itMisCitas -> {

                fragmentSeleccionado =
                    MisCitasFragment()
            }

            R.id.itHistorial -> {

                fragmentSeleccionado =
                    HistorialCompletoFragment()
            }

            R.id.itNotificaciones -> {

                fragmentSeleccionado =
                    NotificacionesFragment()
            }

            R.id.itEspecialidades -> {
                fragmentSeleccionado = ListaEspecialidadesFragment()
            }

            R.id.itDoctores -> {
                fragmentSeleccionado = ListaDoctoresAdminFragment()
            }

            R.id.itHorarios -> {
                fragmentSeleccionado = ListaHorariosAdminFragment()
            }

            R.id.itUsuarios -> {
                fragmentSeleccionado = ListaUsuariosFragment()
            }

            R.id.itCitasGlobales -> {
                fragmentSeleccionado = CitasGlobalesFragment()
            }
        }
        if (fragmentSeleccionado != null) {

            supportFragmentManager
                .popBackStackComplete()

            val transaccion =
                supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                    .replace(
                        R.id.flContenedor,
                        fragmentSeleccionado
                    )

            if (menuItem.itemId != R.id.itInicio) {

                transaccion.addToBackStack(null)
            }

            transaccion.commit()

            menuItem.isChecked = true

            nvMenu.setCheckedItem(
                menuItem.itemId
            )
        }

        drawerLayout.closeDrawer(
            GravityCompat.START
        )

        return true
    }

    private fun abrirMenuLateral() {

        drawerLayout.openDrawer(
            GravityCompat.START
        )
    }

    private fun configurarBotonAtras() {

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (
                        drawerLayout.isDrawerOpen(
                            GravityCompat.START
                        )
                    ) {

                        drawerLayout.closeDrawer(
                            GravityCompat.START
                        )

                        return
                    }

                    val fragmentActual =
                        supportFragmentManager
                            .findFragmentById(
                                R.id.flContenedor
                            )
                    if (
                        fragmentActual
                                is SeleccionarMedicoHorarioFragment
                    ) {

                        supportFragmentManager
                            .popBackStack()

                        nvMenu.setCheckedItem(
                            R.id.itNuevaCita
                        )

                        return
                    }
                    if (
                        supportFragmentManager
                            .backStackEntryCount > 0
                    ) {

                        supportFragmentManager
                            .popBackStackComplete()

                        nvMenu.setCheckedItem(
                            R.id.itInicio
                        )

                        return
                    }
                    isEnabled = false

                    onBackPressedDispatcher
                        .onBackPressed()
                }
            }
        )
    }

    private fun androidx.fragment.app.FragmentManager
            .popBackStackComplete() {

        while (backStackEntryCount > 0) {

            popBackStackImmediate()
        }
    }

    private fun desplegarCierreSesion() {

        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage(
                "¿Está seguro de que desea salir del sistema?"
            )
            .setPositiveButton("Sí") { _, _ ->

                val preferencias =
                    getSharedPreferences(
                        "sesion_clinica",
                        MODE_PRIVATE
                    )

                preferencias
                    .edit()
                    .clear()
                    .apply()

                val intent =
                    Intent(
                        this,
                        LoginActivity::class.java
                    )

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                finish()
            }
            .setNegativeButton(
                "No",
                null
            )
            .show()
    }
}