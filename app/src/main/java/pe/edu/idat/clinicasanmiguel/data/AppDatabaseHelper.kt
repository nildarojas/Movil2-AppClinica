package pe.edu.idat.clinicasanmiguel.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "clinica_san_miguel.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE csma_usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                dni TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL,
                apellido TEXT NOT NULL,
                correo TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                telefono TEXT NOT NULL,
                fecha_nacimiento TEXT NOT NULL,
                genero TEXT NOT NULL,
                rol TEXT NOT NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE csma_especialidades (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre TEXT NOT NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE csma_medicos (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nombre TEXT NOT NULL,
                id_especialidad INTEGER NOT NULL,
                FOREIGN KEY(id_especialidad) REFERENCES csma_especialidades(id)
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE csma_citas (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                id_paciente INTEGER NOT NULL,
                id_medico INTEGER NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL, -- 'Activa', 'Finalizada', 'Cancelada'
                FOREIGN KEY(id_paciente) REFERENCES csma_usuarios(id),
                FOREIGN KEY(id_medico) REFERENCES csma_medicos(id)
            );
        """.trimIndent())
        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS csma_citas")
        db.execSQL("DROP TABLE IF EXISTS csma_medicos")
        db.execSQL("DROP TABLE IF EXISTS csma_especialidades")
        db.execSQL("DROP TABLE IF EXISTS csma_usuarios")
        onCreate(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO csma_especialidades (nombre) VALUES ('Cardiología');")
        db.execSQL("INSERT INTO csma_especialidades (nombre) VALUES ('Pediatría');")
        db.execSQL("INSERT INTO csma_especialidades (nombre) VALUES ('Medicina General');")

        db.execSQL("INSERT INTO csma_medicos (nombre, id_especialidad) VALUES ('Dr. Elías Canchanya', 1);")
        db.execSQL("INSERT INTO csma_medicos (nombre, id_especialidad) VALUES ('Dra. Nilda Rojas', 1);")
        db.execSQL("INSERT INTO csma_medicos (nombre, id_especialidad) VALUES ('Dra. Abigail Valdez', 2);")
        db.execSQL("INSERT INTO csma_medicos (nombre, id_especialidad) VALUES ('Dr. Bryant Yacila', 3);")

        db.execSQL("""
            INSERT INTO csma_usuarios (dni, nombre, apellido, correo, password, telefono, fecha_nacimiento, genero, rol) 
            VALUES ('77777777', 'Admin', 'San Miguel', 'admin@clinica.com', 'admin123', '999999999', '1990-01-01', 'Masculino', 'ADMIN');
        """.trimIndent())
    }
}