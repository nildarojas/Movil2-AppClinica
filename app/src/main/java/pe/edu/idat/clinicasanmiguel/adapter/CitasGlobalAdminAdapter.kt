package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
class CitasGlobalAdminAdapter(private val lista: List<CitaGlobalMock>) :
    RecyclerView.Adapter<CitasGlobalAdminAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPaciente: TextView = view.findViewById(R.id.tvPacienteCitaAdmin)
        val tvMedico: TextView = view.findViewById(R.id.tvMedicoCitaAdmin)
        val tvHorario: TextView = view.findViewById(R.id.tvHorarioCitaAdmin)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoCitaAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita_admin, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val item = lista[position]
        holder.tvPaciente.text = "Paciente: ${item.paciente}"
        holder.tvMedico.text = "Médico: ${item.medico}"
        holder.tvHorario.text = "Horario: ${item.horario}"
        holder.tvEstado.text = item.estado

        if (item.estado == "CANCELADA") {
            holder.tvEstado.setBackgroundColor(0xFFFFEBEE.toInt())
            holder.tvEstado.setTextColor(0xFFC62828.toInt())
        }
    }

    override fun getItemCount(): Int = lista.size
}