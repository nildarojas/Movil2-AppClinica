package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.CitaApiResponse
import java.util.Locale

class CitasAdapter(
    private val lista: List<CitaApiResponse>,
    private val esHistorial: Boolean = false,
    private val onCancelarCita: (CitaApiResponse) -> Unit = {},
    private val onReprogramarCita: (CitaApiResponse) -> Unit = {}
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    private var accionesHabilitadas =
        false

    class CitaViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val card: MaterialCardView =
            view.findViewById(
                R.id.cardCitaPaciente
            )

        val tvEspecialidad: TextView =
            view.findViewById(
                R.id.tvEspecialidadItem
            )

        val tvMedico: TextView =
            view.findViewById(
                R.id.tvDoctorItem
            )

        val tvFechaHora: TextView =
            view.findViewById(
                R.id.tvFechaHoraItem
            )

        val tvEstado: TextView =
            view.findViewById(
                R.id.tvEstadoItem
            )

        val btnReprogramar: MaterialButton =
            view.findViewById(
                R.id.btnReprogramarItem
            )

        val btnCancelar: MaterialButton =
            view.findViewById(
                R.id.btnCancelarItem
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CitaViewHolder {
        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_cita_paciente,
                parent,
                false
            )

        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CitaViewHolder,
        position: Int
    ) {
        val item =
            lista[position]

        val estado =
            item.estado.uppercase(
                Locale.ROOT
            )

        holder.tvEspecialidad.text =
            item.especialidad

        holder.tvMedico.text =
            item.medico

        holder.tvFechaHora.text =
            item.fechaHora

        holder.tvEstado.text =
            item.estado

        holder.btnCancelar
            .setOnClickListener(null)

        holder.btnReprogramar
            .setOnClickListener(null)

        if (position == 0) {
            holder.card.strokeColor =
                0xFF458890.toInt()

            holder.card.strokeWidth = 6
        } else {
            holder.card.strokeColor =
                0xFFE2E8F0.toInt()

            holder.card.strokeWidth = 2
        }

        when (estado) {
            "CANCELADA" -> {
                holder.card.alpha = 0.5f

                holder.btnCancelar.visibility =
                    View.GONE

                holder.btnReprogramar.visibility =
                    View.GONE

                holder.tvEstado.setBackgroundColor(
                    0xFFFFEBEE.toInt()
                )

                holder.tvEstado.setTextColor(
                    0xFFC62828.toInt()
                )
            }

            "REPROGRAMADA" -> {
                holder.card.alpha = 0.6f

                holder.tvFechaHora.text =
                    "Fecha anterior: ${item.fechaHora}"

                holder.btnCancelar.visibility =
                    View.GONE

                holder.btnReprogramar.visibility =
                    View.GONE

                holder.tvEstado.setBackgroundColor(
                    0xFFF5F5F5.toInt()
                )

                holder.tvEstado.setTextColor(
                    0xFF616161.toInt()
                )
            }

            else -> {
                holder.card.alpha = 1.0f

                holder.btnCancelar.visibility =
                    if (esHistorial) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }

                holder.btnReprogramar.visibility =
                    if (esHistorial) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }

                holder.tvEstado.setBackgroundColor(
                    0xFFE8F5E9.toInt()
                )

                holder.tvEstado.setTextColor(
                    0xFF2E7D32.toInt()
                )
            }
        }

        val puedeModificarCita =
            !esHistorial &&
                    estado != "CANCELADA" &&
                    estado != "REPROGRAMADA"

        val botonesHabilitados =
            puedeModificarCita &&
                    accionesHabilitadas

        holder.btnReprogramar.isEnabled =
            botonesHabilitados

        holder.btnCancelar.isEnabled =
            botonesHabilitados

        val alphaAcciones =
            if (botonesHabilitados) {
                1.0f
            } else {
                0.45f
            }

        holder.btnReprogramar.alpha =
            alphaAcciones

        holder.btnCancelar.alpha =
            alphaAcciones

        if (botonesHabilitados) {
            holder.btnReprogramar
                .setOnClickListener {
                    onReprogramarCita(item)
                }

            holder.btnCancelar
                .setOnClickListener {
                    val context =
                        holder.itemView.context

                    AlertDialog.Builder(context)
                        .setTitle(
                            "Confirmar cancelación"
                        )
                        .setMessage(
                            "¿Seguro que deseas cancelar esta cita?"
                        )
                        .setPositiveButton(
                            "Sí"
                        ) { _, _ ->
                            onCancelarCita(item)
                        }
                        .setNegativeButton(
                            "No",
                            null
                        )
                        .show()
                }
        }
    }

    fun actualizarAccionesHabilitadas(
        habilitadas: Boolean
    ) {
        if (accionesHabilitadas == habilitadas) {
            return
        }

        accionesHabilitadas =
            habilitadas

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int =
        lista.size
}