package pe.edu.idat.clinicasanmiguel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

class InicioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        val drawerLayout = findViewById<DrawerLayout>(R.id.MenuGeneral)
        val nvMenu = findViewById<NavigationView>(R.id.nvMenu)
        val ivMenu = findViewById<android.widget.ImageView>(R.id.ivMenu)

        ivMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        if (savedInstanceState == null) {
            replaceFragment(PacienteFragment())
            nvMenu.setCheckedItem(R.id.itInicio)
        }

        nvMenu.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itInicio -> replaceFragment(PacienteFragment())
                R.id.itPerfil -> replaceFragment(PerfilFragment())
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flContenedor, fragment)
            .commit()
    }
}