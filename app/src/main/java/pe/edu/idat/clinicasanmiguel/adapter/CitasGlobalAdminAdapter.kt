package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.CitaGlobalApiResponse

class CitasGlobalAdminAdapter(
    private var listaCitas: List<CitaGlobalApiResponse>
) : RecyclerView.Adapter<CitasGlobalAdminAdapter.CitaViewHolder>() {

    class CitaViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val tvPaciente: TextView =
            view.findViewById(
                R.id.tvPacienteCitaAdmin
            )

        val tvMedico: TextView =
            view.findViewById(
                R.id.tvMedicoCitaAdmin
            )

        val tvHorario: TextView =
            view.findViewById(
                R.id.tvHorarioCitaAdmin
            )

        val tvEstado: TextView =
            view.findViewById(
                R.id.tvEstadoCitaAdmin
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CitaViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_cita_admin,
                    parent,
                    false
                )

        return CitaViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: CitaViewHolder,
        position: Int
    ) {
        val cita =
            listaCitas[position]

        holder.tvPaciente.text =
            "Paciente: ${cita.paciente}"

        holder.tvMedico.text =
            "Médico: ${cita.medico} (${cita.especialidad})"

        holder.tvHorario.text =
            "Horario: ${cita.fechaHora}"

        val estadoNormalizado =
            cita.estado
                .trim()
                .uppercase()

        holder.tvEstado.text =
            estadoNormalizado

        if (estadoNormalizado == "CANCELADA") {
            holder.tvEstado.setBackgroundColor(
                0xFFFFEBEE.toInt()
            )

            holder.tvEstado.setTextColor(
                0xFFC62828.toInt()
            )
        } else {
            holder.tvEstado.setBackgroundColor(
                0xFFFFF3E0.toInt()
            )

            holder.tvEstado.setTextColor(
                0xFFE65100.toInt()
            )
        }
    }

    override fun getItemCount(): Int =
        listaCitas.size

    fun actualizarLista(
        nuevaLista: List<CitaGlobalApiResponse>
    ) {
        listaCitas =
            nuevaLista

        notifyDataSetChanged()
    }
}