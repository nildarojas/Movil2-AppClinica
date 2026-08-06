package pe.edu.idat.clinicasanmiguel.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.entity.Especialidad

class EspecialidadAdminAdapter(
    private var listaEspecialidades:
    List<Especialidad> = emptyList(),

    private val onItemClick:
    ((Especialidad) -> Unit)? = null

) : RecyclerView.Adapter<
        EspecialidadAdminAdapter.ViewHolder
        >() {

    init {
        setHasStableIds(
            true
        )
    }

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(
        view
    ) {
        val tvNombre:
                TextView =
            view.findViewById(
                R.id.tvNombreEspecialidad
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
                    R.layout.item_especialidad,
                    parent,
                    false
                )

        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val especialidad =
            listaEspecialidades[position]

        holder.tvNombre.text =
            especialidad.nombre

        holder.itemView
            .setOnClickListener {
                onItemClick?.invoke(
                    especialidad
                )
            }
    }

    override fun getItemCount(): Int {
        return listaEspecialidades.size
    }

    override fun getItemId(
        position: Int
    ): Long {
        return listaEspecialidades[position]
            .id
            .toLong()
    }

    fun actualizarLista(
        nuevaLista: List<Especialidad>
    ) {
        listaEspecialidades =
            nuevaLista

        notifyDataSetChanged()
    }
}