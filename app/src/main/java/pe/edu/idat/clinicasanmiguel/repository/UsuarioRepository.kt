package pe.edu.idat.clinicasanmiguel.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import pe.edu.idat.clinicasanmiguel.data.AppDatabaseHelper
import pe.edu.idat.clinicasanmiguel.entity.Usuario
import pe.edu.idat.clinicasanmiguel.network.LoginApiRequest
import pe.edu.idat.clinicasanmiguel.network.LoginApiResponse
import pe.edu.idat.clinicasanmiguel.network.RetrofitClient
import pe.edu.idat.clinicasanmiguel.network.UsuarioLoginApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import pe.edu.idat.clinicasanmiguel.network.RegistroApiRequest
import pe.edu.idat.clinicasanmiguel.network.RegistroApiResponse
import org.json.JSONObject
import pe.edu.idat.clinicasanmiguel.network.ActualizarPerfilApiRequest

class UsuarioRepository(context: Context) {


    private val dbHelper = AppDatabaseHelper(context)
    private val apiService =
        RetrofitClient.obtenerApiService(
            context.applicationContext
        )
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

    fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        var usuario: Usuario? = null
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM csma_usuarios WHERE LOWER(correo) = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(correo.trim().lowercase()))

        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                dni = cursor.getString(cursor.getColumnIndexOrThrow("dni")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido")),
                correo = cursor.getString(cursor.getColumnIndexOrThrow("correo")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow("fecha_nacimiento")),
                genero = cursor.getString(cursor.getColumnIndexOrThrow("genero")),
                rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
            )
        }
        cursor.close()
        return usuario
    }

    fun actualizarPerfilApi(
        usuarioEditado: Usuario,
        callback: (ResultadoActualizarPerfilApi) -> Unit
    ) {
        val request = ActualizarPerfilApiRequest(
            dni = usuarioEditado.dni.trim(),
            nombre = usuarioEditado.nombre.trim(),
            apellido = usuarioEditado.apellido.trim(),
            correo = usuarioEditado.correo
                .trim()
                .lowercase(),
            telefono = usuarioEditado.telefono.trim(),
            fechaNacimiento =
                usuarioEditado.fechaNacimiento.trim(),
            genero = usuarioEditado.genero.trim()
        )

        apiService
            .actualizarPerfil(request)
            .enqueue(
                object : Callback<UsuarioLoginApi> {

                    override fun onResponse(
                        call: Call<UsuarioLoginApi>,
                        response: Response<UsuarioLoginApi>
                    ) {
                        if (response.isSuccessful) {
                            val usuarioApi = response.body()

                            if (usuarioApi == null) {
                                callback(
                                    ResultadoActualizarPerfilApi.Error(
                                        "La API devolvió una respuesta vacía"
                                    )
                                )
                                return
                            }

                            /*
                             * La API ya actualizó SQL Server.
                             * Recién ahora sincronizamos la caché.
                             */
                            val usuarioCache =
                                guardarPerfilActualizadoEnCache(
                                    idUsuarioLocal =
                                        usuarioEditado.id,
                                    usuarioApi = usuarioApi
                                )

                            val usuarioResultado =
                                usuarioCache
                                    ?: Usuario(
                                        id = usuarioEditado.id,
                                        dni = usuarioApi.dni,
                                        nombre = usuarioApi.nombre,
                                        apellido = usuarioApi.apellido,
                                        correo = usuarioApi.correo,
                                        password = "",
                                        telefono =
                                            usuarioApi.telefono,
                                        fechaNacimiento =
                                            usuarioApi.fechaNacimiento,
                                        genero = usuarioApi.genero,
                                        rol = usuarioApi.rol
                                    )

                            callback(
                                ResultadoActualizarPerfilApi.Exito(
                                    usuario = usuarioResultado,
                                    cacheActualizada =
                                        usuarioCache != null,
                                    mensaje =
                                        "Perfil actualizado correctamente"
                                )
                            )

                            return
                        }

                        val mensajeApi =
                            obtenerMensajeErrorPerfil(response)

                        when (response.code()) {
                            400 -> {
                                callback(
                                    ResultadoActualizarPerfilApi.Error(
                                        mensajeApi
                                            ?: "Revisa los datos ingresados"
                                    )
                                )
                            }

                            401 -> {
                                callback(
                                    ResultadoActualizarPerfilApi
                                        .SesionExpirada(
                                            mensajeApi
                                                ?: "Tu sesión expiró. Inicia sesión nuevamente."
                                        )
                                )
                            }

                            404 -> {
                                callback(
                                    ResultadoActualizarPerfilApi.Error(
                                        mensajeApi
                                            ?: "El usuario no fue encontrado"
                                    )
                                )
                            }

                            409 -> {
                                callback(
                                    ResultadoActualizarPerfilApi
                                        .DatosDuplicados(
                                            mensajeApi
                                                ?: "El DNI o correo ya está registrado"
                                        )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoActualizarPerfilApi.Error(
                                        mensajeApi
                                            ?: "El servidor respondió con el código ${response.code()}"
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<UsuarioLoginApi>,
                        throwable: Throwable
                    ) {
                        callback(
                            ResultadoActualizarPerfilApi.SinConexion(
                                "Editar el perfil requiere conexión a Internet. " +
                                        "Verifica tu conexión e inténtalo nuevamente."
                            )
                        )
                    }
                }
            )
    }

    private fun obtenerMensajeErrorPerfil(
        response: Response<*>
    ): String? {
        return try {
            val contenido =
                response.errorBody()
                    ?.string()

            if (contenido.isNullOrBlank()) {
                return null
            }

            val json =
                JSONObject(contenido)

            json.optString("mensaje")
                .takeIf {
                    it.isNotBlank()
                }
        } catch (exception: Exception) {
            null
        }
    }

    private fun guardarPerfilActualizadoEnCache(
        idUsuarioLocal: Int,
        usuarioApi: UsuarioLoginApi
    ): Usuario? {
        val db = dbHelper.writableDatabase

        var idCache = idUsuarioLocal

        db.beginTransaction()

        try {
            val valores = ContentValues().apply {
                put("dni", usuarioApi.dni)
                put("nombre", usuarioApi.nombre)
                put("apellido", usuarioApi.apellido)
                put(
                    "correo",
                    usuarioApi.correo
                        .trim()
                        .lowercase()
                )
                put("password", "")
                put("telefono", usuarioApi.telefono)
                put(
                    "fecha_nacimiento",
                    usuarioApi.fechaNacimiento
                )
                put("genero", usuarioApi.genero)
                put("rol", usuarioApi.rol)
            }

            var filasActualizadas = 0
            if (idCache > 0) {
                filasActualizadas =
                    db.update(
                        "csma_usuarios",
                        valores,
                        "id = ?",
                        arrayOf(idCache.toString())
                    )
            }
            if (filasActualizadas <= 0) {
                val cursor = db.rawQuery(
                    """
                SELECT id
                FROM csma_usuarios
                WHERE LOWER(correo) = ?
                   OR dni = ?
                LIMIT 1
                """.trimIndent(),
                    arrayOf(
                        usuarioApi.correo
                            .trim()
                            .lowercase(),
                        usuarioApi.dni.trim()
                    )
                )

                cursor.use {
                    if (it.moveToFirst()) {
                        idCache =
                            it.getInt(
                                it.getColumnIndexOrThrow("id")
                            )
                    }
                }

                if (idCache > 0) {
                    filasActualizadas =
                        db.update(
                            "csma_usuarios",
                            valores,
                            "id = ?",
                            arrayOf(idCache.toString())
                        )

                    if (filasActualizadas <= 0) {
                        return null
                    }
                } else {
                    val nuevoId =
                        db.insert(
                            "csma_usuarios",
                            null,
                            valores
                        )

                    if (nuevoId == -1L) {
                        return null
                    }

                    idCache = nuevoId.toInt()
                }
            }

            db.setTransactionSuccessful()

            return Usuario(
                id = idCache,
                dni = usuarioApi.dni,
                nombre = usuarioApi.nombre,
                apellido = usuarioApi.apellido,
                correo = usuarioApi.correo,
                password = "",
                telefono = usuarioApi.telefono,
                fechaNacimiento =
                    usuarioApi.fechaNacimiento,
                genero = usuarioApi.genero,
                rol = usuarioApi.rol
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            return null
        } finally {
            db.endTransaction()
        }
    }

    fun actualizarPasswordPorCorreo(correo: String, nuevaPassword: String): Boolean {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("password", nuevaPassword)
        }
        val filasAfectadas = db.update(
            "csma_usuarios",
            valores,
            "LOWER(correo) = ?",
            arrayOf(correo.trim().lowercase())
        )
        return filasAfectadas > 0
    }

    fun registrarUsuarioApi(
        usuario: Usuario,
        callback: (ResultadoRegistroApi) -> Unit
    ) {
        val request = RegistroApiRequest(
            dni = usuario.dni.trim(),
            nombre = usuario.nombre.trim(),
            apellido = usuario.apellido.trim(),
            correo = usuario.correo.trim().lowercase(),
            password = usuario.password,
            telefono = usuario.telefono.trim(),
            fechaNacimiento = usuario.fechaNacimiento.trim(),
            genero = usuario.genero.trim()
        )

        apiService
            .registrar(request)
            .enqueue(
                object : Callback<RegistroApiResponse> {

                    override fun onResponse(
                        call: Call<RegistroApiResponse>,
                        response: Response<RegistroApiResponse>
                    ) {
                        if (response.isSuccessful) {
                            val respuesta = response.body()
                            val idUsuarioApi = respuesta?.idUsuario

                            if (
                                respuesta?.exito == true &&
                                idUsuarioApi != null &&
                                idUsuarioApi > 0
                            ) {
                                val idUsuarioLocal =
                                    guardarOActualizarUsuarioRegistradoEnLocal(
                                        usuario
                                    )

                                if (idUsuarioLocal > 0) {
                                    callback(
                                        ResultadoRegistroApi.Exito(
                                            datos = DatosRegistroApi(
                                                idUsuarioApi = idUsuarioApi,
                                                idUsuarioLocal = idUsuarioLocal
                                            ),
                                            mensaje = respuesta.mensaje
                                        )
                                    )
                                } else {
                                    callback(
                                        ResultadoRegistroApi.Error(
                                            "El usuario se registró en Azure, pero no se pudo guardar la copia local"
                                        )
                                    )
                                }
                            } else {
                                callback(
                                    ResultadoRegistroApi.Error(
                                        respuesta?.mensaje
                                            ?: "La API devolvió una respuesta incompleta"
                                    )
                                )
                            }

                            return
                        }

                        when (response.code()) {

                            409 -> {
                                callback(
                                    ResultadoRegistroApi.Duplicado(
                                        "El DNI o correo ya se encuentra registrado"
                                    )
                                )
                            }

                            400 -> {
                                callback(
                                    ResultadoRegistroApi.Error(
                                        "Revise los datos ingresados en el formulario"
                                    )
                                )
                            }

                            else -> {
                                callback(
                                    ResultadoRegistroApi.Error(
                                        "El servidor respondió con el código ${response.code()}"
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<RegistroApiResponse>,
                        throwable: Throwable
                    ) {
                        callback(
                            ResultadoRegistroApi.Error(
                                "Esta acción requiere conexión a Internet. Verifica tu conexión e inténtalo nuevamente."
                            )
                        )
                    }
                }
            )
    }

    private fun guardarOActualizarUsuarioRegistradoEnLocal(
        usuario: Usuario
    ): Long {
        val db = dbHelper.writableDatabase
        var idUsuarioLocal = -1L

        db.beginTransaction()

        try {
            val cursor = db.rawQuery(
                """
            SELECT id
            FROM csma_usuarios
            WHERE correo = ?
               OR dni = ?
            LIMIT 1
            """.trimIndent(),
                arrayOf(
                    usuario.correo.trim().lowercase(),
                    usuario.dni.trim()
                )
            )

            cursor.use {
                if (it.moveToFirst()) {
                    idUsuarioLocal = it.getLong(
                        it.getColumnIndexOrThrow("id")
                    )
                }
            }

            val valores = ContentValues().apply {
                put("dni", usuario.dni.trim())
                put("nombre", usuario.nombre.trim())
                put("apellido", usuario.apellido.trim())
                put(
                    "correo",
                    usuario.correo.trim().lowercase()
                )
                put("password", "")
                put("telefono", usuario.telefono.trim())
                put(
                    "fecha_nacimiento",
                    usuario.fechaNacimiento.trim()
                )
                put("genero", usuario.genero.trim())
                put("rol", "PACIENTE")
            }

            if (idUsuarioLocal > 0) {
                val filasActualizadas = db.update(
                    "csma_usuarios",
                    valores,
                    "id = ?",
                    arrayOf(idUsuarioLocal.toString())
                )

                if (filasActualizadas <= 0) {
                    return -1L
                }
            } else {
                idUsuarioLocal = db.insert(
                    "csma_usuarios",
                    null,
                    valores
                )

                if (idUsuarioLocal == -1L) {
                    return -1L
                }
            }

            db.setTransactionSuccessful()
            return idUsuarioLocal

        } catch (exception: Exception) {
            exception.printStackTrace()
            return -1L

        } finally {
            db.endTransaction()
        }
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

    fun loginApi(
        correo: String,
        password: String,
        callback: (ResultadoLoginApi) -> Unit
    ) {
        val request = LoginApiRequest(
            correo = correo.trim(),
            password = password
        )

        apiService
            .login(request)
            .enqueue(
                object : Callback<LoginApiResponse> {

                    override fun onResponse(
                        call: Call<LoginApiResponse>,
                        response: Response<LoginApiResponse>
                    ) {
                        if (response.isSuccessful) {

                            val respuesta = response.body()
                            val usuarioApi = respuesta?.usuario
                            val token = respuesta?.token

                            if (
                                respuesta?.exito == true &&
                                usuarioApi != null &&
                                !token.isNullOrBlank()
                            ) {
                                val usuarioLocal =
                                    guardarOActualizarUsuarioApiEnLocal(
                                        usuarioApi = usuarioApi,
                                        passwordIngresado = password
                                    )

                                if (usuarioLocal != null) {
                                    callback(
                                        ResultadoLoginApi.Exito(
                                            usuarioLocal = usuarioLocal,
                                            idUsuarioApi = usuarioApi.id,
                                            token = token,
                                            mensaje = respuesta.mensaje
                                        )
                                    )
                                } else {
                                    callback(
                                        ResultadoLoginApi.Error(
                                            "La sesión fue validada, pero no se pudo guardar la copia local"
                                        )
                                    )
                                }
                            } else {
                                callback(
                                    ResultadoLoginApi.Error(
                                        respuesta?.mensaje
                                            ?: "La API devolvió una respuesta incompleta"
                                    )
                                )
                            }

                            return
                        }

                        if (response.code() == 401) {
                            callback(
                                ResultadoLoginApi.CredencialesInvalidas(
                                    "Correo o contraseña incorrectos"
                                )
                            )
                        } else {
                            callback(
                                ResultadoLoginApi.Error(
                                    "El servidor respondió con el código ${response.code()}"
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<LoginApiResponse>,
                        throwable: Throwable
                    ) {
                        callback(
                            ResultadoLoginApi.SinConexion(
                                throwable.message
                                    ?: "No se pudo establecer conexión con la API"
                            )
                        )
                    }
                }
            )
    }

    private fun guardarOActualizarUsuarioApiEnLocal(
        usuarioApi: UsuarioLoginApi,
        passwordIngresado: String
    ): Usuario? {

        val db = dbHelper.writableDatabase
        var idUsuarioLocal = -1

        db.beginTransaction()

        try {
            val cursor = db.rawQuery(
                """
            SELECT id
            FROM csma_usuarios
            WHERE correo = ?
               OR dni = ?
            LIMIT 1
            """.trimIndent(),
                arrayOf(
                    usuarioApi.correo,
                    usuarioApi.dni
                )
            )

            cursor.use {
                if (it.moveToFirst()) {
                    idUsuarioLocal = it.getInt(
                        it.getColumnIndexOrThrow("id")
                    )
                }
            }

            val valores = ContentValues().apply {
                put("dni", usuarioApi.dni)
                put("nombre", usuarioApi.nombre)
                put("apellido", usuarioApi.apellido)
                put("correo", usuarioApi.correo)
                put("password", "")
                put("telefono", usuarioApi.telefono)
                put(
                    "fecha_nacimiento",
                    usuarioApi.fechaNacimiento
                )
                put("genero", usuarioApi.genero)
                put("rol", usuarioApi.rol)
            }

            if (idUsuarioLocal > 0) {
                val filasActualizadas = db.update(
                    "csma_usuarios",
                    valores,
                    "id = ?",
                    arrayOf(idUsuarioLocal.toString())
                )

                if (filasActualizadas <= 0) {
                    return null
                }
            } else {
                val nuevoId = db.insert(
                    "csma_usuarios",
                    null,
                    valores
                )

                if (nuevoId == -1L) {
                    return null
                }

                idUsuarioLocal = nuevoId.toInt()
            }

            db.setTransactionSuccessful()

            return Usuario(
                id = idUsuarioLocal,
                dni = usuarioApi.dni,
                nombre = usuarioApi.nombre,
                apellido = usuarioApi.apellido,
                correo = usuarioApi.correo,
                password = "",
                telefono = usuarioApi.telefono,
                fechaNacimiento = usuarioApi.fechaNacimiento,
                genero = usuarioApi.genero,
                rol = usuarioApi.rol
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
            return null
        } finally {
            db.endTransaction()
        }
    }
    fun actualizarPassword(idUsuario: Int, nuevaPassword: String): Int {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("password", nuevaPassword)
        }
        val filasAfectadas = db.update("csma_usuarios", valores, "id = ?", arrayOf(idUsuario.toString()))
        return filasAfectadas
    }
    fun obtenerUsuarioPorId(idUsuario: Int): Usuario? {
        var usuario: Usuario? = null
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM csma_usuarios WHERE id = ?", arrayOf(idUsuario.toString()))

        if (cursor.moveToFirst()) {
            usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                dni = cursor.getString(cursor.getColumnIndexOrThrow("dni")),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                apellido = cursor.getString(cursor.getColumnIndexOrThrow("apellido")),
                correo = cursor.getString(cursor.getColumnIndexOrThrow("correo")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow("fecha_nacimiento")),
                genero = cursor.getString(cursor.getColumnIndexOrThrow("genero")),
                rol = cursor.getString(cursor.getColumnIndexOrThrow("rol"))
            )
        }
        return usuario
    }

    fun obtenerTodosLosUsuarios(): List<Usuario> {

        val listaUsuarios =
            mutableListOf<Usuario>()

        val db =
            dbHelper.readableDatabase

        val cursor: Cursor =
            db.rawQuery(
                """
            SELECT *
            FROM csma_usuarios
            ORDER BY id DESC
            """.trimIndent(),
                null
            )

        if (cursor.moveToFirst()) {

            do {

                val usuario = Usuario(
                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                    ),

                    dni = cursor.getString(
                        cursor.getColumnIndexOrThrow("dni")
                    ),

                    nombre = cursor.getString(
                        cursor.getColumnIndexOrThrow("nombre")
                    ),

                    apellido = cursor.getString(
                        cursor.getColumnIndexOrThrow("apellido")
                    ),

                    correo = cursor.getString(
                        cursor.getColumnIndexOrThrow("correo")
                    ),

                    password = cursor.getString(
                        cursor.getColumnIndexOrThrow("password")
                    ),

                    telefono = cursor.getString(
                        cursor.getColumnIndexOrThrow("telefono")
                    ),

                    fechaNacimiento = cursor.getString(
                        cursor.getColumnIndexOrThrow("fecha_nacimiento")
                    ),

                    genero = cursor.getString(
                        cursor.getColumnIndexOrThrow("genero")
                    ),

                    rol = cursor.getString(
                        cursor.getColumnIndexOrThrow("rol")
                    )
                )

                listaUsuarios.add(usuario)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return listaUsuarios
    }
}