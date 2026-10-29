package com.example.flujodeacceso_.data.local.turno

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.ConteoAcceso

@Entity(
    tableName = "conteos",
    primaryKeys = ["turnoId", "hora"],   // Un solo registro por turno y por hora
    foreignKeys = [
        ForeignKey(
            entity = TurnoEntity::class,
            parentColumns = ["id"],
            childColumns = ["turnoId"],
            onDelete = ForeignKey.CASCADE // Al borrar un turno se borran sus conteos
        )
    ],
    indices = [Index("turnoId")]
)
data class ConteoAccesoEntity(
    val turnoId: Long,
    val acceso: Acceso,
    val fecha: String,
    val hora: Int,
    val hombres: Int,
    val mujeres: Int,
    val menores: Int,
    val sinClasificar: Int,
    val transito: Int
)

fun ConteoAccesoEntity.aModelo(): ConteoAcceso = ConteoAcceso(
    turnoId = turnoId, acceso = acceso, fecha = fecha, hora = hora,
    hombres = hombres, mujeres = mujeres, menores = menores,
    sinClasificar = sinClasificar, transito = transito
)

fun ConteoAcceso.aEntidad(): ConteoAccesoEntity = ConteoAccesoEntity(
    turnoId = turnoId, acceso = acceso, fecha = fecha, hora = hora,
    hombres = hombres, mujeres = mujeres, menores = menores,
    sinClasificar = sinClasificar, transito = transito
)