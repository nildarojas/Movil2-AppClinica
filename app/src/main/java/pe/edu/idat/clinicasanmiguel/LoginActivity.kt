package pe.edu.idat.clinicasanmiguel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.entity.Usuario
import pe.edu.idat.clinicasanmiguel.repository.ResultadoLoginApi
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository
import pe.edu.idat.clinicasanmiguel.utils.LoadingController

class LoginActivity : AppCompatActivity() {

    private lateinit var tvRegistro: TextView
    private lateinit var tvOlvidaste: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText

    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var loadingController: LoadingController

    /*
     * Evita que se ejecuten varias consultas de login
     * por toques rápidos o repetidos.
     */
    private var loginEnProceso = false

    /*
     * Identifica la operación de carga actual.
     * LoadingController utiliza este token para evitar
     * que una carga anterior cierre un modal más reciente.
     */
    private var loginLoadingToken: Long? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        usuarioRepository = UsuarioRepository(this)

        loadingController = LoadingController(
            fragmentManager = supportFragmentManager,
            coroutineScope = lifecycleScope
        )

        tvRegistro = findViewById(R.id.tvRegistro)
        tvOlvidaste = findViewById(R.id.tvOlvidaste)
        btnLogin = findViewById(R.id.btnLogin)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)

        tvRegistro.setOnClickListener {
            if (loginEnProceso) {
                return@setOnClickListener
            }

            val intent = Intent(
                this,
                RegistroActivity::class.java
            )

            startActivity(intent)
        }

        tvOlvidaste.setOnClickListener {
            if (loginEnProceso) {
                return@setOnClickListener
            }

            val intent = Intent(
                this,
                ResetearPasswordActivity::class.java
            )

            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            iniciarSesion()
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun iniciarSesion() {

        /*
         * Segunda protección contra varios toques.
         * Aunque el botón ya esté deshabilitado, esta variable
         * evita cualquier ejecución duplicada pendiente.
         */
        if (loginEnProceso) {
            return
        }

        val correo = etCorreo.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val contrasena = etContrasena.text
            ?.toString()
            ?.trim()
            .orEmpty()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(
                this,
                "Por favor, ingrese sus datos",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        /*
         * Primero bloqueamos la pantalla y mostramos el modal.
         */
        iniciarCargaLogin()

        /*
         * La consulta comienza inmediatamente.
         * No existe ningún delay antes de llamar a Azure.
         */
        usuarioRepository.loginApi(
            correo = correo,
            password = contrasena
        ) { resultado ->

            /*
             * Se utiliza runOnUiThread por seguridad,
             * por si el repositorio devuelve el resultado
             * desde un hilo secundario.
             */
            runOnUiThread {

                if (isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                /*
                 * El resultado ya llegó, pero LoadingController
                 * esperará el tiempo restante hasta completar
                 * los 1.5 segundos mínimos.
                 */
                finalizarCargaLogin(resultado)
            }
        }
    }

    private fun iniciarCargaLogin() {

        loginEnProceso = true

        btnLogin.isEnabled = false
        etCorreo.isEnabled = false
        etContrasena.isEnabled = false
        tvRegistro.isEnabled = false
        tvOlvidaste.isEnabled = false

        btnLogin.text = "CONECTANDO..."

        loginLoadingToken = loadingController.show(
            message = "Iniciando sesión..."
        )
    }

    private fun finalizarCargaLogin(
        resultado: ResultadoLoginApi
    ) {

        val token = loginLoadingToken
            ?: return

        loadingController.hide(
            requestToken = token
        ) callback@{

            if (isFinishing || isDestroyed) {
                return@callback
            }

            loginLoadingToken = null

            when (resultado) {

                is ResultadoLoginApi.Exito -> {

                    /*
                     * En caso exitoso no habilitamos nuevamente
                     * el formulario porque la Activity será cerrada.
                     */
                    guardarSesion(
                        usuario = resultado.usuarioLocal,
                        idUsuarioApi = resultado.idUsuarioApi,
                        token = resultado.token,
                        sesionRemota = true
                    )

                    abrirPantallaPrincipal(
                        resultado.usuarioLocal
                    )
                }

                is ResultadoLoginApi.CredencialesInvalidas -> {

                    restaurarFormularioLogin()

                    Toast.makeText(
                        this,
                        resultado.mensaje,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ResultadoLoginApi.SinConexion -> {

                    restaurarFormularioLogin()

                    Toast.makeText(
                        this,
                        "Necesitas conexión a Internet para iniciar sesión.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is ResultadoLoginApi.Error -> {

                    restaurarFormularioLogin()

                    Toast.makeText(
                        this,
                        resultado.mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun restaurarFormularioLogin() {

        loginEnProceso = false

        btnLogin.isEnabled = true
        etCorreo.isEnabled = true
        etContrasena.isEnabled = true
        tvRegistro.isEnabled = true
        tvOlvidaste.isEnabled = true

        btnLogin.text = "INICIAR SESIÓN"
    }

    private fun guardarSesion(
        usuario: Usuario,
        idUsuarioApi: Int?,
        token: String?,
        sesionRemota: Boolean
    ) {
        val preferencias = getSharedPreferences(
            "sesion_clinica",
            Context.MODE_PRIVATE
        )

        preferencias.edit().apply {

            putInt(
                "ID_USUARIO",
                usuario.id
            )

            putString(
                "ROL_USUARIO",
                usuario.rol
            )

            putString(
                "NOMBRE_USUARIO",
                "${usuario.nombre} ${usuario.apellido}"
            )

            putBoolean(
                "SESION_REMOTA",
                sesionRemota
            )

            // ID de SQL Server/Azure.
            if (idUsuarioApi != null && idUsuarioApi > 0) {
                putInt(
                    "ID_USUARIO_API",
                    idUsuarioApi
                )
            } else {
                remove("ID_USUARIO_API")
            }

            if (!token.isNullOrBlank()) {
                putString(
                    "TOKEN",
                    token
                )
            } else {
                remove("TOKEN")
            }

            apply()
        }
    }

    private fun abrirPantallaPrincipal(
        usuarioLogueado: Usuario
    ) {
        val esAdministrador = usuarioLogueado.rol.equals(
            "ADMIN",
            ignoreCase = true
        )

        val mensajeBienvenida =
            if (esAdministrador) {
                "Bienvenido ADMIN: ${usuarioLogueado.nombre}"
            } else {
                "Bienvenido PACIENTE: ${usuarioLogueado.nombre}"
            }

        Toast.makeText(
            this,
            mensajeBienvenida,
            Toast.LENGTH_SHORT
        ).show()

        val intentInicio = Intent(
            this,
            InicioActivity::class.java
        ).apply {
            putExtra(
                "ROL_USUARIO",
                usuarioLogueado.rol
            )
        }

        startActivity(intentInicio)
        finish()
    }

    override fun onDestroy() {

        if (::loadingController.isInitialized) {
            loadingController.forceHide()
        }

        super.onDestroy()
    }
}