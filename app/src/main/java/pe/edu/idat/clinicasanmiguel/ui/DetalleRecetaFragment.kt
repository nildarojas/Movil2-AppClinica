package pe.edu.idat.clinicasanmiguel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.RecetaApiResponse
import java.text.SimpleDateFormat
import java.util.Locale

class DetalleRecetaFragment :
    Fragment(R.layout.fragment_detalle_receta) {

    private lateinit var btnVolver:
            MaterialButton

    private lateinit var tvNumeroCita:
            TextView

    private lateinit var tvEspecialidad:
            TextView

    private lateinit var tvMedico:
            TextView

    private lateinit var tvFechaCita:
            TextView

    private lateinit var tvFechaAtencion:
            TextView

    private lateinit var tvDiagnostico:
            TextView

    private lateinit var tvTituloMedicamentos:
            TextView

    private lateinit var llMedicamentos:
            LinearLayout

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        enlazarVistas(
            view
        )

        configurarBotonVolver()

        mostrarInformacion()
    }

    private fun enlazarVistas(
        view: View
    ) {
        btnVolver =
            view.findViewById(
                R.id.btnVolverDetalleReceta
            )

        tvNumeroCita =
            view.findViewById(
                R.id.tvNumeroCitaDetalleReceta
            )

        tvEspecialidad =
            view.findViewById(
                R.id.tvEspecialidadDetalleReceta
            )

        tvMedico =
            view.findViewById(
                R.id.tvMedicoDetalleReceta
            )

        tvFechaCita =
            view.findViewById(
                R.id.tvFechaCitaDetalleReceta
            )

        tvFechaAtencion =
            view.findViewById(
                R.id.tvFechaAtencionDetalleReceta
            )

        tvDiagnostico =
            view.findViewById(
                R.id.tvDiagnosticoDetalleReceta
            )

        tvTituloMedicamentos =
            view.findViewById(
                R.id.tvTituloMedicamentosDetalleReceta
            )

        llMedicamentos =
            view.findViewById(
                R.id.llMedicamentosDetalleReceta
            )
    }

    private fun configurarBotonVolver() {
        btnVolver.setOnClickListener {
            parentFragmentManager
                .popBackStack()
        }
    }

    private fun mostrarInformacion() {
        val argumentos =
            arguments ?: return

        val idCita =
            argumentos.getInt(
                ARG_ID_CITA
            )

        val medico =
            argumentos.getString(
                ARG_MEDICO
            ).orEmpty()

        val especialidad =
            argumentos.getString(
                ARG_ESPECIALIDAD
            ).orEmpty()

        val fechaHoraCita =
            argumentos.getString(
                ARG_FECHA_CITA
            ).orEmpty()

        val fechaAtencion =
            argumentos.getString(
                ARG_FECHA_ATENCION
            ).orEmpty()

        val diagnostico =
            argumentos.getString(
                ARG_DIAGNOSTICO
            ).orEmpty()

        val medicamentos =
            argumentos.getStringArrayList(
                ARG_MEDICAMENTOS
            ) ?: arrayListOf()

        tvNumeroCita.text =
            "Correspondiente a la cita N.º $idCita"

        tvEspecialidad.text =
            especialidad.ifBlank {
                "Especialidad no disponible"
            }

        tvMedico.text =
            "Médico: ${
                medico.ifBlank {
                    "No disponible"
                }
            }"

        tvFechaCita.text =
            "Fecha de la cita: ${
                fechaHoraCita.ifBlank {
                    "No disponible"
                }
            }"

        tvFechaAtencion.text =
            "Fecha de atención: ${
                formatearFechaIso(
                    fechaAtencion
                )
            }"

        tvDiagnostico.text =
            diagnostico.ifBlank {
                "Sin diagnóstico registrado."
            }

        val cantidad =
            medicamentos.size

        tvTituloMedicamentos.text =
            if (cantidad == 1) {
                "MEDICAMENTO PRESCRITO"
            } else {
                "MEDICAMENTOS PRESCRITOS ($cantidad)"
            }

        mostrarMedicamentos(
            medicamentos
        )
    }

    private fun mostrarMedicamentos(
        medicamentos: List<String>
    ) {
        llMedicamentos.removeAllViews()

        if (medicamentos.isEmpty()) {
            agregarMedicamento(
                numero = "—",
                descripcion =
                    "No se registraron medicamentos."
            )

            return
        }

        medicamentos.forEachIndexed {
                indice,
                medicamento ->

            agregarMedicamento(
                numero =
                    (indice + 1).toString(),
                descripcion =
                    medicamento.ifBlank {
                        "Medicamento sin descripción."
                    }
            )
        }
    }

    private fun agregarMedicamento(
        numero: String,
        descripcion: String
    ) {
        val item =
            LayoutInflater
                .from(requireContext())
                .inflate(
                    R.layout.item_medicamento_receta,
                    llMedicamentos,
                    false
                )

        val tvNumero =
            item.findViewById<TextView>(
                R.id.tvNumeroMedicamento
            )

        val tvDescripcion =
            item.findViewById<TextView>(
                R.id.tvDescripcionMedicamento
            )

        tvNumero.text =
            numero

        tvDescripcion.text =
            descripcion

        llMedicamentos.addView(
            item
        )
    }

    private fun formatearFechaIso(
        valor: String
    ): String {
        if (valor.isBlank()) {
            return "No disponible"
        }

        return try {
            val fechaNormalizada =
                valor.trim().replace(
                    Regex("(\\.\\d{3})\\d+")
                ) {
                    it.groupValues[1]
                }

            val contieneZona =
                fechaNormalizada.endsWith("Z") ||
                        Regex(
                            "[+-]\\d{2}:\\d{2}$"
                        ).containsMatchIn(
                            fechaNormalizada
                        )

            val patronEntrada =
                when {
                    contieneZona &&
                            fechaNormalizada.contains(".") -> {
                        "yyyy-MM-dd'T'HH:mm:ss.SSSX"
                    }

                    contieneZona -> {
                        "yyyy-MM-dd'T'HH:mm:ssX"
                    }

                    fechaNormalizada.contains(".") -> {
                        "yyyy-MM-dd'T'HH:mm:ss.SSS"
                    }

                    else -> {
                        "yyyy-MM-dd'T'HH:mm:ss"
                    }
                }

            val formatoEntrada =
                SimpleDateFormat(
                    patronEntrada,
                    Locale.US
                )

            val fecha =
                formatoEntrada.parse(
                    fechaNormalizada
                )

            if (fecha == null) {
                formatearFechaSimple(
                    valor
                )
            } else {
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(
                    fecha
                )
            }

        } catch (exception: Exception) {
            formatearFechaSimple(
                valor
            )
        }
    }

    private fun formatearFechaSimple(
        valor: String
    ): String {
        return valor
            .replace(
                "T",
                " "
            )
            .substringBefore(
                "."
            )
            .removeSuffix(
                "Z"
            )
    }

    companion object {

        private const val ARG_ID_CONSULTA =
            "ARG_ID_CONSULTA"

        private const val ARG_ID_CITA =
            "ARG_ID_CITA"

        private const val ARG_MEDICO =
            "ARG_MEDICO"

        private const val ARG_ESPECIALIDAD =
            "ARG_ESPECIALIDAD"

        private const val ARG_FECHA_CITA =
            "ARG_FECHA_CITA"

        private const val ARG_FECHA_ATENCION =
            "ARG_FECHA_ATENCION"

        private const val ARG_DIAGNOSTICO =
            "ARG_DIAGNOSTICO"

        private const val ARG_MEDICAMENTOS =
            "ARG_MEDICAMENTOS"

        fun nuevaInstancia(
            receta: RecetaApiResponse
        ): DetalleRecetaFragment {

            return DetalleRecetaFragment()
                .apply {
                    arguments =
                        Bundle().apply {

                            putInt(
                                ARG_ID_CONSULTA,
                                receta.idConsulta
                            )

                            putInt(
                                ARG_ID_CITA,
                                receta.idCita
                            )

                            putString(
                                ARG_MEDICO,
                                receta.medico
                            )

                            putString(
                                ARG_ESPECIALIDAD,
                                receta.especialidad
                            )

                            putString(
                                ARG_FECHA_CITA,
                                receta.fechaHoraCita
                            )

                            putString(
                                ARG_FECHA_ATENCION,
                                receta.fechaAtencion
                            )

                            putString(
                                ARG_DIAGNOSTICO,
                                receta.diagnostico
                            )

                            putStringArrayList(
                                ARG_MEDICAMENTOS,
                                ArrayList(
                                    receta.medicamentos
                                )
                            )
                        }
                }
        }
    }
}