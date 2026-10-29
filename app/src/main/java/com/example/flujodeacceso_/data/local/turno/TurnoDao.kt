package com.example.flujodeacceso_.data.local.turno

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    // ---------- Turnos ----------
    @Insert
    suspend fun insertarTurno(turno: TurnoEntity): Long        // Devuelve el id generado

    @Update
    suspend fun actualizarTurno(turno: TurnoEntity)            // Busca la fila por su id y la reemplaza

    @Delete
    suspend fun eliminarTurno(turno: TurnoEntity)              // CASCADE borra sus conteos

    @Query("SELECT * FROM turnos WHERE id = :id")
    suspend fun buscarTurno(id: Long): TurnoEntity?

    @Query("UPDATE turnos SET horaFinReal = :horaFin WHERE id = :id")
    suspend fun cerrarTurno(id: Long, horaFin: String)

    @Query("SELECT * FROM turnos WHERE usuarioId = :usuarioId AND horaFinReal IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun turnoAbiertoDe(usuarioId: Long): TurnoEntity?

    @Query("SELECT * FROM turnos WHERE fecha = :fecha ORDER BY tipo, acceso")
    fun turnosDeFecha(fecha: String): Flow<List<TurnoEntity>>

    // ---------- Conteos ----------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarConteo(conteo: ConteoAccesoEntity)      // Si ya existe esa hora, la reemplaza

    @Update
    suspend fun actualizarConteo(conteo: ConteoAccesoEntity)

    @Query("SELECT * FROM conteos WHERE turnoId = :turnoId ORDER BY hora")
    suspend fun conteosDeTurno(turnoId: Long): List<ConteoAccesoEntity>

    @Query("SELECT * FROM conteos WHERE fecha = :fecha")
    fun conteosDeFecha(fecha: String): Flow<List<ConteoAccesoEntity>>
}