package pe.edu.idat.clinicasanmiguel.repository

import android.content.ContentValues
import android.content.Context
import pe.edu.idat.clinicasanmiguel.data.AppDatabaseHelper
import pe.edu.idat.clinicasanmiguel.entity.Especialidad
import pe.edu.idat.clinicasanmiguel.entity.HorarioAdmin
import pe.edu.idat.clinicasanmiguel.entity.MedicoAdmin

class AdminRepository(context: Context) {

    private val dbHelper =
        AppDatabaseHelper(context.applicationContext)

    fun obtenerEspecialidades(): List<Especialidad> {

        val lista = mutableListOf<Especialidad>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                id,
                nombre
            FROM csma_especialidades
            ORDER BY nombre COLLATE NOCASE
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {

                val id =
                    it.getInt(
                        it.getColumnIndexOrThrow("id")
                    )

                val nombre =
                    it.getString(
                        it.getColumnIndexOrThrow("nombre")
                    )

                lista.add(
                    Especialidad(
                        id = id,
                        nombre = nombre
                    )
                )
            }
        }

        return lista
    }

    fun registrarEspecialidad(
        nombre: String
    ): Long {

        val nombreLimpio = nombre.trim()

        if (nombreLimpio.isEmpty()) {
            return RESULTADO_ERROR
        }

        if (existeEspecialidad(nombreLimpio)) {
            return RESULTADO_DUPLICADO
        }

        val db = dbHelper.writableDatabase

        val valores = ContentValues().apply {
            put("nombre", nombreLimpio)
        }

        return db.insert(
            "csma_especialidades",
            null,
            valores
        )
    }

    private fun existeEspecialidad(
        nombre: String
    ): Boolean {

        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT id
            FROM csma_especialidades
            WHERE nombre = ? COLLATE NOCASE
            LIMIT 1
            """.trimIndent(),
            arrayOf(nombre)
        )

        return cursor.use {
            it.moveToFirst()
        }
    }

    fun obtenerMedicos(): List<MedicoAdmin> {

        val lista = mutableListOf<MedicoAdmin>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT
                m.id,
                m.nombre,
                m.id_especialidad,
                e.nombre AS especialidad
            FROM csma_medicos m

            INNER JOIN csma_especialidades e
                ON e.id = m.id_especialidad

            ORDER BY m.id DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {

                val medico = MedicoAdmin(
                    id = it.getInt(
                        it.getColumnIndexOrThrow("id")
                    ),

                    nombre = it.getString(
                        it.getColumnIndexOrThrow("nombre")
                    ),

                    idEspecialidad = it.getInt(
                        it.getColumnIndexOrThrow(
                            "id_especialidad"
                        )
                    ),

                    especialidad = it.getString(
                        it.getColumnIndexOrThrow(
                            "especialidad"
                        )
                    )
                )

                lista.add(medico)
            }
        }

        return lista
    }

    fun registrarMedico(
        nombreCompleto: String,
        idEspecialidad: Int
    ): Long {

        val nombreLimpio =
            nombreCompleto
                .trim()
                .replace(Regex("\\s+"), " ")

        if (
            nombreLimpio.isEmpty() ||
            idEspecialidad <= 0
        ) {
            return RESULTADO_ERROR
        }

        val db = dbHelper.writableDatabase

        val valores = ContentValues().apply {
            put("nombre", nombreLimpio)
            put("id_especialidad", idEspecialidad)
        }

        return db.insert(
            "csma_medicos",
            null,
            valores
        )
    }
    fun registrarHorario(
        idMedico: Int,
        fechaHoraTexto: String
    ): Long {

        val horarioLimpio =
            fechaHoraTexto.trim()

        if (
            idMedico <= 0 ||
            horarioLimpio.isEmpty()
        ) {
            return RESULTADO_ERROR
        }

        val db = dbHelper.writableDatabase

        val valores = ContentValues().apply {
            put("id_medico", idMedico)
            put("fecha_hora_texto", horarioLimpio)
            put("estado", "DISPONIBLE")
        }
        return db.insert(
            "csma_horarios_disponibles",
            null,
            valores
        )
    }

    fun obtenerHorarios(): List<HorarioAdmin> {

        val lista = mutableListOf<HorarioAdmin>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT
                h.id,
                h.id_medico,
                h.fecha_hora_texto,
                h.estado,
                m.nombre AS medico,
                e.nombre AS especialidad

            FROM csma_horarios_disponibles h

            INNER JOIN csma_medicos m
                ON m.id = h.id_medico

            INNER JOIN csma_especialidades e
                ON e.id = m.id_especialidad

            ORDER BY h.id DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {

                val horario = HorarioAdmin(
                    id = it.getInt(
                        it.getColumnIndexOrThrow("id")
                    ),

                    idMedico = it.getInt(
                        it.getColumnIndexOrThrow(
                            "id_medico"
                        )
                    ),

                    medico = it.getString(
                        it.getColumnIndexOrThrow(
                            "medico"
                        )
                    ),

                    especialidad = it.getString(
                        it.getColumnIndexOrThrow(
                            "especialidad"
                        )
                    ),

                    fechaHoraTexto = it.getString(
                        it.getColumnIndexOrThrow(
                            "fecha_hora_texto"
                        )
                    ),

                    estado = it.getString(
                        it.getColumnIndexOrThrow(
                            "estado"
                        )
                    )
                )

                lista.add(horario)
            }
        }

        return lista
    }

    fun obtenerHorariosPorMedico(
        idMedico: Int
    ): List<HorarioAdmin> {

        val lista = mutableListOf<HorarioAdmin>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT
                h.id,
                h.id_medico,
                h.fecha_hora_texto,
                h.estado,
                m.nombre AS medico,
                e.nombre AS especialidad

            FROM csma_horarios_disponibles h

            INNER JOIN csma_medicos m
                ON m.id = h.id_medico

            INNER JOIN csma_especialidades e
                ON e.id = m.id_especialidad

            WHERE h.id_medico = ?

            ORDER BY h.id ASC
        """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(idMedico.toString())
        )

        cursor.use {
            while (it.moveToNext()) {

                lista.add(
                    HorarioAdmin(
                        id = it.getInt(
                            it.getColumnIndexOrThrow("id")
                        ),

                        idMedico = it.getInt(
                            it.getColumnIndexOrThrow(
                                "id_medico"
                            )
                        ),

                        medico = it.getString(
                            it.getColumnIndexOrThrow(
                                "medico"
                            )
                        ),

                        especialidad = it.getString(
                            it.getColumnIndexOrThrow(
                                "especialidad"
                            )
                        ),

                        fechaHoraTexto = it.getString(
                            it.getColumnIndexOrThrow(
                                "fecha_hora_texto"
                            )
                        ),

                        estado = it.getString(
                            it.getColumnIndexOrThrow(
                                "estado"
                            )
                        )
                    )
                )
            }
        }

        return lista
    }

    companion object {

        const val RESULTADO_ERROR = -1L
        const val RESULTADO_DUPLICADO = -2L
    }
}