package com.example.flujodeacceso_.data.local.usuario

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.flujodeacceso_.model.Rol
import com.example.flujodeacceso_.model.Usuario

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]  // No puede haber dos cuentas con el mismo correo
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nombre: String,
    val correo: String,
    val rol: Rol,            // Room guarda el enum como texto: "EMPLEADO" o "SUPERVISOR"
    val claveHash: String    // SHA-256 de la contraseña: la contraseña real nunca se guarda
)

/** Entidad → modelo. El hash no sale de la capa de datos. */
fun UsuarioEntity.aModelo(): Usuario =
    Usuario(id = id, nombre = nombre, correo = correo, rol = rol)