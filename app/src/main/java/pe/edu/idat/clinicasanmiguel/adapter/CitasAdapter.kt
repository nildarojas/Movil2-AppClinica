package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
//final
data class CitaMock(val especialidad: String, val doctor: String, val fechaHora: String, val estado: String)
class CitasAdapter(
    private val listaCitas: List<CitaMock>,
    private val onCancelarClick: (Int) -> Unit,
    private val onReprogramarClick: (CitaMock) -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEspecialidad: TextView = view.findViewById(R.id.tvEspecialidadItem)
        val tvDoctor: TextView = view.findViewById(R.id.tvDoctorItem)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHoraItem)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoItem)
        val btnCancelar: Button = view.findViewById(R.id.btnCancelarItem)
        val btnReprogramar: Button = view.findViewById(R.id.btnReprogramarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita_paciente, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = listaCitas[position]
        holder.tvEspecialidad.text = "Especialidad: ${cita.especialidad}"
        holder.tvDoctor.text = "Médico: ${cita.doctor}"
        holder.tvFechaHora.text = cita.fechaHora
        holder.tvEstado.text = cita.estado

        if (cita.estado == "CANCELADA") {
            holder.btnCancelar.isEnabled = false
            holder.btnCancelar.alpha = 0.5f
        } else {
            holder.btnCancelar.isEnabled = true
            holder.btnCancelar.alpha = 1.0f
        }
        holder.btnCancelar.setOnClickListener { onCancelarClick(position) }
        holder.btnReprogramar.setOnClickListener { onReprogramarClick(cita) }
    }

    override fun getItemCount(): Int = listaCitas.size
}