package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pe.edu.idat.clinicasanmiguel.network.JwtUtils
import pe.edu.idat.clinicasanmiguel.network.SessionManager

class MainActivity : AppCompatActivity() {

    private var navegacionRealizada =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_main
        )

        ViewCompat
            .setOnApplyWindowInsetsListener(
                findViewById(
                    R.id.main
                )
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

        lifecycleScope.launch {
            delay(
                TIEMPO_SPLASH_MS
            )

            resolverDestinoInicial()
        }
    }

    private fun resolverDestinoInicial() {
        if (
            navegacionRealizada ||
            isFinishing ||
            isDestroyed
        ) {
            return
        }

        val sessionManager =
            SessionManager(
                this
            )

        val sesionValida =
            runCatching {
                val token =
                    sessionManager.obtenerToken()

                sessionManager.esSesionRemota() &&
                        !token.isNullOrBlank() &&
                        JwtUtils.esTokenVigente(
                            token
                        )
            }
                .onFailure { exception ->
                    Log.e(
                        TAG,
                        "No se pudo validar la sesión",
                        exception
                    )
                }
                .getOrDefault(
                    false
                )

        if (!sesionValida) {
            runCatching {
                sessionManager.limpiarSesion()
            }.onFailure { exception ->
                Log.e(
                    TAG,
                    "No se pudo limpiar la sesión",
                    exception
                )
            }
        }

        navegacionRealizada =
            true

        if (sesionValida) {
            abrirInicio()
        } else {
            abrirLogin()
        }
    }

    private fun abrirInicio() {
        abrirDestino(
            InicioActivity::class.java
        )
    }

    private fun abrirLogin() {
        abrirDestino(
            LoginActivity::class.java
        )
    }

    private fun abrirDestino(
        destino: Class<*>
    ) {
        val intent =
            Intent(
                this,
                destino
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(
            intent
        )

        finish()
    }

    companion object {
        private const val TAG =
            "MainActivity"

        private const val TIEMPO_SPLASH_MS =
            1_500L
    }
}