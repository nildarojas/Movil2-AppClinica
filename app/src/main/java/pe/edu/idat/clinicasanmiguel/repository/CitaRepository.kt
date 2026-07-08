package pe.edu.idat.clinicasanmiguel.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import pe.edu.idat.clinicasanmiguel.data.AppDatabaseHelper
import pe.edu.idat.clinicasanmiguel.entity.Especialidad

class CitaRepository(context: Context) {

    private val dbHelper = AppDatabaseHelper(context)

    fun obtenerEspecialidades(): List<Especialidad> {
        val lista = mutableListOf<Especialidad>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM csma_especialidades", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                lista.add(Especialidad(id, nombre))
            } while (cursor.moveToNext())
        }
        return lista
    }
    fun obtenerMedicosPorEspecialidad(idEspecialidad: Int): Map<String, Int> {
        val mapaMedicos = mutableMapOf<String, Int>()
        val db = dbHelper.readableDatabase
        val query = "SELECT id, nombre FROM csma_medicos WHERE id_especialidad = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(idEspecialidad.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                mapaMedicos[nombre] = id
            } while (cursor.moveToNext())
        }
        return mapaMedicos
    }
    fun insertarCita(idPaciente: Int, idMedico: Int, fechaHora: String): Long {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("id_paciente", idPaciente)
            put("id_medico", idMedico)
            put("fecha_hora", fechaHora)
            put("estado", "Activa")
        }
        val idGenerado = db.insert("csma_citas", null, valores)
        return idGenerado
    }

    fun obtenerCitasActivasPorPaciente(idPaciente: Int): List<pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard> {
        val lista = mutableListOf<pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT c.id AS id_cita, e.nombre AS especialidad, m.nombre AS medico, c.fecha_hora, c.estado
            FROM csma_citas c
            INNER JOIN csma_medicos m ON c.id_medico = m.id
            INNER JOIN csma_especialidades e ON m.id_especialidad = e.id
            WHERE c.id_paciente = ? AND c.estado = 'Activa'
        """.trimIndent()

        val cursor: Cursor = db.rawQuery(query, arrayOf(idPaciente.toString()))

        if (cursor.moveToFirst()) {
            do {
                val idCita = cursor.getInt(cursor.getColumnIndexOrThrow("id_cita"))
                val especialidad = cursor.getString(cursor.getColumnIndexOrThrow("especialidad"))
                val medico = cursor.getString(cursor.getColumnIndexOrThrow("medico"))
                val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora"))
                val estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))

                lista.add(pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard(idCita, especialidad, medico, fechaHora, estado))
            } while (cursor.moveToNext())
        }
        return lista
    }

    fun obtenerHistorialCitasPorPaciente(idPaciente: Int): List<pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard> {
        val lista = mutableListOf<pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT c.id AS id_cita, e.nombre AS especialidad, m.nombre AS medico, c.fecha_hora, c.estado
            FROM csma_citas c
            INNER JOIN csma_medicos m ON c.id_medico = m.id
            INNER JOIN csma_especialidades e ON m.id_especialidad = e.id
            WHERE c.id_paciente = ? AND c.estado != 'Activa'
        """.trimIndent()

        val cursor: Cursor = db.rawQuery(query, arrayOf(idPaciente.toString()))

        if (cursor.moveToFirst()) {
            do {
                val idCita = cursor.getInt(cursor.getColumnIndexOrThrow("id_cita"))
                val especialidad = cursor.getString(cursor.getColumnIndexOrThrow("especialidad"))
                val medico = cursor.getString(cursor.getColumnIndexOrThrow("medico"))
                val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora"))
                val estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))

                lista.add(pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard(idCita, especialidad, medico, fechaHora, estado))
            } while (cursor.moveToNext())
        }
        return lista
    }

    fun cancelarCita(idCita: Int): Int {
        val db = dbHelper.writableDatabase
        val valores = android.content.ContentValues().apply {
            put("estado", "CANCELADA")
        }
        return db.update("csma_citas", valores, "id = ?", arrayOf(idCita.toString()))
    }

    fun reprogramarCitaTransaccional(idCitaVieja: Int, nuevoHorario: String): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction() // 🔒 Iniciamos bloque seguro de hilos
        try {
            // Paso A: Obtener la data de la cita vieja para clonar id_paciente e id_medico
            var idPaciente = -1
            var idMedico = -1
            val cursor = db.rawQuery("SELECT id_paciente, id_medico FROM csma_citas WHERE id = ?", arrayOf(idCitaVieja.toString()))
            if (cursor.moveToFirst()) {
                idPaciente = cursor.getInt(cursor.getColumnIndexOrThrow("id_paciente"))
                idMedico = cursor.getInt(cursor.getColumnIndexOrThrow("id_medico"))
            }
            cursor.close()

            if (idPaciente == -1 || idMedico == -1) return false

            // Paso B: Cambiar el estado de la cita antigua a 'REPROGRAMADA'
            val valoresUpdate = android.content.ContentValues().apply {
                put("estado", "REPROGRAMADA")
            }
            db.update("csma_citas", valoresUpdate, "id = ?", arrayOf(idCitaVieja.toString()))

            // Paso C: Insertar la nueva cita clonada con el nuevo bloque horario en estado 'Activa'
            val valoresInsert = android.content.ContentValues().apply {
                put("id_paciente", idPaciente)
                put("id_medico", idMedico)
                put("fecha_hora", nuevoHorario)
                put("estado", "Activa") // Vuelve a nacer como Activa/Pendiente
            }
            db.insert("csma_citas", null, valoresInsert)

            db.setTransactionSuccessful() // 🔓 Si todo salió bien, guardamos cambios físicos
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction() // Cierra el canal transaccional
        }
    }
}