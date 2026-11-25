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

    @Query("UPDATE turnos SET horaFinReal = :horaFin, sincronizado = 0 WHERE id = :id")
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

    // ---------- Sincronización con turnoMS ----------
    @Query("SELECT * FROM turnos WHERE idRemoto IS NULL")
    suspend fun turnosSinIdRemoto(): List<TurnoEntity>

    @Query("UPDATE turnos SET idRemoto = :idRemoto WHERE id = :id")
    suspend fun asignarIdRemoto(id: Long, idRemoto: Long)

    @Query("SELECT * FROM turnos WHERE idRemoto IS NOT NULL AND sincronizado = 0")
    suspend fun turnosPendientes(): List<TurnoEntity>

    /** Solo marca si el turno no cambió mientras se enviaba. */
    @Query("UPDATE turnos SET sincronizado = 1 WHERE id = :id AND exportado = :exportado AND horaFinReal IS :horaFin")
    suspend fun marcarTurnoSincronizado(id: Long, exportado: Boolean, horaFin: String?)

    @Query("SELECT * FROM turnos WHERE idRemoto = :idRemoto LIMIT 1")
    suspend fun buscarPorIdRemoto(idRemoto: Long): TurnoEntity?

    @Query("SELECT * FROM turnos WHERE fecha = :fecha")
    suspend fun listaTurnosDeFecha(fecha: String): List<TurnoEntity>

    @Query("SELECT * FROM conteos WHERE turnoId = :turnoId AND hora = :hora")
    suspend fun buscarConteo(turnoId: Long, hora: Int): ConteoAccesoEntity?

    @Query("SELECT * FROM conteos WHERE sincronizado = 0")
    suspend fun conteosPendientes(): List<ConteoAccesoEntity>

    @Query("SELECT COUNT(*) FROM conteos WHERE turnoId = :turnoId AND sincronizado = 0")
    suspend fun contarConteosPendientes(turnoId: Long): Int

    /** Solo marca si los valores no cambiaron mientras se enviaban (el empleado sigue tocando). */
    @Query(
        "UPDATE conteos SET sincronizado = 1 WHERE turnoId = :turnoId AND hora = :hora " +
                "AND hombres = :hombres AND mujeres = :mujeres AND menores = :menores " +
                "AND sinClasificar = :sinClasificar AND transito = :transito"
    )
    suspend fun marcarConteoSincronizado(
        turnoId: Long, hora: Int, hombres: Int, mujeres: Int,
        menores: Int, sinClasificar: Int, transito: Int
    )
}