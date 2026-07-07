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
}