package com.example.flujodeacceso_.data.remote.turno

import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.ConteoAcceso
import com.example.flujodeacceso_.model.TipoTurno
import com.example.flujodeacceso_.model.Turno

// Gson convierte los enum por su nombre: "A1" ↔ Acceso.A1, "AM" ↔ TipoTurno.AM

data class TurnoRequestDto(
    val usuarioId: Long, val acceso: Acceso, val tipo: TipoTurno,
    val fecha: String, val horaInicioReal: String, val fotoUri: String
)

data class TurnoDto(
    val id: Long, val usuarioId: Long, val acceso: Acceso, val tipo: TipoTurno,
    val fecha: String, val horaInicioReal: String, val horaFinReal: String?,
    val fotoUri: String?, val exportado: Boolean, val corregido: Boolean
)

data class CerrarTurnoRequestDto(val horaFin: String)

data class ConteoRequestDto(
    val hombres: Int, val mujeres: Int, val menores: Int, val sinClasificar: Int, val transito: Int
)

data class CorreccionRequestDto(
    val supervisorId: Long,
    val hombres: Int, val mujeres: Int, val menores: Int, val sinClasificar: Int, val transito: Int
)

data class ConteoDto(
    val turnoId: Long, val acceso: Acceso, val fecha: String, val hora: Int,
    val hombres: Int, val mujeres: Int, val menores: Int, val sinClasificar: Int, val transito: Int
)

// ---------- Conversiones ----------
fun TurnoDto.aTurno(): Turno = Turno(
    id = id, usuarioId = usuarioId, acceso = acceso, tipo = tipo, fecha = fecha,
    horaInicioReal = horaInicioReal, horaFinReal = horaFinReal,
    fotoUri = fotoUri ?: "", exportado = exportado
)

fun Turno.aRequest(): TurnoRequestDto =
    TurnoRequestDto(usuarioId, acceso, tipo, fecha, horaInicioReal, fotoUri)

fun ConteoDto.aConteo(): ConteoAcceso = ConteoAcceso(
    turnoId = turnoId, acceso = acceso, fecha = fecha, hora = hora,
    hombres = hombres, mujeres = mujeres, menores = menores,
    sinClasificar = sinClasificar, transito = transito
)

fun ConteoAcceso.aRequest(): ConteoRequestDto =
    ConteoRequestDto(hombres, mujeres, menores, sinClasificar, transito)

fun ConteoAcceso.aCorreccion(supervisorId: Long): CorreccionRequestDto =
    CorreccionRequestDto(supervisorId, hombres, mujeres, menores, sinClasificar, transito)