package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R

data class EspecialidadMock(val nombre: String, val area: String, val estado: String)

class EspecialidadAdminAdapter(
    private val lista: List<EspecialidadMock>,
    private val esModoAdmin: Boolean = true,
    private val onItemClick: ((EspecialidadMock) -> Unit)? = null
) : RecyclerView.Adapter<EspecialidadAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreEspecialidad)
        val tvArea: TextView = view.findViewById(R.id.tvAreaEspecialidad)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoEspecialidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_especialidad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val esp = lista[position]
        holder.tvNombre.text = esp.nombre

        if (esModoAdmin) {
            holder.tvArea.visibility = View.VISIBLE
            holder.tvEstado.visibility = View.VISIBLE
            holder.tvArea.text = "Área: ${esp.area}"
            holder.tvEstado.text = esp.estado

            if (esp.estado == "ACTIVO") {
                holder.tvEstado.setTextColor(0xFF458890.toInt())
                holder.tvEstado.setBackgroundColor(0xFFE0F2F1.toInt())
            } else {
                holder.tvEstado.setTextColor(0xFFD50000.toInt())
                holder.tvEstado.setBackgroundColor(0xFFFFEBEE.toInt())
            }
        } else {
            holder.tvArea.visibility = View.GONE
            holder.tvEstado.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(esp)
        }
    }

    override fun getItemCount(): Int = lista.size
}