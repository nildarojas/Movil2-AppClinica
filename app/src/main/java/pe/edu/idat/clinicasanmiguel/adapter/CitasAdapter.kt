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
import pe.edu.idat.clinicasanmiguel.repository.CitaRepository

data class CitaPacienteMock(val id: Int, val especialidad: String, val medico: String, val fechaHora: String, var estado: String)

class CitasAdapter(
    private val lista: List<CitaPacienteMock>,
    private val esHistorial: Boolean = false,
    private val onCitaCancelada: () -> Unit
) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

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
        holder.tvEstado.text = item.estado

        if (position == 0) {
            holder.card.strokeColor = 0xFF458890.toInt()
            holder.card.strokeWidth = 6
        } else {
            holder.card.strokeColor = 0xFFE2E8F0.toInt()
            holder.card.strokeWidth = 2
        }

        if (item.estado == "CANCELADA") {
            holder.tvFechaHora.text = item.fechaHora
            holder.card.alpha = 0.5f
            holder.btnReprogramar.visibility = View.GONE
            holder.btnCancelar.visibility = View.GONE
            holder.tvEstado.setBackgroundColor(0xFFFFEBEE.toInt())
            holder.tvEstado.setTextColor(0xFFC62828.toInt())
        } else if (item.estado == "REPROGRAMADA") {
            holder.tvFechaHora.text = "Fecha anterior: " + item.fechaHora
            holder.card.alpha = 0.6f
            holder.btnReprogramar.visibility = View.GONE
            holder.btnCancelar.visibility = View.GONE
            holder.tvEstado.setBackgroundColor(0xFFF5F5F5.toInt())
            holder.tvEstado.setTextColor(0xFF616161.toInt())
        } else {
            holder.tvFechaHora.text = item.fechaHora
            holder.card.alpha = 1.0f
            if (esHistorial) {
                holder.btnReprogramar.visibility = View.GONE
                holder.btnCancelar.visibility = View.GONE
            } else {
                holder.btnReprogramar.visibility = View.VISIBLE
                holder.btnCancelar.visibility = View.VISIBLE
            }
            holder.tvEstado.setBackgroundColor(0xFFE8F5E9.toInt())
            holder.tvEstado.setTextColor(0xFF2E7D32.toInt())
        }

        if (!esHistorial && item.estado != "CANCELADA" && item.estado != "REPROGRAMADA") {
            holder.btnReprogramar.setOnClickListener {
                val context = holder.itemView.context as Activity
                val intent = Intent(context, ReprogramarCitaActivity::class.java)
                intent.putExtra("id_cita", item.id)
                intent.putExtra("posicion", position)
                intent.putExtra("especialidad", item.especialidad)
                intent.putExtra("medico", item.medico)
                intent.putExtra("fecha_hora", item.fechaHora)
                context.startActivityForResult(intent, 100)
            }

            holder.btnCancelar.setOnClickListener {
                val context = holder.itemView.context
                AlertDialog.Builder(context)
                    .setTitle("Confirmar cancelación")
                    .setMessage("¿Seguro que deseas cancelar esta cita?")
                    .setPositiveButton("Sí") { _, _ ->
                        val citaRepository = CitaRepository(context)
                        val filasAfectadas = citaRepository.cancelarCita(item.id)
                        if (filasAfectadas > 0) {
                            Toast.makeText(context, "Cita cancelada físicamente", Toast.LENGTH_SHORT).show()
                            onCitaCancelada()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        } else {
            holder.btnReprogramar.setOnClickListener(null)
            holder.btnCancelar.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = lista.size
}