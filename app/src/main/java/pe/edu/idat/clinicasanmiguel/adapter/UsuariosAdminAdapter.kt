package pe.edu.idat.clinicasanmiguel.adapter
import pe.edu.idat.clinicasanmiguel.adapter.UsuarioMock
import pe.edu.idat.clinicasanmiguel.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuariosAdminAdapter(private val lista: List<UsuarioMock>) :
    RecyclerView.Adapter<UsuariosAdminAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuarioAdmin)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoUsuarioAdmin)
        val tvRol: TextView = view.findViewById(R.id.tvRolUsuarioAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario_admin, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = "${item.nombre} ${item.apellido}"
        holder.tvCorreo.text = item.correo
        holder.tvRol.text = item.rol
    }

    override fun getItemCount(): Int = lista.size
}