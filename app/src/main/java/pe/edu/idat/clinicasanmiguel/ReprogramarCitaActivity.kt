package pe.edu.idat.clinicasanmiguel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
import pe.edu.idat.clinicasanmiguel.network.ReprogramarCitaApiRequest
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReprogramarCitaActivity :
    AppCompatActivity() {

    private lateinit var acHorario:
            AutoCompleteTextView

    private lateinit var btnConfirmar:
            Button

    private var idCita =
        -1

    private var idMedico =
        -1

    private var horarioOriginal =
        ""

    private var horariosDisponibles =
        emptyList<String>()

    private var procesando =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_reprogramar_cita
        )

        val tvEspecialidad =
            findViewById<TextView>(
                R.id.tvEspecialidadAnterior
            )

        val tvMedico =
            findViewById<TextView>(
                R.id.tvMedicoAnterior
            )

        val tvHorario =
            findViewById<TextView>(
                R.id.tvHorarioAnterior
            )

        acHorario =
            findViewById(
                R.id.acNuevoHorario
            )

        btnConfirmar =
            findViewById(
                R.id.btnConfirmarReprogramacion
            )

        idCita =
            intent.getIntExtra(
                "id_cita",
                -1
            )

        idMedico =
            intent.getIntExtra(
                "id_medico",
                -1
            )

        val especialidadOriginal =
            intent.getStringExtra(
                "especialidad"
            ).orEmpty()

        val medicoOriginal =
            intent.getStringExtra(
                "medico"
            ).orEmpty()

        horarioOriginal =
            intent.getStringExtra(
                "fecha_hora"
            ).orEmpty()

        tvEspecialidad.text =
            "Especialidad: $especialidadOriginal"

        tvMedico.text =
            "Médico: $medicoOriginal"

        tvHorario.text =
            "Horario actual: $horarioOriginal"

        if (
            idCita <= 0 ||
            idMedico <= 0 ||
            horarioOriginal.isBlank()
        ) {
            Toast.makeText(
                this,
                "La información de la cita está incompleta",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        acHorario.setOnClickListener {
            acHorario.showDropDown()
        }

        btnConfirmar.setOnClickListener {
            validarReprogramacion()
        }

        cargarHorariosDesdeApi()
    }

    private fun cargarHorariosDesdeApi() {
        acHorario.isEnabled = false
        btnConfirmar.isEnabled = false

        val apiService =
            RetrofitClient.obtenerApiService(
                this
            )

        apiService
            .listarHorariosConEstado(
                idMedico = idMedico,
                horarioOriginal = horarioOriginal
            )
            .enqueue(
                object :
                    Callback<List<String>> {

                    override fun onResponse(
                        call: Call<List<String>>,
                        response: Response<List<String>>
                    ) {
                        if (isFinishing || isDestroyed) {
                            return
                        }

                        acHorario.isEnabled = true

                        if (response.isSuccessful) {
                            horariosDisponibles =
                                response.body()
                                    ?: emptyList()

                            acHorario.setAdapter(
                                ArrayAdapter(
                                    this@ReprogramarCitaActivity,
                                    android.R.layout
                                        .simple_dropdown_item_1line,
                                    horariosDisponibles
                                )
                            )

                            if (
                                horariosDisponibles.isEmpty()
                            ) {
                                Toast.makeText(
                                    this@ReprogramarCitaActivity,
                                    "El médico no tiene otros horarios disponibles",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            btnConfirmar.isEnabled =
                                true

                            acHorario.showDropDown()
                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<List<String>>,
                        throwable: Throwable
                    ) {
                        if (isFinishing || isDestroyed) {
                            return
                        }

                        acHorario.isEnabled = true

                        Toast.makeText(
                            this@ReprogramarCitaActivity,
                            "No se pudieron cargar los horarios",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun validarReprogramacion() {
        val nuevoHorario =
            acHorario.text
                .toString()
                .trim()

        if (nuevoHorario.isEmpty()) {
            Toast.makeText(
                this,
                "Selecciona el nuevo horario",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (
            !horariosDisponibles.contains(
                nuevoHorario
            )
        ) {
            Toast.makeText(
                this,
                "Selecciona un horario de la lista",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (
            nuevoHorario.contains(
                "(Ocupado por ti)"
            )
        ) {
            Toast.makeText(
                this,
                "Ya tienes una cita en ese horario",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (
            nuevoHorario.contains(
                "(Médico ocupado en este horario)"
            )
        ) {
            Toast.makeText(
                this,
                "El médico ya está ocupado en ese horario",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        AlertDialog.Builder(this)
            .setTitle(
                "Reprogramar cita"
            )
            .setMessage(
                "La cita actual pasará al historial y se generará una nueva cita pendiente."
            )
            .setPositiveButton(
                "Confirmar"
            ) { _, _ ->
                reprogramarCitaDesdeApi(
                    nuevoHorario
                )
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }

    private fun reprogramarCitaDesdeApi(
        nuevoHorario: String
    ) {
        if (procesando) {
            return
        }

        procesando = true
        btnConfirmar.isEnabled = false
        acHorario.isEnabled = false
        btnConfirmar.text =
            "REPROGRAMANDO..."

        val request =
            ReprogramarCitaApiRequest(
                nuevoHorario =
                    nuevoHorario
            )

        val apiService =
            RetrofitClient.obtenerApiService(
                this
            )

        apiService
            .reprogramarCita(
                idCita = idCita,
                request = request
            )
            .enqueue(
                object :
                    Callback<CitaApiResponse> {

                    override fun onResponse(
                        call: Call<CitaApiResponse>,
                        response: Response<CitaApiResponse>
                    ) {
                        restaurarFormulario()

                        if (isFinishing || isDestroyed) {
                            return
                        }

                        if (response.isSuccessful) {
                            val nuevaCita =
                                response.body()

                            if (nuevaCita == null) {
                                Toast.makeText(
                                    this@ReprogramarCitaActivity,
                                    "La API devolvió una respuesta incompleta",
                                    Toast.LENGTH_LONG
                                ).show()

                                return
                            }

                            Toast.makeText(
                                this@ReprogramarCitaActivity,
                                "Cita reprogramada. Nueva cita N° ${nuevaCita.id}",
                                Toast.LENGTH_LONG
                            ).show()

                            val resultado =
                                Intent().apply {
                                    putExtra(
                                        "id_cita_nueva",
                                        nuevaCita.id
                                    )
                                }

                            setResult(
                                Activity.RESULT_OK,
                                resultado
                            )

                            finish()
                            return
                        }

                        procesarErrorRespuesta(
                            response
                        )
                    }

                    override fun onFailure(
                        call: Call<CitaApiResponse>,
                        throwable: Throwable
                    ) {
                        restaurarFormulario()

                        if (isFinishing || isDestroyed) {
                            return
                        }

                        Toast.makeText(
                            this@ReprogramarCitaActivity,
                            "No se pudo reprogramar la cita",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }

    private fun restaurarFormulario() {
        procesando = false
        btnConfirmar.isEnabled = true
        acHorario.isEnabled = true
        btnConfirmar.text =
            "CONFIRMAR CAMBIO"
    }

    private fun procesarErrorRespuesta(
        response: Response<*>
    ) {
        val mensaje =
            obtenerMensajeError(
                response
            )

        when (response.code()) {
            400 -> {
                Toast.makeText(
                    this,
                    mensaje
                        ?: "El nuevo horario no es válido",
                    Toast.LENGTH_LONG
                ).show()
            }

            401 -> {
                cerrarSesion(
                    mensaje
                        ?: "Tu sesión ha vencido"
                )
            }

            403 -> {
                Toast.makeText(
                    this,
                    mensaje
                        ?: "No tienes permiso para reprogramar la cita",
                    Toast.LENGTH_LONG
                ).show()
            }

            404 -> {
                Toast.makeText(
                    this,
                    mensaje
                        ?: "La cita o el horario no fueron encontrados",
                    Toast.LENGTH_LONG
                ).show()
            }

            409 -> {
                Toast.makeText(
                    this,
                    mensaje
                        ?: "El horario ya no se encuentra disponible",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    this,
                    mensaje
                        ?: "El servidor respondió con el código ${response.code()}",
                    Toast.LENGTH_LONG
                ).show()
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
                JSONObject(contenido)
                    .optString(
                        "mensaje"
                    )
                    .takeIf {
                        it.isNotBlank()
                    }
            }
        } catch (exception: Exception) {
            null
        }
    }

    private fun cerrarSesion(
        mensaje: String
    ) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()

        SessionManager(
            this
        ).limpiarSesion()

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
}