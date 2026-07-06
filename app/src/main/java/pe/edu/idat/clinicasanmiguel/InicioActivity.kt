package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

class InicioActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var nvMenu: NavigationView
    private lateinit var ivMenu: ImageView
    private var rolUsuario: String = "PACIENTE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        rolUsuario = intent.getStringExtra("ROL_USUARIO") ?: "PACIENTE"

        drawerLayout = findViewById(R.id.MenuGeneral)
        nvMenu = findViewById(R.id.nvMenu)
        ivMenu = findViewById(R.id.ivMenu)

        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        configurarMenuPorRol()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.flContenedor, PacienteFragment())
                .commit()
            nvMenu.setCheckedItem(R.id.itInicio)
        }
        nvMenu.setNavigationItemSelectedListener { menuItem ->
            ejecutarRuteoMenu(menuItem)
        }
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    val fragmentActual = supportFragmentManager.findFragmentById(R.id.flContenedor)
                    if (fragmentActual is SeleccionarMedicoHorarioFragment) {
                        supportFragmentManager.popBackStack()
                        nvMenu.setCheckedItem(R.id.itNuevaCita)
                    } else if (supportFragmentManager.backStackEntryCount > 0) {

                        supportFragmentManager.popBackStackComplete()
                        nvMenu.setCheckedItem(R.id.itInicio)
                    } else {

                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    private fun configurarMenuPorRol() {
        if (rolUsuario == "PACIENTE") {

        }
    }

    private fun ejecutarRuteoMenu(menuItem: MenuItem): Boolean {
        var esFragment = false
        var fragmentSeleccionado: Fragment? = null

        when (menuItem.itemId) {
            R.id.itInicio -> {
                fragmentSeleccionado = PacienteFragment()
                esFragment = true
            }
            R.id.itPerfil -> {
                fragmentSeleccionado = PerfilFragment()
                esFragment = true
            }
            R.id.itMisCitas -> {
                fragmentSeleccionado = MisCitasFragment()
                esFragment = true
            }
            R.id.itHistorial -> {
                fragmentSeleccionado = HistorialCompletoFragment()
                esFragment = true
            }
            R.id.itNotificaciones -> {
                fragmentSeleccionado = NotificacionesFragment()
                esFragment = true
            }
            R.id.itNuevaCita -> {
                fragmentSeleccionado = SeleccionarEspecialidadFragment()
                esFragment = true
            }
            R.id.itSeguridad -> {
                fragmentSeleccionado = CambiarPasswordInternoFragment().apply {
                    arguments = Bundle().apply { putString("ROL_USUARIO", rolUsuario) }
                }
                esFragment = true
            }
            R.id.itCerrarSesion -> {
                desplegarCierreSesion()
            }
        }

        if (esFragment && fragmentSeleccionado != null) {
            supportFragmentManager.popBackStackComplete()
            val transaccion = supportFragmentManager.beginTransaction()
                .replace(R.id.flContenedor, fragmentSeleccionado)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            if (menuItem.itemId != R.id.itInicio) {
                transaccion.addToBackStack(null)
            }
            transaccion.commit()

            menuItem.isChecked = true
            nvMenu.setCheckedItem(menuItem.itemId)
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun androidx.fragment.app.FragmentManager.popBackStackComplete() {
        while (backStackEntryCount > 0) {
            popBackStackImmediate()
        }
    }

    private fun desplegarCierreSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Está seguro de que desea salir del sistema?")
            .setPositiveButton("Sí") { _, _ ->
                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                prefs.edit().clear().apply()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}