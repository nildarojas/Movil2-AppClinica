package pe.edu.idat.clinicasanmiguel.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import pe.edu.idat.clinicasanmiguel.R
import pe.edu.idat.clinicasanmiguel.ReprogramarCitaActivity

data class CitaPacienteMock(val id: Int, val especialidad: String, val medico: String, val fechaHora: String, var estado: String)

class CitasAdapter(private val lista: List<CitaPacienteMock>) :
    RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

    class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardCitaPaciente)
        val tvEspecialidad: TextView = view.findViewById(R.id.tvEspecialidadItem)
        val tvMedico: TextView = view.findViewById(R.id.tvDoctorItem)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHoraItem)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoItem)
        val btnReprogramar: MaterialButton = view.findViewById(R.id.btnReprogramarItem)
        val btnCancelar: MaterialButton = view.findViewById(R.id.btnCancelarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita_paciente, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val item = lista[position]
        holder.tvEspecialidad.text = item.especialidad
        holder.tvMedico.text = item.medico
        holder.tvFechaHora.text = item.fechaHora
        holder.tvEstado.text = item.estado

        when (item.estado) {
            "CANCELADA" -> {
                holder.card.alpha = 0.5f
                holder.btnReprogramar.isEnabled = false
                holder.btnReprogramar.visibility = View.GONE
                holder.btnCancelar.isEnabled = false
                holder.btnCancelar.visibility = View.GONE
                holder.tvEstado.setBackgroundColor(0xFFFFEBEE.toInt())
                holder.tvEstado.setTextColor(0xFFC62828.toInt())
            }
            "REPROGRAMADA" -> {
                holder.card.alpha = 1.0f
                holder.btnReprogramar.isEnabled = true
                holder.btnReprogramar.visibility = View.VISIBLE
                holder.btnCancelar.isEnabled = true
                holder.btnCancelar.visibility = View.VISIBLE
                holder.tvEstado.setBackgroundColor(0xFFE0F2F1.toInt())
                holder.tvEstado.setTextColor(0xFF004D40.toInt())

                holder.btnReprogramar.setOnClickListener {
                    val context = holder.itemView.context as Activity
                    val intent = Intent(context, ReprogramarCitaActivity::class.java)
                    intent.putExtra("id_cita", item.id)
                    intent.putExtra("posicion", position)
                    context.startActivityForResult(intent, 100)
                }
            }
            else -> {
                holder.card.alpha = 1.0f
                holder.btnReprogramar.isEnabled = true
                holder.btnReprogramar.visibility = View.VISIBLE
                holder.btnCancelar.isEnabled = true
                holder.btnCancelar.visibility = View.VISIBLE
                holder.tvEstado.setBackgroundColor(0xFFE8F5E9.toInt())
                holder.tvEstado.setTextColor(0xFF2E7D32.toInt())

                holder.btnReprogramar.setOnClickListener {
                    val context = holder.itemView.context as Activity
                    val intent = Intent(context, ReprogramarCitaActivity::class.java)
                    intent.putExtra("id_cita", item.id)
                    intent.putExtra("posicion", position)
                    context.startActivityForResult(intent, 100)
                }
            }
        }

        holder.btnCancelar.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Confirmar cancelación")
                .setMessage("¿Seguro que deseas cancelar esta cita?")
                .setPositiveButton("Sí") { _, _ ->
                    item.estado = "CANCELADA"
                    notifyItemChanged(position)
                    Toast.makeText(context, "Cita cancelada con éxito", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount(): Int = lista.size
}