package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pe.edu.idat.clinicasanmiguel.network.JwtUtils
import pe.edu.idat.clinicasanmiguel.network.SessionManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_main
        )

        ViewCompat
            .setOnApplyWindowInsetsListener(
                findViewById(R.id.main)
            ) { view, insets ->

                val systemBars =
                    insets.getInsets(
                        WindowInsetsCompat
                            .Type
                            .systemBars()
                    )

                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )

                insets
            }

        Handler(
            Looper.getMainLooper()
        ).postDelayed(
            {
                resolverDestinoInicial()
            },
            3000
        )
    }

    private fun resolverDestinoInicial() {
        val sessionManager =
            SessionManager(this)

        val token =
            sessionManager.obtenerToken()

        val sesionValida =
            sessionManager.esSesionRemota() &&
                    !token.isNullOrBlank() &&
                    JwtUtils.esTokenVigente(token)

        if (sesionValida) {
            abrirInicio()
        } else {
            sessionManager.limpiarSesion()
            abrirLogin()
        }
    }

    private fun abrirInicio() {
        val intent =
            Intent(
                this,
                InicioActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }

    private fun abrirLogin() {
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
}