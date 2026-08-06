package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.entity.HorarioAdmin

class HorarioAdminAdapter(
    private var listaHorarios:
    List<HorarioAdmin> = emptyList()
) : RecyclerView.Adapter<
        HorarioAdminAdapter.ViewHolder
        >() {

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val tvMedico:
                TextView =
            view.findViewById(
                R.id.tvNombreDoctor
            )

        val tvFecha:
                TextView =
            view.findViewById(
                R.id.tvEspecialidadDoctor
            )

        val tvEstado:
                TextView =
            view.findViewById(
                R.id.tvEstadoDoctor
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_doctor_admin,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val horario =
            listaHorarios[position]

        holder.tvMedico.text =
            if (horario.especialidad.isBlank()) {
                horario.medico
            } else {
                "${horario.medico} (${horario.especialidad})"
            }

        holder.tvFecha.text =
            "Fecha / Hora: ${horario.fechaHoraTexto}"

        val estado =
            horario.estado
                .trim()
                .uppercase()

        holder.tvEstado.text =
            estado

        if (estado == "DISPONIBLE") {
            holder.tvEstado.setTextColor(
                0xFF458890.toInt()
            )

            holder.tvEstado.setBackgroundColor(
                0xFFE0F2F1.toInt()
            )
        } else {
            holder.tvEstado.setTextColor(
                0xFFD50000.toInt()
            )

            holder.tvEstado.setBackgroundColor(
                0xFFFFEBEE.toInt()
            )
        }
    }

    override fun getItemCount(): Int {
        return listaHorarios.size
    }

    fun actualizarLista(
        nuevaLista: List<HorarioAdmin>
    ) {
        listaHorarios =
            nuevaLista

        notifyDataSetChanged()
    }
}