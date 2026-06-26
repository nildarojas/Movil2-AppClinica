package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R

data class NotificacionMock(val id: Int, val mensaje: String, val fecha: String)

class NotificacionesAdapter(private val lista: List<NotificacionMock>) :
    RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMensaje: TextView = view.findViewById(R.id.tvMensajeNoti)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaNoti)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvMensaje.text = item.mensaje
        holder.tvFecha.text = item.fecha
    }

    override fun getItemCount(): Int = lista.size
}