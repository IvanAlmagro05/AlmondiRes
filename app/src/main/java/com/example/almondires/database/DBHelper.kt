package com.example.almondires.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.almondires.models.Alojamientos
import com.example.almondires.models.Reservas
import com.example.almondires.models.Usuarios
import java.text.SimpleDateFormat
import java.util.*

class DBHelper(context: Context): SQLiteOpenHelper(context,"reservas.db",null,15) { // Subido a versión 15
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario TEXT,
                email TEXT,
                password TEXT,
                telefono TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE alojamientos(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                precio REAL,
                usuario_id INTEGER,
                FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
            )            
        """)

        db.execSQL("""
            CREATE TABLE reservas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                fechaInicio TEXT,
                fechaFinal TEXT,
                precio REAL,
                telf TEXT,
                noches INTEGER,
                usuario_id INTEGER,
                FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE reserva_alojamientos (
                reserva_id INTEGER,
                alojamiento_id INTEGER,
                PRIMARY KEY(reserva_id, alojamiento_id),
                FOREIGN KEY(reserva_id) REFERENCES reservas(id) ON DELETE CASCADE,
                FOREIGN KEY(alojamiento_id) REFERENCES alojamientos(id) ON DELETE CASCADE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 15) {
            db.execSQL("DROP TABLE IF EXISTS reservas")
            db.execSQL("DROP TABLE IF EXISTS reserva_alojamientos")
            db.execSQL("""
                CREATE TABLE reservas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT,
                    fechaInicio TEXT,
                    fechaFinal TEXT,
                    precio REAL,
                    telf TEXT,
                    noches INTEGER,
                    usuario_id INTEGER,
                    FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
                )
            """)
            db.execSQL("""
                CREATE TABLE reserva_alojamientos (
                    reserva_id INTEGER,
                    alojamiento_id INTEGER,
                    PRIMARY KEY(reserva_id, alojamiento_id),
                    FOREIGN KEY(reserva_id) REFERENCES reservas(id) ON DELETE CASCADE,
                    FOREIGN KEY(alojamiento_id) REFERENCES alojamientos(id) ON DELETE CASCADE
                )
            """)
        }
    }

    fun validarUsuario(usuario: String, pass: String): Long {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM usuarios WHERE usuario = ? AND password = ?", arrayOf(usuario, pass))
        var id: Long = -1
        if (cursor.moveToFirst()) id = cursor.getLong(0)
        cursor.close()
        return id
    }

    fun insertarUsuario(usuario: String, email: String, pass: String, telf: String): Long {
        val values = ContentValues().apply {
            put("usuario", usuario); put("email", email); put("password", pass); put("telefono", telf)
        }
        return writableDatabase.insert("usuarios", null, values)
    }

    fun actualizarUsuario(id: Long, usuario: String, email: String, pass: String, telf: String): Int {
        val values = ContentValues().apply {
            put("usuario", usuario); put("email", email); put("password", pass); put("telefono", telf)
        }
        return writableDatabase.update("usuarios", values, "id = ?", arrayOf(id.toString()))
    }

    fun obtenerUsuario(id: Long): Usuarios? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuarios WHERE id = ?", arrayOf(id.toString()))
        var usuario: Usuarios? = null
        if (cursor.moveToFirst()) {
            usuario = Usuarios(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4))
        }
        cursor.close()
        return usuario
    }

    fun insertarAlojamiento(nombre : String, precio: Double, usuarioId: Long): Long {
        val values = ContentValues().apply {
            put("nombre", nombre); put("precio", precio); put("usuario_id", usuarioId)
        }
        return writableDatabase.insert("alojamientos", null, values)
    }

    fun obtenerAlojamientosPorUsuario(usuarioId: Long): List<Alojamientos> {
        val lista = mutableListOf<Alojamientos>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM alojamientos WHERE usuario_id = ?", arrayOf(usuarioId.toString()))
        while (cursor.moveToNext()) {
            lista.add(Alojamientos(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2)))
        }
        cursor.close()
        return lista
    }

    fun obtenerReservasPorUsuario(usuarioId: Long): MutableList<Reservas> {
        val lista = mutableListOf<Reservas>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM reservas WHERE usuario_id = ?", arrayOf(usuarioId.toString()))
        while (cursor.moveToNext()) {
            lista.add(Reservas(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5), cursor.getInt(6)))
        }
        cursor.close()
        return lista
    }

    fun borrarReserva(id:Int) {
        writableDatabase.delete("reservas", "id=?", arrayOf(id.toString()))
        writableDatabase.delete("reserva_alojamientos", "reserva_id=?", arrayOf(id.toString()))
    }

    fun borrarAlojamiento(id:Int) {
        writableDatabase.delete("alojamientos", "id=?", arrayOf(id.toString()))
    }

    fun fechaOcupada(nuevaIniStr: String, nuevaFinStr: String, usuarioId: Long): Boolean {
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val nuevaIni = sdf.parse(nuevaIniStr) ?: return false
        val nuevaFin = sdf.parse(nuevaFinStr) ?: return false
        
        val reservasHost = obtenerReservasPorUsuario(usuarioId)
        for (reserva in reservasHost) {
            val exIni = sdf.parse(reserva.fechaInicio) ?: continue
            val exFin = sdf.parse(reserva.fechaFinal) ?: continue
            if (nuevaIni.before(exFin) && exIni.before(nuevaFin)) return true
        }
        return false
    }

    fun insertarReserva(nombre: String, fIni: String, fFin: String, precio: Double, telf: String, noches: Int, usuarioId: Long): Long {
        val values = ContentValues().apply {
            put("nombre", nombre); put("fechaInicio", fIni); put("fechaFinal", fFin)
            put("precio", precio); put("telf", telf); put("noches", noches); put("usuario_id", usuarioId)
        }
        return writableDatabase.insert("reservas", null, values)
    }

    fun insertarReservaAlojamiento(reservaId: Long, alojamientoId: Int) {
        val values = ContentValues().apply {
            put("reserva_id", reservaId)
            put("alojamiento_id", alojamientoId)
        }
        writableDatabase.insert("reserva_alojamientos", null, values)
    }

    fun actualizarReserva(id: Int, nombre: String, fIni: String, fFin: String, precio: Double, telf: String, noches: Int): Int {
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("fechaInicio", fIni)
            put("fechaFinal", fFin)
            put("precio", precio)
            put("telf", telf)
            put("noches", noches)
        }
        return writableDatabase.update("reservas", values, "id = ?", arrayOf(id.toString()))
    }

    fun actualizarReservaAlojamientos(reservaId: Int, alojamientosIds: List<Int>) {
        writableDatabase.delete("reserva_alojamientos", "reserva_id = ?", arrayOf(reservaId.toString()))
        for (alojId in alojamientosIds) {
            insertarReservaAlojamiento(reservaId.toLong(), alojId)
        }
    }

    fun obtenerAlojamientosPorReserva(reservaId: Int): List<Int> {
        val ids = mutableListOf<Int>()
        val cursor = readableDatabase.rawQuery("SELECT alojamiento_id FROM reserva_alojamientos WHERE reserva_id = ?", arrayOf(reservaId.toString()))
        while (cursor.moveToNext()) {
            ids.add(cursor.getInt(0))
        }
        cursor.close()
        return ids
    }

    fun obtenerIngresos(usuarioId: Long, alojamientoId: Int?, tipo: String): Double {
        val db = readableDatabase
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val hoy = Calendar.getInstance()
        
        var total = 0.0
        
        val query = if (alojamientoId != null) {
            "SELECT r.* FROM reservas r JOIN reserva_alojamientos ra ON r.id = ra.reserva_id WHERE r.usuario_id = ? AND ra.alojamiento_id = ?"
        } else {
            "SELECT * FROM reservas WHERE usuario_id = ?"
        }
        
        val args = if (alojamientoId != null) arrayOf(usuarioId.toString(), alojamientoId.toString()) else arrayOf(usuarioId.toString())
        val cursor = db.rawQuery(query, args)
        
        while (cursor.moveToNext()) {
            val fIniStr = cursor.getString(cursor.getColumnIndexOrThrow("fechaInicio"))
            val precioTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("precio"))
            
            var precioProporcional = precioTotal
            if (alojamientoId != null) {
                val numAlojamientos = obtenerNumAlojamientosEnReserva(cursor.getLong(cursor.getColumnIndexOrThrow("id")))
                if (numAlojamientos > 0) precioProporcional = precioTotal / numAlojamientos
            }

            try {
                val fecha = sdf.parse(fIniStr) ?: continue
                val cal = Calendar.getInstance().apply { time = fecha }
                
                val coincide = when (tipo) {
                    "DIARIO" -> cal.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)
                    "MENSUAL" -> cal.get(Calendar.MONTH) == hoy.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)
                    "ANUAL" -> cal.get(Calendar.YEAR) == hoy.get(Calendar.YEAR)
                    else -> false
                }
                
                if (coincide) total += precioProporcional
            } catch (e: Exception) {}
        }
        cursor.close()
        return total
    }

    private fun obtenerNumAlojamientosEnReserva(reservaId: Long): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM reserva_alojamientos WHERE reserva_id = ?", arrayOf(reservaId.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }
}
