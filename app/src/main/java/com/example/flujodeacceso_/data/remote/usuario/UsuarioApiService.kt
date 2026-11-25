package com.example.flujodeacceso_.data.remote.usuario

import com.example.flujodeacceso_.data.remote.RetrofitInstance
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsuarioApiService {

    @POST("api/v1/usuarios/login")
    suspend fun login(@Body request: LoginRequestDto): UsuarioDto

    @GET("api/v1/usuarios/dto/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Long): UsuarioDto

    companion object {
        val instancia: UsuarioApiService by lazy {
            RetrofitInstance.crear(RetrofitInstance.URL_USUARIOS).create(UsuarioApiService::class.java)
        }
    }
}