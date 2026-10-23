package com.example.flujodeacceso_.domain.validation

import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.TipoTurno

fun validarTipoTurno(tipo: TipoTurno?): String? =
        if (tipo == null) "Seleciona el turno" else null

fun validarAcceso(acceso: Acceso?): String? =
    if (acceso ==null) "Selecciona el acceso asignado" else null

fun validarFotosPuesto(fotoUri: String?): String? =
    if (fotoUri.isNullOrBlank()) "Toma la foto del puesto para comenzar" else null
