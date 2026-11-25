package com.example.flujodeacceso_.data.local.usuario

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
@Dao
interface UsuarioDao {

    /** Busca por correo sin distinguir mayúsculas. Devuelve null si no existe. */
    @Query("SELECT * FROM usuarios WHERE correo = :correo COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    /** Busca un usuario por su id (el resumen y el panel lo usan para mostrar quién hizo el turno). */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Long): UsuarioEntity?

    /** Guarda o actualiza la copia local del usuario que inició sesión en línea. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(usuario: UsuarioEntity)
}