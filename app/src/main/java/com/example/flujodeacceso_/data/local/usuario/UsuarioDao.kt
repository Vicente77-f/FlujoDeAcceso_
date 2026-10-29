package com.example.flujodeacceso_.data.local.usuario

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UsuarioDao {

    /** Busca por correo sin distinguir mayúsculas. Devuelve null si no existe. */
    @Query("SELECT * FROM usuarios WHERE correo = :correo COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    /** Se usará en la Ronda 2 para recuperar la sesión guardada en DataStore. */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Long): UsuarioEntity?
}