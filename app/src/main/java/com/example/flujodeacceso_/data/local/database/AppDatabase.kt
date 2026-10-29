package com.example.flujodeacceso_.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.flujodeacceso_.data.local.turno.ConteoAccesoEntity
import com.example.flujodeacceso_.data.local.turno.TurnoDao
import com.example.flujodeacceso_.data.local.turno.TurnoEntity
import com.example.flujodeacceso_.data.local.usuario.UsuarioDao
import com.example.flujodeacceso_.data.local.usuario.UsuarioEntity

@Database(
    entities = [UsuarioEntity::class, TurnoEntity::class, ConteoAccesoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun turnoDao(): TurnoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flujo_acceso.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        // Igual que el profesor: onOpen corre cada vez que se abre la base,
                        // así la semilla también vuelve después de una migración destructiva.
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            sembrarDatos(db)
                        }
                    })
                    .fallbackToDestructiveMigration() // Si sube "version", borra y recrea las tablas
                    .build()
                    .also { instancia = it }
            }
    }
}