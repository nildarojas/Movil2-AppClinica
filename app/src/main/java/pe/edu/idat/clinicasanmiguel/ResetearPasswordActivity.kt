package pe.edu.idat.clinicasanmiguel

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ResetearPasswordActivity : AppCompatActivity() {

    private lateinit var etCorreo: TextInputEditText
    private lateinit var etCodigo: TextInputEditText
    private lateinit var etNuevaPassword: TextInputEditText
    private lateinit var tilNuevaPassword: TextInputLayout
    private lateinit var btnVerificarCodigo: MaterialButton
    private lateinit var btnRestablecer: MaterialButton
    private lateinit var tvTemporizador: TextView
    private lateinit var tvReenviarCodigo: TextView

    private var timerReenvio: CountDownTimer? = null
    private val TIEMPO_ESPERA_REENVIO: Long = 30000
    private var codigoVerificado = false
    private var timer: CountDownTimer? = null
    private var tiempoRestanteMillis: Long = 120000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restablecer_password)

        etCorreo = findViewById(R.id.etCorreoReset)
        etCodigo = findViewById(R.id.etCodigoReset)
        etNuevaPassword = findViewById(R.id.etNuevaPasswordReset)
        tilNuevaPassword = findViewById(R.id.tilNuevaPasswordReset)
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo)
        btnRestablecer = findViewById(R.id.btnRestablecerPassword)
        tvTemporizador = findViewById(R.id.tvTemporizadorCodigo)
        tvReenviarCodigo = findViewById(R.id.tvReenviarCodigo)

        val correoRecibido = intent.getStringExtra("correo")
        etCorreo.setText(correoRecibido ?: "")
        etCorreo.isEnabled = false

        bloquearNuevaPassword()
        iniciarTemporizador()
        iniciarContadorReenvio()

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

    private fun bloquearNuevaPassword() {
        codigoVerificado = false
        tilNuevaPassword.visibility = View.GONE
        btnRestablecer.visibility = View.GONE
        etNuevaPassword.setText("")
        btnVerificarCodigo.isEnabled = true
        btnVerificarCodigo.alpha = 1.0f
        btnVerificarCodigo.text = "VERIFICAR CÓDIGO"
    }

    private fun habilitarNuevaPassword() {
        codigoVerificado = true
        tilNuevaPassword.visibility = View.VISIBLE
        btnRestablecer.visibility = View.VISIBLE
        btnVerificarCodigo.isEnabled = false
        btnVerificarCodigo.alpha = 0.5f
        btnVerificarCodigo.text = "CÓDIGO VÁLIDO"
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
                bloquearNuevaPassword()

                Toast.makeText(this@ResetearPasswordActivity, "Código expirado, vuelva a intentarlo", Toast.LENGTH_LONG).show()

                val intent = Intent(this@ResetearPasswordActivity, SolicitarRecuperacionActivity::class.java)
                intent.putExtra("correo", etCorreo.text.toString().trim())
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }.start()
    }

    private fun verificarCodigoLocal() {
        val codigo = etCodigo.text.toString().trim()

        if (codigo.isEmpty()) {
            Toast.makeText(this, "Ingresa el código", Toast.LENGTH_SHORT).show()
            return
        }

        if (codigo == "123456") {
            Toast.makeText(this, "Código verificado con éxito", Toast.LENGTH_SHORT).show()
            habilitarNuevaPassword()
        } else {
            Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show()
            bloquearNuevaPassword()
        }
    }

    private fun resetearPasswordLocal() {
        if (!codigoVerificado) {
            Toast.makeText(this, "Primero verifica el código", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevaPassword = etNuevaPassword.text.toString().trim()

        if (nuevaPassword.isEmpty()) {
            Toast.makeText(this, "Ingresa tu nueva contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        if (nuevaPassword.length < 6) {
            Toast.makeText(this, "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Contraseña restablecida correctamente", Toast.LENGTH_LONG).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun reenviarCodigoLocal() {
        Toast.makeText(this, "Se envió un nuevo código local", Toast.LENGTH_SHORT).show()
        etCodigo.setText("")
        bloquearNuevaPassword()
        iniciarTemporizador()
        iniciarContadorReenvio()
    }

    override fun onDestroy() {
        timer?.cancel()
        timerReenvio?.cancel()
        super.onDestroy()
    }
}