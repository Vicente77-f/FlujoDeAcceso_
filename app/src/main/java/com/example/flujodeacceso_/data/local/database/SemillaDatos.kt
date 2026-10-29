package com.example.flujodeacceso_.data.local.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.flujodeacceso_.data.local.turno.ConteoAccesoEntity
import com.example.flujodeacceso_.data.local.turno.TurnoEntity
import com.example.flujodeacceso_.data.local.usuario.UsuarioEntity
import com.example.flujodeacceso_.domain.repartir
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.Rol
import com.example.flujodeacceso_.model.TipoTurno

/** Día de los datos de demostración (FICTICIOS, coherentes con el mockup). */
const val FECHA_DEMO = "2026-10-13"

/** SHA-256 de "demo1234" y de "super1234". En el código solo aparece el hash. */
const val HASH_DEMO = "0ead2060b65992dca4769af601a1b3a35ef38cfad2c2c465bb160ea764157c5d"
const val HASH_SUPERVISOR = "e80b9b5a428eaa804690ad5096312c0aa713f6253da6aa23784b1b2867e37472"

val usuariosSemilla = listOf(
    UsuarioEntity(1, "Camila R.", "camila.r@tienda-demo.cl", Rol.EMPLEADO, HASH_DEMO),
    UsuarioEntity(2, "Diego M.", "diego.m@tienda-demo.cl", Rol.EMPLEADO, HASH_DEMO),
    UsuarioEntity(3, "Supervisora Demo", "supervisor@tienda-demo.cl", Rol.SUPERVISOR, HASH_SUPERVISOR),
    UsuarioEntity(4, "Sofía L.", "sofia.l@tienda-demo.cl", Rol.EMPLEADO, HASH_DEMO),
    UsuarioEntity(5, "Tomás V.", "tomas.v@tienda-demo.cl", Rol.EMPLEADO, HASH_DEMO),
    UsuarioEntity(6, "Javiera P.", "javiera.p@tienda-demo.cl", Rol.EMPLEADO, HASH_DEMO),
    UsuarioEntity(7, "Benjamín O.", "benjamin.o@tienda-demo.cl", Rol.EMPLEADO, HASH_DEMO),
    UsuarioEntity(8, "Martina C.", "martina.c@tienda-demo.cl", Rol.EMPLEADO, HASH_DEMO),
    UsuarioEntity(9, "Ignacio S.", "ignacio.s@tienda-demo.cl", Rol.EMPLEADO, HASH_DEMO)
)

/** Totales de cada turno del 13-oct. El tránsito está INCLUIDO en los ingresos. */
private data class TurnoDemo(
    val id: Long, val usuarioId: Long, val acceso: Acceso, val tipo: TipoTurno,
    val hombres: Int, val mujeres: Int, val menores: Int, val sinClasificar: Int,
    val transito: Int, val exportado: Boolean
)

private val turnosDemo = listOf(
    TurnoDemo(1, 1, Acceso.A1, TipoTurno.AM, 210, 246, 71, 13, 128, exportado = true),   // 540
    TurnoDemo(2, 4, Acceso.A2, TipoTurno.AM, 148, 173, 50, 9, 54, exportado = true),     // 380
    TurnoDemo(3, 5, Acceso.A3, TipoTurno.AM, 117, 136, 40, 7, 28, exportado = true),     // 300
    TurnoDemo(4, 6, Acceso.A4, TipoTurno.AM, 307, 360, 104, 19, 283, exportado = true),  // 790
    TurnoDemo(5, 2, Acceso.A1, TipoTurno.PM, 272, 319, 92, 17, 165, exportado = true),   // 700
    TurnoDemo(6, 7, Acceso.A2, TipoTurno.PM, 187, 219, 63, 11, 68, exportado = true),    // 480
    TurnoDemo(7, 8, Acceso.A3, TipoTurno.PM, 121, 141, 41, 7, 29, exportado = false),    // 310
    TurnoDemo(8, 9, Acceso.A4, TipoTurno.PM, 288, 336, 99, 17, 265, exportado = false)   // 740
)

/** Porcentaje del turno que llega en cada una de sus 5 horas (suma 100). */
private val PESOS_POR_HORA = listOf(16, 20, 24, 22, 18)

fun turnosSemilla(): List<TurnoEntity> = turnosDemo.map { d ->
    TurnoEntity(
        id = d.id, usuarioId = d.usuarioId, acceso = d.acceso, tipo = d.tipo,
        fecha = FECHA_DEMO,
        horaInicioReal = "${d.tipo.horaInicio}:00",
        horaFinReal = "${d.tipo.horaFin}:00",
        fotoUri = "",                 // Dato de demostración: sin foto
        exportado = d.exportado
    )
}

fun conteosSemilla(): List<ConteoAccesoEntity> = turnosDemo.flatMap { d ->
    val hombres = repartir(d.hombres, PESOS_POR_HORA)
    val mujeres = repartir(d.mujeres, PESOS_POR_HORA)
    val menores = repartir(d.menores, PESOS_POR_HORA)
    val sinClasificar = repartir(d.sinClasificar, PESOS_POR_HORA)
    val transito = repartir(d.transito, PESOS_POR_HORA)
    (d.tipo.horaInicio until d.tipo.horaFin).mapIndexed { i, hora ->
        ConteoAccesoEntity(
            turnoId = d.id, acceso = d.acceso, fecha = FECHA_DEMO, hora = hora,
            hombres = hombres[i], mujeres = mujeres[i], menores = menores[i],
            sinClasificar = sinClasificar[i], transito = transito[i]
        )
    }
}

/** Se llama cada vez que se abre la base. IGNORE: si la fila ya existe, no hace nada. */
fun sembrarDatos(db: SupportSQLiteDatabase) {
    usuariosSemilla.forEach { u ->
        insertar(db, "usuarios", ContentValues().apply {
            put("id", u.id); put("nombre", u.nombre); put("correo", u.correo)
            put("rol", u.rol.name); put("claveHash", u.claveHash)
        })
    }
    // Solo con la tabla vacía: si el supervisor borra un turno demo, no "revive" al reiniciar
    if (!estaVacia(db, "turnos")) return
    turnosSemilla().forEach { t ->
        insertar(db, "turnos", ContentValues().apply {
            put("id", t.id); put("usuarioId", t.usuarioId)
            put("acceso", t.acceso.name); put("tipo", t.tipo.name); put("fecha", t.fecha)
            put("horaInicioReal", t.horaInicioReal); put("horaFinReal", t.horaFinReal)
            put("fotoUri", t.fotoUri); put("exportado", if (t.exportado) 1 else 0)
        })
    }
    conteosSemilla().forEach { c ->
        insertar(db, "conteos", ContentValues().apply {
            put("turnoId", c.turnoId); put("acceso", c.acceso.name); put("fecha", c.fecha)
            put("hora", c.hora); put("hombres", c.hombres); put("mujeres", c.mujeres)
            put("menores", c.menores); put("sinClasificar", c.sinClasificar); put("transito", c.transito)
        })
    }
}

private fun estaVacia(db: SupportSQLiteDatabase, tabla: String): Boolean =
    db.query("SELECT COUNT(*) FROM $tabla").use { cursor ->
        cursor.moveToFirst() && cursor.getInt(0) == 0
    }

private fun insertar(db: SupportSQLiteDatabase, tabla: String, valores: ContentValues) {
    db.insert(tabla, SQLiteDatabase.CONFLICT_IGNORE, valores)
}