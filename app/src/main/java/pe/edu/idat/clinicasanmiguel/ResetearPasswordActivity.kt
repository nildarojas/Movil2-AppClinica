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
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
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
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.network.RecuperacionPasswordApiResponse
import pe.edu.idat.clinicasanmiguel.network.ResetearPasswordApiRequest
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import pe.edu.idat.clinicasanmiguel.network.SolicitarRecuperacionApiRequest
import pe.edu.idat.clinicasanmiguel.network.SolicitarRecuperacionApiResponse
import pe.edu.idat.clinicasanmiguel.network.VerificarCodigoRecuperacionApiRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private var esValidoMinimo = false
    private var esValidoMayuscula = false
    private var esValidoNumero = false
    private var esValidoCoincidencia = false

    private var correoRecuperacion = ""
    private var codigoRecuperacion = ""
    private var codigoVerificado = false
    private var operacionEnCurso = false
    private var reenviarDisponible = false

    private var dialogCargando: Dialog? = null
    private var timerReenvio: CountDownTimer? = null
    private var timerExpiracion: CountDownTimer? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_restablecer_password
        )

        tvTituloReset =
            findViewById(
                R.id.tvTituloReset
            )

        tvCorreoConfirmado =
            findViewById(
                R.id.tvCorreoConfirmado
            )

        etCorreo =
            findViewById(
                R.id.etCorreoReset
            )

        etCodigo =
            findViewById(
                R.id.etCodigoReset
            )

        etNuevaPassword =
            findViewById(
                R.id.etNuevaPasswordReset
            )

        etRepetirPassword =
            findViewById(
                R.id.etRepetirPasswordReset
            )

        tilNuevaPassword =
            findViewById(
                R.id.tilNuevaPasswordReset
            )

        tilRepetirPassword =
            findViewById(
                R.id.tilRepetirPasswordReset
            )

        btnEnviarCodigo =
            findViewById(
                R.id.btnEnviarCodigo
            )

        btnVerificarCodigo =
            findViewById(
                R.id.btnVerificarCodigo
            )

        btnRestablecer =
            findViewById(
                R.id.btnRestablecerPassword
            )

        tvTemporizador =
            findViewById(
                R.id.tvTemporizadorCodigo
            )

        tvReenviarCodigo =
            findViewById(
                R.id.tvReenviarCodigo
            )

        layoutPaso1Correo =
            findViewById(
                R.id.layoutPaso1Correo
            )

        layoutPaso2Codigo =
            findViewById(
                R.id.layoutPaso2Codigo
            )

        layoutPaso3Password =
            findViewById(
                R.id.layoutPaso3Password
            )

        cardRequisitosPassword =
            findViewById(
                R.id.cardRequisitosPassword
            )

        reqMinCaracteres =
            findViewById(
                R.id.reqMinCaracteres
            )

        reqMayuscula =
            findViewById(
                R.id.reqMayuscula
            )

        reqNumero =
            findViewById(
                R.id.reqNumero
            )

        reqCoinciden =
            findViewById(
                R.id.reqCoinciden
            )

        digitBoxes =
            arrayOf(
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
            solicitarCodigoDesdeApi(
                esReenvio = false
            )
        }

        btnVerificarCodigo.setOnClickListener {
            verificarCodigoDesdeApi()
        }

        btnRestablecer.setOnClickListener {
            validarNuevaPassword()
        }

        tvReenviarCodigo.setOnClickListener {
            if (reenviarDisponible) {
                solicitarCodigoDesdeApi(
                    esReenvio = true
                )
            }
        }
    }

    private fun mostrarPaso1() {
        timerExpiracion?.cancel()
        timerReenvio?.cancel()

        codigoVerificado = false
        codigoRecuperacion = ""
        reenviarDisponible = false

        tvTituloReset.text =
            "Recuperar contraseña"

        layoutPaso1Correo.visibility =
            View.VISIBLE

        layoutPaso2Codigo.visibility =
            View.GONE

        layoutPaso3Password.visibility =
            View.GONE

        tvTemporizador.text =
            ""

        tvReenviarCodigo.text =
            ""

        limpiarCasillerosCodigo(
            solicitarFoco = false
        )

        etNuevaPassword.text?.clear()
        etRepetirPassword.text?.clear()

        actualizarControles()
    }

    private fun mostrarPaso2(
        correo: String,
        expiracionMinutos: Int
    ) {
        correoRecuperacion =
            correo.trim().lowercase()

        codigoVerificado =
            false

        codigoRecuperacion =
            ""

        tvTituloReset.text =
            "Restablecer contraseña"

        tvCorreoConfirmado.text =
            "Correo: $correoRecuperacion"

        layoutPaso1Correo.visibility =
            View.GONE

        layoutPaso2Codigo.visibility =
            View.VISIBLE

        layoutPaso3Password.visibility =
            View.GONE

        limpiarCasillerosCodigo(
            solicitarFoco = true
        )

        iniciarTemporizador(
            expiracionMinutos
        )

        iniciarContadorReenvio()
    }

    private fun mostrarPaso3() {
        tvTituloReset.text =
            "Nueva contraseña"

        layoutPaso1Correo.visibility =
            View.GONE

        layoutPaso2Codigo.visibility =
            View.GONE

        layoutPaso3Password.visibility =
            View.VISIBLE

        etNuevaPassword.requestFocus()
    }

    private fun solicitarCodigoDesdeApi(
        esReenvio: Boolean
    ) {
        val correo =
            if (esReenvio) {
                correoRecuperacion
            } else {
                etCorreo.text
                    ?.toString()
                    ?.trim()
                    ?.lowercase()
                    .orEmpty()
            }

        if (
            correo.isEmpty() ||
            !Patterns.EMAIL_ADDRESS
                .matcher(correo)
                .matches()
        ) {
            Toast.makeText(
                this,
                "Ingresa un correo electrónico válido",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (
            !iniciarOperacion(
                if (esReenvio) {
                    "Reenviando código..."
                } else {
                    "Enviando código..."
                }
            )
        ) {
            return
        }

        val request =
            SolicitarRecuperacionApiRequest(
                correo = correo
            )

        RetrofitClient
            .obtenerApiService(this)
            .solicitarRecuperacion(request)
            .enqueue(
                object :
                    Callback<SolicitarRecuperacionApiResponse> {

                    override fun onResponse(
                        call: Call<SolicitarRecuperacionApiResponse>,
                        response: Response<SolicitarRecuperacionApiResponse>
                    ) {
                        finalizarOperacion()

                        if (isFinishing || isDestroyed) {
                            return
                        }

                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (
                                respuesta == null ||
                                !respuesta.exito
                            ) {
                                Toast.makeText(
                                    this@ResetearPasswordActivity,
                                    respuesta?.mensaje
                                        ?: "La API devolvió una respuesta incompleta",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            Toast.makeText(
                                this@ResetearPasswordActivity,
                                respuesta.mensaje,
                                Toast.LENGTH_LONG
                            ).show()

                            mostrarPaso2(
                                correo = correo,
                                expiracionMinutos =
                                    respuesta
                                        .expiracionMinutos
                                        ?: 5
                            )

                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<SolicitarRecuperacionApiResponse>,
                        throwable: Throwable
                    ) {
                        finalizarOperacion()

                        if (isFinishing || isDestroyed) {
                            return
                        }

                        Toast.makeText(
                            this@ResetearPasswordActivity,
                            "No se pudo conectar con el servidor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun verificarCodigoDesdeApi() {
        val codigo =
            digitBoxes.joinToString(
                separator = ""
            ) {
                it.text.toString()
            }

        if (
            correoRecuperacion.isBlank()
        ) {
            Toast.makeText(
                this,
                "Primero solicita un código",
                Toast.LENGTH_SHORT
            ).show()

            mostrarPaso1()
            return
        }

        if (
            !codigo.matches(
                Regex("^\\d{6}$")
            )
        ) {
            Toast.makeText(
                this,
                "Ingresa el código completo de 6 dígitos",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (
            !iniciarOperacion(
                "Validando código..."
            )
        ) {
            return
        }

        val request =
            VerificarCodigoRecuperacionApiRequest(
                correo =
                    correoRecuperacion,
                codigo =
                    codigo
            )

        RetrofitClient
            .obtenerApiService(this)
            .verificarCodigoRecuperacion(
                request
            )
            .enqueue(
                object :
                    Callback<RecuperacionPasswordApiResponse> {

                    override fun onResponse(
                        call: Call<RecuperacionPasswordApiResponse>,
                        response: Response<RecuperacionPasswordApiResponse>
                    ) {
                        finalizarOperacion()

                        if (isFinishing || isDestroyed) {
                            return
                        }

                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (
                                respuesta == null ||
                                !respuesta.exito
                            ) {
                                Toast.makeText(
                                    this@ResetearPasswordActivity,
                                    respuesta?.mensaje
                                        ?: "No se pudo verificar el código",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            codigoRecuperacion =
                                codigo

                            codigoVerificado =
                                true

                            Toast.makeText(
                                this@ResetearPasswordActivity,
                                respuesta.mensaje,
                                Toast.LENGTH_SHORT
                            ).show()

                            mostrarPaso3()
                            return
                        }

                        procesarErrorRespuesta(
                            response = response,
                            reiniciarSiCodigoInvalido =
                                true
                        )
                    }

                    override fun onFailure(
                        call: Call<RecuperacionPasswordApiResponse>,
                        throwable: Throwable
                    ) {
                        finalizarOperacion()

                        if (isFinishing || isDestroyed) {
                            return
                        }

                        Toast.makeText(
                            this@ResetearPasswordActivity,
                            "No se pudo verificar el código",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun validarNuevaPassword() {
        if (
            !codigoVerificado ||
            codigoRecuperacion.isBlank()
        ) {
            Toast.makeText(
                this,
                "Primero verifica el código",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val nuevaPassword =
            etNuevaPassword.text
                ?.toString()
                .orEmpty()

        val confirmarPassword =
            etRepetirPassword.text
                ?.toString()
                .orEmpty()

        if (
            !esValidoMinimo ||
            !esValidoMayuscula ||
            !esValidoNumero ||
            !esValidoCoincidencia
        ) {
            Toast.makeText(
                this,
                "Cumple con todos los requisitos de seguridad",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        resetearPasswordDesdeApi(
            nuevaPassword =
                nuevaPassword,
            confirmarPassword =
                confirmarPassword
        )
    }

    private fun resetearPasswordDesdeApi(
        nuevaPassword: String,
        confirmarPassword: String
    ) {
        if (
            !iniciarOperacion(
                "Guardando nueva contraseña..."
            )
        ) {
            return
        }

        val request =
            ResetearPasswordApiRequest(
                correo =
                    correoRecuperacion,
                codigo =
                    codigoRecuperacion,
                nuevaPassword =
                    nuevaPassword,
                confirmarPassword =
                    confirmarPassword
            )

        RetrofitClient
            .obtenerApiService(this)
            .resetearPassword(request)
            .enqueue(
                object :
                    Callback<RecuperacionPasswordApiResponse> {

                    override fun onResponse(
                        call: Call<RecuperacionPasswordApiResponse>,
                        response: Response<RecuperacionPasswordApiResponse>
                    ) {
                        finalizarOperacion()

                        if (isFinishing || isDestroyed) {
                            return
                        }

                        if (response.isSuccessful) {
                            val respuesta =
                                response.body()

                            if (
                                respuesta == null ||
                                !respuesta.exito
                            ) {
                                Toast.makeText(
                                    this@ResetearPasswordActivity,
                                    respuesta?.mensaje
                                        ?: "No se pudo restablecer la contraseña",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            timerExpiracion?.cancel()
                            timerReenvio?.cancel()

                            Toast.makeText(
                                this@ResetearPasswordActivity,
                                respuesta.mensaje,
                                Toast.LENGTH_LONG
                            ).show()

                            abrirLogin()
                            return
                        }

                        procesarErrorRespuesta(
                            response = response,
                            reiniciarSiCodigoInvalido =
                                true
                        )
                    }

                    override fun onFailure(
                        call: Call<RecuperacionPasswordApiResponse>,
                        throwable: Throwable
                    ) {
                        finalizarOperacion()

                        if (isFinishing || isDestroyed) {
                            return
                        }

                        Toast.makeText(
                            this@ResetearPasswordActivity,
                            "No se pudo restablecer la contraseña",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun configurarCasillerosCodigo() {
        for (i in digitBoxes.indices) {
            actualizarBordeCasillero(
                digitBoxes[i],
                digitBoxes[i].text.isNotEmpty()
            )

            digitBoxes[i]
                .addTextChangedListener(
                    object : TextWatcher {

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }

                        override fun afterTextChanged(
                            s: Editable?
                        ) {
                            val tieneTexto =
                                s?.isNotEmpty() == true

                            actualizarBordeCasillero(
                                digitBoxes[i],
                                tieneTexto
                            )

                            if (
                                tieneTexto &&
                                i < digitBoxes.size - 1
                            ) {
                                digitBoxes[i + 1]
                                    .requestFocus()
                            }

                            val codigoCompleto =
                                digitBoxes.joinToString(
                                    separator = ""
                                ) {
                                    it.text.toString()
                                }

                            etCodigo.setText(
                                codigoCompleto
                            )
                        }
                    }
                )

            digitBoxes[i]
                .setOnKeyListener {
                        _,
                        keyCode,
                        event ->

                    if (
                        keyCode ==
                        KeyEvent.KEYCODE_DEL &&
                        event.action ==
                        KeyEvent.ACTION_DOWN &&
                        digitBoxes[i].text.isEmpty() &&
                        i > 0
                    ) {
                        digitBoxes[i - 1]
                            .requestFocus()

                        digitBoxes[i - 1]
                            .setText("")

                        true
                    } else {
                        false
                    }
                }
        }
    }

    private fun actualizarBordeCasillero(
        editText: EditText,
        lleno: Boolean
    ) {
        val drawable =
            editText.background
                ?.mutate() as? GradientDrawable
                ?: return

        val colorBorde =
            if (lleno) {
                Color.parseColor("#2E7D32")
            } else {
                Color.parseColor("#CFD8DC")
            }

        val grosor =
            if (lleno) {
                4
            } else {
                3
            }

        drawable.setStroke(
            grosor,
            colorBorde
        )
    }

    private fun configurarAnimacionRequisitos() {
        val watcher =
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                    val pass1 =
                        etNuevaPassword.text
                            ?.toString()
                            .orEmpty()

                    val pass2 =
                        etRepetirPassword.text
                            ?.toString()
                            .orEmpty()

                    val estaVacio =
                        pass1.isEmpty()

                    esValidoMinimo =
                        pass1.length >= 6

                    esValidoMayuscula =
                        pass1.any {
                            it.isUpperCase()
                        }

                    esValidoNumero =
                        pass1.any {
                            it.isDigit()
                        }

                    esValidoCoincidencia =
                        pass1.isNotEmpty() &&
                                pass1 == pass2

                    animarColorRequisito(
                        reqMinCaracteres,
                        estaVacio,
                        esValidoMinimo
                    )

                    animarColorRequisito(
                        reqMayuscula,
                        estaVacio,
                        esValidoMayuscula
                    )

                    animarColorRequisito(
                        reqNumero,
                        estaVacio,
                        esValidoNumero
                    )

                    animarColorRequisito(
                        reqCoinciden,
                        pass2.isEmpty(),
                        esValidoCoincidencia
                    )
                }
            }

        etNuevaPassword
            .addTextChangedListener(
                watcher
            )

        etRepetirPassword
            .addTextChangedListener(
                watcher
            )
    }

    private fun animarColorRequisito(
        textView: TextView,
        estaVacio: Boolean,
        cumplido: Boolean
    ) {
        val colorTextoOriginal =
            textView.currentTextColor

        val colorTextoDestino =
            when {
                estaVacio ->
                    Color.parseColor("#9E9E9E")

                cumplido ->
                    Color.parseColor("#2E7D32")

                else ->
                    Color.parseColor("#E53935")
            }

        if (
            colorTextoOriginal !=
            colorTextoDestino
        ) {
            ValueAnimator.ofObject(
                ArgbEvaluator(),
                colorTextoOriginal,
                colorTextoDestino
            ).apply {
                duration = 300

                addUpdateListener {
                    textView.setTextColor(
                        it.animatedValue as Int
                    )
                }

                start()
            }
        }
    }

    private fun iniciarTemporizador(
        expiracionMinutos: Int
    ) {
        timerExpiracion?.cancel()

        val duracion =
            expiracionMinutos
                .coerceAtLeast(1)
                .toLong() *
                    60_000L

        timerExpiracion =
            object :
                CountDownTimer(
                    duracion,
                    1_000L
                ) {

                override fun onTick(
                    millisUntilFinished: Long
                ) {
                    val minutos =
                        millisUntilFinished /
                                1_000L /
                                60L

                    val segundos =
                        millisUntilFinished /
                                1_000L %
                                60L

                    tvTemporizador.text =
                        String.format(
                            "El código expira en %02d:%02d",
                            minutos,
                            segundos
                        )
                }

                override fun onFinish() {
                    codigoVerificado =
                        false

                    codigoRecuperacion =
                        ""

                    Toast.makeText(
                        this@ResetearPasswordActivity,
                        "El código ha expirado. Solicita uno nuevo.",
                        Toast.LENGTH_LONG
                    ).show()

                    mostrarPaso1()
                }
            }.start()
    }

    private fun iniciarContadorReenvio() {
        timerReenvio?.cancel()

        reenviarDisponible =
            false

        actualizarControles()

        timerReenvio =
            object :
                CountDownTimer(
                    30_000L,
                    1_000L
                ) {

                override fun onTick(
                    millisUntilFinished: Long
                ) {
                    val segundos =
                        millisUntilFinished /
                                1_000L

                    tvReenviarCodigo.text =
                        "¿No te llegó el código? Espera ${segundos}s"
                }

                override fun onFinish() {
                    reenviarDisponible =
                        true

                    tvReenviarCodigo.text =
                        "¿No te llegó el código? Reenviar"

                    actualizarControles()
                }
            }.start()
    }

    private fun limpiarCasillerosCodigo(
        solicitarFoco: Boolean
    ) {
        digitBoxes.forEach {
            it.setText("")
        }

        etCodigo.text?.clear()

        if (solicitarFoco) {
            digitBoxes.firstOrNull()
                ?.requestFocus()
        }
    }

    private fun iniciarOperacion(
        mensaje: String
    ): Boolean {
        if (operacionEnCurso) {
            return false
        }

        operacionEnCurso =
            true

        actualizarControles()
        mostrarModalCargando(mensaje)

        return true
    }

    private fun finalizarOperacion() {
        operacionEnCurso =
            false

        ocultarModalCargando()
        actualizarControles()
    }

    private fun actualizarControles() {
        btnEnviarCodigo.isEnabled =
            !operacionEnCurso

        btnVerificarCodigo.isEnabled =
            !operacionEnCurso

        btnRestablecer.isEnabled =
            !operacionEnCurso

        tvReenviarCodigo.isEnabled =
            !operacionEnCurso &&
                    reenviarDisponible

        tvReenviarCodigo.alpha =
            if (
                !operacionEnCurso &&
                reenviarDisponible
            ) {
                1f
            } else {
                0.5f
            }
    }

    private fun mostrarModalCargando(
        mensaje: String
    ) {
        dialogCargando?.dismiss()

        dialogCargando =
            Dialog(this).apply {
                requestWindowFeature(
                    Window.FEATURE_NO_TITLE
                )

                setContentView(
                    R.layout.dialog_cargando
                )

                setCancelable(false)

                window?.setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )

                findViewById<TextView>(
                    R.id.tvMensajeCarga
                )?.text =
                    mensaje

                show()
            }
    }

    private fun ocultarModalCargando() {
        dialogCargando?.dismiss()
        dialogCargando = null
    }

    private fun procesarErrorRespuesta(
        response: Response<*>,
        reiniciarSiCodigoInvalido: Boolean =
            false
    ) {
        val mensaje =
            obtenerMensajeError(response)
                ?: when (response.code()) {
                    400 ->
                        "Los datos enviados no son válidos"

                    404 ->
                        "El correo no se encuentra registrado"

                    500 ->
                        "No se pudo enviar el código de recuperación"

                    else ->
                        "El servidor respondió con el código ${response.code()}"
                }

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()

        if (
            reiniciarSiCodigoInvalido
        ) {
            val texto =
                mensaje.lowercase()

            if (
                texto.contains("expirado") ||
                texto.contains("máximo") ||
                texto.contains(
                    "no existe un código"
                )
            ) {
                mostrarPaso1()
            }
        }
    }

    private fun obtenerMensajeError(
        response: Response<*>
    ): String? {
        return try {
            val contenido =
                response.errorBody()
                    ?.string()

            if (contenido.isNullOrBlank()) {
                null
            } else {
                val json =
                    JSONObject(contenido)

                val mensaje =
                    json.optString(
                        "mensaje"
                    )

                if (mensaje.isNotBlank()) {
                    mensaje
                } else {
                    val errores =
                        json.optJSONObject(
                            "errors"
                        )

                    if (errores != null) {
                        val claves =
                            errores.keys()

                        if (claves.hasNext()) {
                            errores
                                .optJSONArray(
                                    claves.next()
                                )
                                ?.optString(0)
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                        } else {
                            json.optString(
                                "title"
                            ).takeIf {
                                it.isNotBlank()
                            }
                        }
                    } else {
                        json.optString(
                            "title"
                        ).takeIf {
                            it.isNotBlank()
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            null
        }
    }

    private fun abrirLogin() {
        SessionManager(this)
            .limpiarSesion()

        val intent =
            Intent(
                this,
                LoginActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        timerExpiracion?.cancel()
        timerReenvio?.cancel()
        ocultarModalCargando()

        super.onDestroy()
    }
}