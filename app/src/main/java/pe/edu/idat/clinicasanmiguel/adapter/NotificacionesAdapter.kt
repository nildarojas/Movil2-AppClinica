package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
import java.text.SimpleDateFormat
import java.util.Locale

data class NotificacionUi(
    val id: Int,
    val idCita: Int,
    val mensaje: String,
    val fechaHoraCita: String,
    val medico: String,
    val especialidad: String,
    val fechaGeneracion: String
)

class NotificacionesAdapter(
    private val lista: List<NotificacionUi>
) : RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val tvMensaje: TextView =
            view.findViewById(
                R.id.tvMensajeNoti
            )

        val tvDetalle: TextView =
            view.findViewById(
                R.id.tvDetalleNoti
            )

        val tvFechaCita: TextView =
            view.findViewById(
                R.id.tvFechaCitaNoti
            )

        val tvFechaGeneracion: TextView =
            view.findViewById(
                R.id.tvFechaNoti
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_notificacion,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item =
            lista[position]

        holder.tvMensaje.text =
            item.mensaje

        holder.tvDetalle.text =
            "${item.especialidad} • ${item.medico}"

        holder.tvFechaCita.text =
            "Cita: ${item.fechaHoraCita}"

        holder.tvFechaGeneracion.text =
            "Aviso generado: ${
                formatearFechaIso(
                    item.fechaGeneracion
                )
            }"
    }

    override fun getItemCount(): Int =
        lista.size

    private fun formatearFechaIso(
        valor: String
    ): String {
        if (valor.isBlank()) {
            return ""
        }

        return try {
            val normalizada =
                valor.trim().replace(
                    Regex("(\\.\\d{3})\\d+")
                ) {
                    it.groupValues[1]
                }

            val tieneZona =
                normalizada.endsWith("Z") ||
                        Regex(
                            "[+-]\\d{2}:\\d{2}$"
                        ).containsMatchIn(
                            normalizada
                        )

            val patron =
                when {
                    tieneZona &&
                            normalizada.contains(".") ->
                        "yyyy-MM-dd'T'HH:mm:ss.SSSX"

                    tieneZona ->
                        "yyyy-MM-dd'T'HH:mm:ssX"

                    normalizada.contains(".") ->
                        "yyyy-MM-dd'T'HH:mm:ss.SSS"

                    else ->
                        "yyyy-MM-dd'T'HH:mm:ss"
                }

            val formatoEntrada =
                SimpleDateFormat(
                    patron,
                    Locale.US
                )

            val fecha =
                formatoEntrada.parse(
                    normalizada
                )

            if (fecha == null) {
                formatearFechaSimple(valor)
            } else {
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(fecha)
            }
        } catch (exception: Exception) {
            formatearFechaSimple(valor)
        }
    }

    private fun formatearFechaSimple(
        valor: String
    ): String {
        return valor
            .replace("T", " ")
            .substringBefore(".")
            .removeSuffix("Z")
    }
}