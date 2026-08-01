package pe.edu.idat.clinicasanmiguel

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pe.edu.idat.clinicasanmiguel.repository.UsuarioRepository

class ResetearPasswordActivity : AppCompatActivity() {

    private lateinit var tvTituloReset: TextView
    private lateinit var tvCorreoConfirmado: TextView
    private lateinit var etCorreo: TextInputEditText
    private lateinit var etCodigo: TextInputEditText
    private lateinit var etNuevaPassword: TextInputEditText
    private lateinit var etRepetirPassword: TextInputEditText
    private lateinit var tilNuevaPassword: TextInputLayout
    private lateinit var tilRepetirPassword: TextInputLayout
    private lateinit var btnEnviarCodigo: MaterialButton
    private lateinit var btnVerificarCodigo: MaterialButton
    private lateinit var btnRestablecer: MaterialButton
    private lateinit var tvTemporizador: TextView
    private lateinit var tvReenviarCodigo: TextView
    private lateinit var layoutPaso1Correo: LinearLayout
    private lateinit var layoutPaso2Codigo: LinearLayout
    private lateinit var layoutPaso3Password: LinearLayout
    private lateinit var cardRequisitosPassword: MaterialCardView
    private lateinit var reqMinCaracteres: TextView
    private lateinit var reqMayuscula: TextView
    private lateinit var reqNumero: TextView
    private lateinit var reqCoinciden: TextView
    private lateinit var digitBoxes: Array<EditText>
    private lateinit var usuarioRepository: UsuarioRepository
    private var esValidoMinimo = false
    private var esValidoMayuscula = false
    private var esValidoNumero = false
    private var esValidoCoincidencia = false

    private var dialogCargando: Dialog? = null
    private var timerReenvio: CountDownTimer? = null
    private val TIEMPO_ESPERA_REENVIO: Long = 30000
    private var codigoVerificado = false
    private var timer: CountDownTimer? = null
    private var tiempoRestanteMillis: Long = 120000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restablecer_password)

        usuarioRepository = UsuarioRepository(this)

        tvTituloReset = findViewById(R.id.tvTituloReset)
        tvCorreoConfirmado = findViewById(R.id.tvCorreoConfirmado)
        etCorreo = findViewById(R.id.etCorreoReset)
        etCodigo = findViewById(R.id.etCodigoReset)
        etNuevaPassword = findViewById(R.id.etNuevaPasswordReset)
        etRepetirPassword = findViewById(R.id.etRepetirPasswordReset)
        tilNuevaPassword = findViewById(R.id.tilNuevaPasswordReset)
        tilRepetirPassword = findViewById(R.id.tilRepetirPasswordReset)

        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo)
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo)
        btnRestablecer = findViewById(R.id.btnRestablecerPassword)
        tvTemporizador = findViewById(R.id.tvTemporizadorCodigo)
        tvReenviarCodigo = findViewById(R.id.tvReenviarCodigo)

        layoutPaso1Correo = findViewById(R.id.layoutPaso1Correo)
        layoutPaso2Codigo = findViewById(R.id.layoutPaso2Codigo)
        layoutPaso3Password = findViewById(R.id.layoutPaso3Password)

        cardRequisitosPassword = findViewById(R.id.cardRequisitosPassword)
        reqMinCaracteres = findViewById(R.id.reqMinCaracteres)
        reqMayuscula = findViewById(R.id.reqMayuscula)
        reqNumero = findViewById(R.id.reqNumero)
        reqCoinciden = findViewById(R.id.reqCoinciden)

        digitBoxes = arrayOf(
            findViewById(R.id.etDigit1),
            findViewById(R.id.etDigit2),
            findViewById(R.id.etDigit3),
            findViewById(R.id.etDigit4),
            findViewById(R.id.etDigit5),
            findViewById(R.id.etDigit6)
        )

        configurarCasillerosCodigo()
        configurarAnimacionRequisitos()
        mostrarPaso1()

        btnEnviarCodigo.setOnClickListener {
            validarCorreoYEnviarCodigo()
        }

        btnVerificarCodigo.setOnClickListener {
            verificarCodigoLocal()
        }

        btnRestablecer.setOnClickListener {
            resetearPasswordLocal()
        }

        tvReenviarCodigo.setOnClickListener {
            reenviarCodigoLocal()
        }
    }

    private fun mostrarPaso1() {
        tvTituloReset.text = "Recuperar contraseña"
        layoutPaso1Correo.visibility = View.VISIBLE
        layoutPaso2Codigo.visibility = View.GONE
        layoutPaso3Password.visibility = View.GONE
    }

    private fun validarCorreoYEnviarCodigo() {
        val correo = etCorreo.text.toString().trim()

        if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarModalCargando("Verificando usuario en base de datos...")

        Handler(Looper.getMainLooper()).postDelayed({
            ocultarModalCargando()
            val usuarioEncontrado = usuarioRepository.obtenerUsuarioPorCorreo(correo)

            if (usuarioEncontrado != null) {
                Toast.makeText(this, "Usuario encontrado. Código enviado", Toast.LENGTH_SHORT).show()
                mostrarPaso2(correo)
            } else {
                Toast.makeText(this, "El correo no se encuentra registrado", Toast.LENGTH_LONG).show()
            }
        }, 1200)
    }

    private fun mostrarPaso2(correo: String) {
        tvTituloReset.text = "Restablecer contraseña"
        tvCorreoConfirmado.text = "Correo: $correo"

        layoutPaso1Correo.visibility = View.GONE
        layoutPaso2Codigo.visibility = View.VISIBLE
        layoutPaso3Password.visibility = View.GONE

        iniciarTemporizador()
        iniciarContadorReenvio()
    }

    private fun verificarCodigoLocal() {
        val codigo = etCodigo.text.toString().trim()

        if (codigo.length < 6) {
            Toast.makeText(this, "Ingresa el código completo de 6 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarModalCargando("Validando código...")

        Handler(Looper.getMainLooper()).postDelayed({
            ocultarModalCargando()

            if (codigo == "123456") {
                Toast.makeText(this, "Código verificado con éxito", Toast.LENGTH_SHORT).show()
                codigoVerificado = true
                mostrarPaso3()
            } else {
                Toast.makeText(this, "Código inválido (Prueba con 123456)", Toast.LENGTH_SHORT).show()
            }
        }, 1200)
    }

    private fun mostrarPaso3() {
        tvTituloReset.text = "Nueva contraseña"
        layoutPaso1Correo.visibility = View.GONE
        layoutPaso2Codigo.visibility = View.GONE
        layoutPaso3Password.visibility = View.VISIBLE
    }

    private fun resetearPasswordLocal() {
        if (!codigoVerificado) {
            Toast.makeText(this, "Primero verifica el código", Toast.LENGTH_SHORT).show()
            return
        }

        if (!esValidoMinimo || !esValidoMayuscula || !esValidoNumero || !esValidoCoincidencia) {
            Toast.makeText(this, "Por favor cumple con todos los requisitos de seguridad", Toast.LENGTH_LONG).show()
            return
        }

        val correo = etCorreo.text.toString().trim()
        val nuevaPassword = etNuevaPassword.text.toString().trim()

        mostrarModalCargando("Guardando nueva contraseña...")

        Handler(Looper.getMainLooper()).postDelayed({
            ocultarModalCargando()
            val actualizado = usuarioRepository.actualizarPasswordPorCorreo(correo, nuevaPassword)

            if (actualizado) {
                Toast.makeText(this, "Contraseña actualizada exitosamente en SQLite", Toast.LENGTH_LONG).show()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_LONG).show()
            }
        }, 1500)
    }

    private fun configurarCasillerosCodigo() {
        for (i in digitBoxes.indices) {
            actualizarBordeCasillero(digitBoxes[i], digitBoxes[i].text.isNotEmpty())

            digitBoxes[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val tieneTexto = s?.isNotEmpty() == true
                    actualizarBordeCasillero(digitBoxes[i], tieneTexto)

                    if (tieneTexto && i < digitBoxes.size - 1) {
                        digitBoxes[i + 1].requestFocus()
                    }

                    val codigoCompleto = digitBoxes.joinToString("") { it.text.toString() }
                    etCodigo.setText(codigoCompleto)
                }
            })

            digitBoxes[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (digitBoxes[i].text.isEmpty() && i > 0) {
                        digitBoxes[i - 1].requestFocus()
                        digitBoxes[i - 1].setText("")
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    private fun actualizarBordeCasillero(editText: EditText, lleno: Boolean) {
        val drawable = editText.background.mutate() as? GradientDrawable ?: return
        val colorBorde = if (lleno) Color.parseColor("#2E7D32") else Color.parseColor("#CFD8DC")
        val grosor = if (lleno) 4 else 3
        drawable.setStroke(grosor, colorBorde)
    }

    private fun configurarAnimacionRequisitos() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val pass1 = etNuevaPassword.text.toString()
                val pass2 = etRepetirPassword.text.toString()

                val estaVacio = pass1.isEmpty()
                esValidoMinimo = pass1.length >= 6
                esValidoMayuscula = pass1.any { it.isUpperCase() }
                esValidoNumero = pass1.any { it.isDigit() }
                esValidoCoincidencia = pass1.isNotEmpty() && pass1 == pass2

                animarColorRequisito(reqMinCaracteres, estaVacio, esValidoMinimo)
                animarColorRequisito(reqMayuscula, estaVacio, esValidoMayuscula)
                animarColorRequisito(reqNumero, estaVacio, esValidoNumero)
                animarColorRequisito(reqCoinciden, pass2.isEmpty(), esValidoCoincidencia)
            }
        }

        etNuevaPassword.addTextChangedListener(watcher)
        etRepetirPassword.addTextChangedListener(watcher)
    }

    private fun animarColorRequisito(textView: TextView, estaVacio: Boolean, cumplido: Boolean) {
        val colorTextoOriginal = textView.currentTextColor

        val colorTextoDestino = when {
            estaVacio -> Color.parseColor("#9E9E9E")
            cumplido -> Color.parseColor("#2E7D32")
            else -> Color.parseColor("#E53935")
        }

        if (colorTextoOriginal != colorTextoDestino) {
            val animadorColor = ValueAnimator.ofObject(ArgbEvaluator(), colorTextoOriginal, colorTextoDestino)
            animadorColor.duration = 300
            animadorColor.addUpdateListener { animator ->
                textView.setTextColor(animator.animatedValue as Int)
            }
            animadorColor.start()
        }
    }

    private fun mostrarModalCargando(mensaje: String) {
        dialogCargando = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_cargando)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<TextView>(R.id.tvMensajeCarga)?.text = mensaje
            show()
        }
    }

    private fun ocultarModalCargando() {
        dialogCargando?.dismiss()
    }

    private fun iniciarContadorReenvio() {
        timerReenvio?.cancel()
        tvReenviarCodigo.isEnabled = false
        tvReenviarCodigo.alpha = 0.5f

        timerReenvio = object : CountDownTimer(TIEMPO_ESPERA_REENVIO, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished / 1000
                tvReenviarCodigo.text = "¿No te llegó el código? Espera ${segundos}s"
            }

            override fun onFinish() {
                tvReenviarCodigo.isEnabled = true
                tvReenviarCodigo.alpha = 1.0f
                tvReenviarCodigo.text = "¿No te llegó el código? Reenviar"
            }
        }.start()
    }

    private fun limpiarCasillerosCodigo() {
        for (box in digitBoxes) {
            box.setText("")
        }
        digitBoxes[0].requestFocus()
    }

    private fun iniciarTemporizador() {
        timer?.cancel()
        tiempoRestanteMillis = 120000

        timer = object : CountDownTimer(tiempoRestanteMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tiempoRestanteMillis = millisUntilFinished
                val minutos = (millisUntilFinished / 1000) / 60
                val segundos = (millisUntilFinished / 1000) % 60
                tvTemporizador.text = String.format("El código expira en %02d:%02d", minutos, segundos)
            }

            override fun onFinish() {
                tvTemporizador.text = "Código expirado"
                Toast.makeText(this@ResetearPasswordActivity, "Código expirado, vuelva a intentarlo", Toast.LENGTH_LONG).show()
                mostrarPaso1()
            }
        }.start()
    }

    private fun reenviarCodigoLocal() {
        Toast.makeText(this, "Se envió un nuevo código local", Toast.LENGTH_SHORT).show()
        limpiarCasillerosCodigo()
        iniciarTemporizador()
        iniciarContadorReenvio()
    }

    override fun onDestroy() {
        timer?.cancel()
        timerReenvio?.cancel()
        ocultarModalCargando()
        super.onDestroy()
    }
}