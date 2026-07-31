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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pe.edu.idat.clinicasanmiguel.entity.Usuario
import pe.edu.idat.clinicasanmiguel.repository.ResultadoLoginApi
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var tvRegistro: TextView
    private lateinit var tvOlvidaste: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var usuarioRepository: UsuarioRepository

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        usuarioRepository = UsuarioRepository(this)

        tvRegistro = findViewById(R.id.tvRegistro)
        tvOlvidaste = findViewById(R.id.tvOlvidaste)
        btnLogin = findViewById(R.id.btnLogin)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)

        tvRegistro.setOnClickListener {
            val intent = Intent(
                this,
                RegistroActivity::class.java
            )

            startActivity(intent)
        }

        tvOlvidaste.setOnClickListener {
            val intent = Intent(
                this,
                SolicitarRecuperacionActivity::class.java
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

        mostrarCargando(true)

        usuarioRepository.loginApi(
            correo = correo,
            password = contrasena
        ) { resultado ->

            if (isFinishing || isDestroyed) {
                return@loginApi
            }

            when (resultado) {

                is ResultadoLoginApi.Exito -> {

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

                    mostrarCargando(false)

                    Toast.makeText(
                        this,
                        resultado.mensaje,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ResultadoLoginApi.SinConexion -> {

                    intentarLoginLocal(
                        correo = correo,
                        contrasena = contrasena
                    )
                }

                is ResultadoLoginApi.Error -> {

                    mostrarCargando(false)

                    Toast.makeText(
                        this,
                        resultado.mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun intentarLoginLocal(
        correo: String,
        contrasena: String
    ) {
        val usuarioLogueado = usuarioRepository.login(
            correo = correo,
            password = contrasena
        )

        if (usuarioLogueado != null) {

            guardarSesion(
                usuario = usuarioLogueado,
                idUsuarioApi = null,
                token = null,
                sesionRemota = false
            )

            Toast.makeText(
                this,
                "Sin conexión. Sesión iniciada con datos locales.",
                Toast.LENGTH_LONG
            ).show()

            abrirPantallaPrincipal(usuarioLogueado)

        } else {

            mostrarCargando(false)

            Toast.makeText(
                this,
                "No se pudo conectar con el servidor y las credenciales no existen localmente",
                Toast.LENGTH_LONG
            ).show()
        }
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

    private fun mostrarCargando(
        cargando: Boolean
    ) {
        btnLogin.isEnabled = !cargando

        btnLogin.text =
            if (cargando) {
                "CONECTANDO..."
            } else {
                "INICIAR SESIÓN"
            }
    }
}