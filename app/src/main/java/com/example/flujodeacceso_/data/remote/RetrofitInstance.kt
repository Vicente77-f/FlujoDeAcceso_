package com.example.flujodeacceso_.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // 10.0.2.2 es el "localhost" del PC visto desde el emulador
    const val URL_USUARIOS = "http://10.0.2.2:8081/"
    const val URL_TURNOS = "http://10.0.2.2:8082/"
    const val URL_CLIMA = "https://api.open-meteo.com/"

    // Si el servidor no responde en 5 segundos, la app usa la copia local (Room)
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    /** Crea un Retrofit para una URL base. Cada ApiService lo usa con su propia URL. */
    fun crear(urlBase: String): Retrofit = Retrofit.Builder()
        .baseUrl(urlBase)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}