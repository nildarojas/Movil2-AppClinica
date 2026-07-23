package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.entity.MedicoAdmin

class DoctorAdminAdapter(private var listaDoctores: List<MedicoAdmin>) :
    RecyclerView.Adapter<DoctorAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreDoctor)
        val tvEspecialidad: TextView = view.findViewById(R.id.tvEspecialidadDoctor)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoDoctor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medico = listaDoctores[position]
        holder.tvNombre.text = medico.nombre
        holder.tvEspecialidad.text = medico.especialidad
        holder.tvEstado.text = "ACTIVO"
        holder.tvEstado.setTextColor(0xFF2E7D32.toInt())
        holder.tvEstado.setBackgroundColor(0xFFE8F5E9.toInt())
    }

    override fun getItemCount(): Int = listaDoctores.size

    fun actualizarLista(nuevaLista: List<MedicoAdmin>) {
        listaDoctores = nuevaLista
        notifyDataSetChanged()
    }
}