package pe.edu.idat.clinicasanmiguel.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import pe.edu.idat.clinicasanmiguel.data.AppDatabaseHelper
import pe.edu.idat.clinicasanmiguel.entity.Usuario

class UsuarioRepository(context: Context) {

    private val dbHelper = AppDatabaseHelper(context)
    fun registrarUsuario(usuario: Usuario): Long {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("dni", usuario.dni)
            put("nombre", usuario.nombre)
            put("apellido", usuario.apellido)
            put("correo", usuario.correo)
            put("password", usuario.password)
            put("telefono", usuario.telefono)
            put("fecha_nacimiento", usuario.fechaNacimiento)
            put("genero", usuario.genero)
            put("rol", usuario.rol)
        }
        val idGenerado = db.insert("csma_usuarios", null, valores)
        return idGenerado
    }
    fun login(correo: String, password: String): Usuario? {
        var usuarioLogueado: Usuario? = null
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM csma_usuarios WHERE correo = ? AND password = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(correo, password))

        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val dni = cursor.getString(cursor.getColumnIndexOrThrow("dni"))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido"))
            val uCorreo = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
            val uPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            val telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono"))
            val fechaNac = cursor.getString(cursor.getColumnIndexOrThrow("fecha_nacimiento"))
            val genero = cursor.getString(cursor.getColumnIndexOrThrow("genero"))
            val rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))

            usuarioLogueado = Usuario(id, dni, nombre, apellido, uCorreo, uPassword, telefono, fechaNac, genero, rol)
        }
        return usuarioLogueado
    }
    fun actualizarPassword(idUsuario: Int, nuevaPassword: String): Int {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("password", nuevaPassword)
        }
        val filasAfectadas = db.update("csma_usuarios", valores, "id = ?", arrayOf(idUsuario.toString()))
        return filasAfectadas
    }
}