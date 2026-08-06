package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.network.UsuarioListadoApiResponse

class UsuariosAdminAdapter(
    private var listaUsuarios: List<UsuarioListadoApiResponse>
) : RecyclerView.Adapter<UsuariosAdminAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val tvNombre: TextView =
            view.findViewById(
                R.id.tvNombreUsuarioAdmin
            )

        val tvCorreo: TextView =
            view.findViewById(
                R.id.tvCorreoUsuarioAdmin
            )

        val tvRol: TextView =
            view.findViewById(
                R.id.tvRolUsuarioAdmin
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsuarioViewHolder {
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_usuario_admin,
                    parent,
                    false
                )

        return UsuarioViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: UsuarioViewHolder,
        position: Int
    ) {
        val usuario =
            listaUsuarios[position]

        holder.tvNombre.text =
            "${usuario.nombre} ${usuario.apellido}"
                .trim()

        holder.tvCorreo.text =
            usuario.correo

        holder.tvRol.text =
            usuario.rol
    }

    override fun getItemCount(): Int =
        listaUsuarios.size

    fun actualizarLista(
        nuevaLista: List<UsuarioListadoApiResponse>
    ) {
        listaUsuarios =
            nuevaLista

        notifyDataSetChanged()
    }
}