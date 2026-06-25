package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.DoctorMock

class DoctorAdminAdapter(private var listaDoctores: List<DoctorMock>) :
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
        val doctor = listaDoctores[position]

        holder.tvNombre.text = "${doctor.apellido}, ${doctor.nombre}"
        holder.tvEspecialidad.text = doctor.especialidad

        if (doctor.estado) {
            holder.tvEstado.text = "ACTIVO"
            holder.tvEstado.setTextColor(0xFF2E7D32.toInt())
            holder.tvEstado.setBackgroundColor(0xFFE8F5E9.toInt())
        } else {
            holder.tvEstado.text = "INACTIVO"
            holder.tvEstado.setTextColor(0xFFD50000.toInt())
            holder.tvEstado.setBackgroundColor(0xFFFFEBEE.toInt())
        }
    }

    override fun getItemCount(): Int = listaDoctores.size

    fun actualizarLista(nuevaLista: List<DoctorMock>) {
        listaDoctores = nuevaLista
        notifyDataSetChanged()
    }
}