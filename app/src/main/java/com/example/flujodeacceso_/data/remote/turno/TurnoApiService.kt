package com.example.flujodeacceso_.data.remote.turno

import com.example.flujodeacceso_.data.remote.RetrofitInstance
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TurnoApiService {

    @POST("api/v1/turnos")
    suspend fun crearTurno(@Body request: TurnoRequestDto): TurnoDto

    @GET("api/v1/turnos")
    suspend fun turnosDeFecha(@Query("fecha") fecha: String): List<TurnoDto>

    @GET("api/v1/turnos/{id}")
    suspend fun obtenerTurno(@Path("id") id: Long): TurnoDto

    @PUT("api/v1/turnos/{id}/cerrar")
    suspend fun cerrarTurno(@Path("id") id: Long, @Body request: CerrarTurnoRequestDto): TurnoDto

    @PUT("api/v1/turnos/{id}/exportado")
    suspend fun marcarExportado(@Path("id") id: Long): TurnoDto

    @DELETE("api/v1/turnos/{id}")
    suspend fun eliminarTurno(@Path("id") id: Long): Response<Unit>   // 204: responde sin cuerpo

    @GET("api/v1/turnos/{id}/conteos")
    suspend fun conteosDeTurno(@Path("id") id: Long): List<ConteoDto>

    @PUT("api/v1/turnos/{id}/conteos/{hora}")
    suspend fun guardarConteo(
        @Path("id") id: Long, @Path("hora") hora: Int, @Body request: ConteoRequestDto
    ): ConteoDto

    @PUT("api/v1/turnos/{id}/conteos/{hora}/correccion")
    suspend fun corregirConteo(
        @Path("id") id: Long, @Path("hora") hora: Int, @Body request: CorreccionRequestDto
    ): ConteoDto

    @GET("api/v1/conteos")
    suspend fun conteosDeFecha(@Query("fecha") fecha: String): List<ConteoDto>

    companion object {
        val instancia: TurnoApiService by lazy {
            RetrofitInstance.crear(RetrofitInstance.URL_TURNOS).create(TurnoApiService::class.java)
        }
    }
}