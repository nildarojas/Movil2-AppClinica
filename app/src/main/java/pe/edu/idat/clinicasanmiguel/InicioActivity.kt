package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
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
            actualizarEstadoBarraInferior("MENU")
        }

        btnInicioBottom.setOnClickListener {
            cargarPantallaInicial()
            nvMenu.setCheckedItem(R.id.itInicio)
            actualizarEstadoBarraInferior("INICIO")
        }

        btnSalirBottom.setOnClickListener {
            actualizarEstadoBarraInferior("SALIR")
            desplegarCierreSesion()
        }

        configurarMenuPorRol()
        configurarCabeceraPorRol()

        if (savedInstanceState == null) {

            cargarPantallaInicial()

            nvMenu.setCheckedItem(R.id.itInicio)
            actualizarEstadoBarraInferior("INICIO")
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
                actualizarEstadoBarraInferior("MENU")
            } else {
                actualizarEstadoBarraInferior("INICIO")
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
                        actualizarEstadoBarraInferior("MENU")

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
                        actualizarEstadoBarraInferior("INICIO")

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

    private fun actualizarEstadoBarraInferior(opcionSeleccionada: String) {
        val colorActivo = Color.parseColor("#458890")
        val colorInactivo = Color.parseColor("#A0A0A0")

        val imgInicio = btnInicioBottom.getChildAt(0) as ImageView
        val tvInicio = btnInicioBottom.getChildAt(1) as TextView

        val imgMenu = btnMenuCentro.getChildAt(0) as ImageView
        val tvMenu = btnMenuCentro.getChildAt(1) as TextView

        val imgSalir = btnSalirBottom.getChildAt(0) as ImageView
        val tvSalir = btnSalirBottom.getChildAt(1) as TextView

        imgInicio.setColorFilter(colorInactivo)
        tvInicio.setTextColor(colorInactivo)

        imgMenu.setColorFilter(colorInactivo)
        tvMenu.setTextColor(colorInactivo)

        imgSalir.setColorFilter(colorInactivo)
        tvSalir.setTextColor(colorInactivo)

        when (opcionSeleccionada) {
            "INICIO" -> {
                imgInicio.setColorFilter(colorActivo)
                tvInicio.setTextColor(colorActivo)
            }
            "MENU" -> {
                imgMenu.setColorFilter(colorActivo)
                tvMenu.setTextColor(colorActivo)
            }
            "SALIR" -> {
                imgSalir.setColorFilter(Color.parseColor("#FF5252"))
                tvSalir.setTextColor(Color.parseColor("#FF5252"))
            }
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