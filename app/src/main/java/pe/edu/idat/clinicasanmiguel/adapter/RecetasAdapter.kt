package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.RecetaApiResponse
import java.text.SimpleDateFormat
import java.util.Locale

class RecetasAdapter(
    private val lista:
    MutableList<RecetaApiResponse>,
    private val onSeleccionarReceta:
        (RecetaApiResponse) -> Unit
) : RecyclerView.Adapter<RecetasAdapter.RecetaViewHolder>() {

    class RecetaViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val tvEspecialidad: TextView =
            view.findViewById(
                R.id.tvEspecialidadReceta
            )

        val tvMedico: TextView =
            view.findViewById(
                R.id.tvMedicoReceta
            )

        val tvFechaCita: TextView =
            view.findViewById(
                R.id.tvFechaCitaReceta
            )

        val tvFechaAtencion: TextView =
            view.findViewById(
                R.id.tvFechaAtencionReceta
            )

        val tvDiagnostico: TextView =
            view.findViewById(
                R.id.tvDiagnosticoReceta
            )

        val tvCantidadMedicamentos: TextView =
            view.findViewById(
                R.id.tvCantidadMedicamentosReceta
            )

        val btnVerReceta: MaterialButton =
            view.findViewById(
                R.id.btnVerReceta
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecetaViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_receta,
                    parent,
                    false
                )

        return RecetaViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: RecetaViewHolder,
        position: Int
    ) {
        val receta =
            lista[position]

        holder.tvEspecialidad.text =
            receta.especialidad.ifBlank {
                "Especialidad no disponible"
            }

        holder.tvMedico.text =
            "Médico: ${
                receta.medico.ifBlank {
                    "No disponible"
                }
            }"

        holder.tvFechaCita.text =
            "Cita: ${
                receta.fechaHoraCita.ifBlank {
                    "Fecha no disponible"
                }
            }"

        holder.tvFechaAtencion.text =
            "Atendida: ${
                formatearFechaIso(
                    receta.fechaAtencion
                )
            }"

        holder.tvDiagnostico.text =
            receta.diagnostico.ifBlank {
                "Sin diagnóstico registrado"
            }

        val cantidadMedicamentos =
            receta.medicamentos.size

        holder.tvCantidadMedicamentos.text =
            if (cantidadMedicamentos == 1) {
                "1 medicamento prescrito"
            } else {
                "$cantidadMedicamentos medicamentos prescritos"
            }

        holder.btnVerReceta
            .setOnClickListener {
                onSeleccionarReceta(
                    receta
                )
            }

        holder.itemView
            .setOnClickListener {
                onSeleccionarReceta(
                    receta
                )
            }
    }

    override fun getItemCount(): Int =
        lista.size

    fun actualizarLista(
        nuevasRecetas:
        List<RecetaApiResponse>
    ) {
        lista.clear()
        lista.addAll(
            nuevasRecetas
        )

        notifyDataSetChanged()
    }

    private fun formatearFechaIso(
        valor: String
    ): String {
        if (valor.isBlank()) {
            return "Fecha no disponible"
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
}