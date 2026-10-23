package com.example.flujodeacceso_.model

/** Todo lo que muestra la pantalla Iniciar turno. Vive en el ViewModel: sobrevive al girar. */
data class IniciarTurnoUiState(
    val nombreUsuario: String = "",
    val tipo: TipoTurno? = null,         // null = aún no elegido
    val acceso: Acceso? = null,
    val fotoUri: String? = null,         // Dirección de la foto tomada
    val horaFoto: String? = null,        // "10:02"
    val errorTipo: String? = null,
    val errorAcceso: String? = null,
    val errorFoto: String? = null,
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val turnoCreadoId: Long? = null      // distinto de null = navegar al contador
)