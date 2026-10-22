package com.example.flujodeacceso_.data.repository
import com.example.flujodeacceso_.domain.seguridad.hashClave
import com.example.flujodeacceso_.model.Rol
import com.example.flujodeacceso_.model.Usuario
import kotlinx.coroutines.delay


class UsuarioRepository {

    private data class Cuenta(val usuario: Usuario, val hash: String)

    private val cuentas = listOf(
        Cuenta(Usuario(1, "Camila R.", "camila.r@tienda-demo.cl", Rol.EMPLEADO), hashClave("demo1234")),
        Cuenta(Usuario(2, "Diego M.", "diego.m@tienda-demo.cl", Rol.EMPLEADO), hashClave("demo1234")),
        Cuenta(Usuario(3, "Supervisora Demo", "supervisor@tienda-demo.cl", Rol.SUPERVISOR), hashClave("super1234"))
    )

    suspend fun iniciarSesion(correo: String, clave: String): Result<Usuario> {
        delay(700)
        val cuenta = cuentas.find { it.usuario.correo.equals(correo.trim(), ignoreCase = true) }
        return if (cuenta != null && cuenta.hash == hashClave(clave)) {
            Result.success(cuenta.usuario)
        } else {
            Result.failure(IllegalArgumentException("Correo o contraseña incorrectos"))
        }

    }

}