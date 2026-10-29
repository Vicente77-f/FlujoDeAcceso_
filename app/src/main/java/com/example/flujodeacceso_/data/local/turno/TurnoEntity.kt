package com.example.flujodeacceso_.data.local.turno

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.TipoTurno
import com.example.flujodeacceso_.model.Turno

@Entity(tableName = "turnos")
data class TurnoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val usuarioId: Long,
    val acceso: Acceso,             // Se guarda como "A1", "A2"...
    val tipo: TipoTurno,            // Se guarda como "AM" o "PM"
    val fecha: String,              // "AAAA-MM-DD"
    val horaInicioReal: String,
    val horaFinReal: String?,       // NULL en la tabla = turno abierto
    val fotoUri: String,
    val exportado: Boolean = false  // Room lo guarda como 0 o 1
)

/** Fila de la tabla → modelo de la app. */
fun TurnoEntity.aModelo(): Turno = Turno(
    id = id, usuarioId = usuarioId, acceso = acceso, tipo = tipo, fecha = fecha,
    horaInicioReal = horaInicioReal, horaFinReal = horaFinReal,
    fotoUri = fotoUri, exportado = exportado
)

/** Modelo de la app → fila de la tabla (para insertar, actualizar o eliminar). */
fun Turno.aEntidad(): TurnoEntity = TurnoEntity(
    id = id, usuarioId = usuarioId, acceso = acceso, tipo = tipo, fecha = fecha,
    horaInicioReal = horaInicioReal, horaFinReal = horaFinReal,
    fotoUri = fotoUri, exportado = exportado
)