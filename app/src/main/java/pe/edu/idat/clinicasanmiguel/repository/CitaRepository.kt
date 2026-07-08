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
        cursor.close()
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
        cursor.close()
        return mapaMedicos
    }

    fun verificarMedicoOcupado(idMedico: Int, fechaHora: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) FROM csma_citas WHERE id_medico = ? AND fecha_hora = ? AND estado IN ('PENDIENTE', 'CONFIRMADA')"
        val cursor = db.rawQuery(query, arrayOf(idMedico.toString(), fechaHora))
        var ocupado = false
        if (cursor.moveToFirst()) {
            ocupado = cursor.getInt(0) > 0
        }
        cursor.close()
        return ocupado
    }

    fun verificarPacienteOcupado(idPaciente: Int, fechaHora: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) FROM csma_citas WHERE id_paciente = ? AND fecha_hora = ? AND estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_ATENCION')"
        val cursor = db.rawQuery(query, arrayOf(idPaciente.toString(), fechaHora))
        var ocupado = false
        if (cursor.moveToFirst()) {
            ocupado = cursor.getInt(0) > 0
        }
        cursor.close()
        return ocupado
    }

    fun obtenerMedicoPorCita(idCita: Int): Int {
        val db = dbHelper.readableDatabase
        val query = "SELECT id_medico FROM csma_citas WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(idCita.toString()))
        var idMedico = -1
        if (cursor.moveToFirst()) {
            idMedico = cursor.getInt(0)
        }
        cursor.close()
        return idMedico
    }

    fun insertarCita(idPaciente: Int, idMedico: Int, fechaHora: String): Long {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("id_paciente", idPaciente)
            put("id_medico", idMedico)
            put("fecha_hora", fechaHora)
            put("estado", "PENDIENTE")
        }
        return db.insert("csma_citas", null, valores)
    }

    fun obtenerCitasActivasPorPaciente(idPaciente: Int): List<pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard> {
        val lista = mutableListOf<pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard>()
        val db = dbHelper.readableDatabase
        val query = """
            SELECT c.id AS id_cita, e.nombre AS especialidad, m.nombre AS medico, c.fecha_hora, c.estado
            FROM csma_citas c
            INNER JOIN csma_medicos m ON c.id_medico = m.id
            INNER JOIN csma_especialidades e ON m.id_especialidad = e.id
            WHERE c.id_paciente = ? AND c.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_ATENCION')
            ORDER BY c.id DESC
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
        cursor.close()
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
            WHERE c.id_paciente = ? AND c.estado IN ('CANCELADA', 'REPROGRAMADA', 'ATENDIDA')
            ORDER BY c.id DESC
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
        cursor.close()
        return lista
    }

    fun cancelarCita(idCita: Int): Int {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("estado", "CANCELADA")
        }
        return db.update("csma_citas", valores, "id = ?", arrayOf(idCita.toString()))
    }

    fun reprogramarCitaTransaccional(idCitaVieja: Int, nuevoHorario: String): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            var idPaciente = -1
            var idMedico = -1
            val cursor = db.rawQuery("SELECT id_paciente, id_medico FROM csma_citas WHERE id = ?", arrayOf(idCitaVieja.toString()))
            if (cursor.moveToFirst()) {
                idPaciente = cursor.getInt(cursor.getColumnIndexOrThrow("id_paciente"))
                idMedico = cursor.getInt(cursor.getColumnIndexOrThrow("id_medico"))
            }
            cursor.close()

            if (idPaciente == -1 || idMedico == -1) return false
            val valoresUpdate = ContentValues().apply {
                put("estado", "REPROGRAMADA")
            }
            db.update("csma_citas", valoresUpdate, "id = ?", arrayOf(idCitaVieja.toString()))
            val valoresInsert = ContentValues().apply {
                put("id_paciente", idPaciente)
                put("id_medico", idMedico)
                put("fecha_hora", nuevoHorario)
                put("estado", "PENDIENTE")
                put("id_cita_anterior", idCitaVieja)
            }
            db.insert("csma_citas", null, valoresInsert)

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }


    fun obtenerHorariosConEstado(idPaciente: Int, idMedico: Int, horarioOriginal: String): List<String> {
        val horariosBase = listOf(
            "Lunes 08 de Junio - 08:30 AM",
            "Miércoles 10 de Junio - 10:15 AM",
            "Viernes 12 de Junio - 04:00 PM"
        )
        val listaResultado = mutableListOf<String>()
        val db = dbHelper.readableDatabase

        for (horario in horariosBase) {
            if (horario == horarioOriginal) {
                continue
            }

            var pacienteOcupado = false
            val queryPac = "SELECT COUNT(*) FROM csma_citas WHERE id_paciente = ? AND fecha_hora = ? AND estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_ATENCION')"
            val cursorPac = db.rawQuery(queryPac, arrayOf(idPaciente.toString(), horario))
            if (cursorPac.moveToFirst()) {
                pacienteOcupado = cursorPac.getInt(0) > 0
            }
            cursorPac.close()

            if (pacienteOcupado) {
                listaResultado.add("$horario (Ocupado por ti)")
                continue
            }

            var medicoOcupado = false
            val queryMed = "SELECT COUNT(*) FROM csma_citas WHERE id_medico = ? AND fecha_hora = ? AND estado IN ('PENDIENTE', 'CONFIRMADA')"
            val cursorMed = db.rawQuery(queryMed, arrayOf(idMedico.toString(), horario))
            if (cursorMed.moveToFirst()) {
                medicoOcupado = cursorMed.getInt(0) > 0
            }
            cursorMed.close()

            if (medicoOcupado) {
                listaResultado.add("$horario (Médico ocupado en este horario)")
                continue
            }

            listaResultado.add(horario)
        }
        return listaResultado
    }


    fun obtenerUltimaCitaPorPaciente(idPaciente: Int): pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT c.id AS id_cita, e.nombre AS especialidad, m.nombre AS medico, c.fecha_hora, c.estado
            FROM csma_citas c
            INNER JOIN csma_medicos m ON c.id_medico = m.id
            INNER JOIN csma_especialidades e ON m.id_especialidad = e.id
            WHERE c.id_paciente = ?
            ORDER BY c.id DESC LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idPaciente.toString()))
        var cita: pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard? = null

        if (cursor.moveToFirst()) {
            val idCita = cursor.getInt(cursor.getColumnIndexOrThrow("id_cita"))
            val especialidad = cursor.getString(cursor.getColumnIndexOrThrow("especialidad"))
            val medico = cursor.getString(cursor.getColumnIndexOrThrow("medico"))
            val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora"))
            val estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))

            cita = pe.edu.idat.clinicasanmiguel.entity.CitaPacienteCard(idCita, especialidad, medico, fechaHora, estado)
        }
        cursor.close()
        return cita
    }
}