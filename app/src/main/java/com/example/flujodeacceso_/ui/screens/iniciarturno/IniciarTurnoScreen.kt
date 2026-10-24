package com.example.flujodeacceso_.ui.screens.iniciarturno

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.flujodeacceso_.model.Acceso
import com.example.flujodeacceso_.model.TipoTurno
import com.example.flujodeacceso_.viewmodel.IniciarTurnoViewModel
import java.io.File

/** Crea un archivo vacío (con nombre único) en la carpeta privada "fotos". */
private fun crearArchivoFoto(context: Context): File {
    val carpeta = File(context.filesDir, "fotos").apply { if (!exists()) mkdirs() }
    return File(carpeta, "puesto_${System.currentTimeMillis()}.jpg")
}

/** Dirección segura (content://) del archivo, para entregársela a la app de cámara. */
private fun uriSegura(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

@Composable
fun IniciarTurnoScreen(
    onTurnoIniciado: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: IniciarTurnoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Dirección de la foto mientras la cámara está abierta. rememberSaveable: si Android
    // recrea la pantalla mientras se usa la cámara, no se pierde.
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    // Lanzador de la cámara: al volver, "exito" indica si se guardó una foto
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val uri = uriPendiente
        if (exito && uri != null) {
            viewModel.onFotoTomada(uri)
        } else {
            Toast.makeText(context, "No se tomó la foto", Toast.LENGTH_SHORT).show()
        }
        uriPendiente = null
    }

    val tomarFoto: () -> Unit = {
        val uri = uriSegura(context, crearArchivoFoto(context))
        uriPendiente = uri.toString()
        try {
            camara.launch(uri)
        } catch (e: ActivityNotFoundException) {
            uriPendiente = null
            Toast.makeText(context, "No hay una app de cámara disponible", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(estado.turnoCreadoId) {
        estado.turnoCreadoId?.let { id ->
            onTurnoIniciado(id)
            viewModel.onNavegacionRealizada()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ---------- Encabezado ----------
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Hola, ${estado.nombreUsuario}", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Inicia tu turno para comenzar a contar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCerrarSesion) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
            }
        }

        // ---------- 1. Turno ----------
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("1. Turno", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TipoTurno.entries.forEach { tipo ->
                    val seleccionado = estado.tipo == tipo
                    OutlinedCard(
                        onClick = { viewModel.onTipoChange(tipo) },
                        border = BorderStroke(
                            width = if (seleccionado) 2.dp else 1.dp,
                            color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (seleccionado) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tipo.etiqueta, style = MaterialTheme.typography.titleMedium)
                            Text(tipo.horario, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            MensajeError(estado.errorTipo)
        }

        // ---------- 2. Acceso ----------
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("2. Acceso asignado", style = MaterialTheme.typography.titleMedium)
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Acceso.entries.forEach { acceso ->
                    val seleccionado = estado.acceso == acceso
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = seleccionado,
                                onClick = { viewModel.onAccesoChange(acceso) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = seleccionado, onClick = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("${acceso.name} · ${acceso.nombre}", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                acceso.conectaCon,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            MensajeError(estado.errorAcceso)
        }

        // ---------- 3. Foto del puesto ----------
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("3. Foto del puesto", style = MaterialTheme.typography.titleMedium)
            val foto = estado.fotoUri
            if (foto != null) {
                AsyncImage(
                    model = foto,
                    contentDescription = "Foto del puesto de trabajo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Text(
                    "Tomada a las ${estado.horaFoto}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text("Sin foto", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            OutlinedButton(onClick = tomarFoto, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (foto == null) "Tomar foto" else "Repetir foto")
            }
            Text(
                "Fotografía el acceso o su letrero, sin personas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MensajeError(estado.errorFoto)
        }

        MensajeError(estado.errorGeneral)

        // ---------- Botón ----------
        Button(
            onClick = viewModel::comenzarTurno,
            enabled = !estado.guardando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (estado.guardando) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Guardando…")
            } else {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Comenzar turno")
            }
        }
    }
}

/** Mensaje de error con ícono, que aparece y desaparece con animación. */
@Composable
private fun MensajeError(texto: String?) {
    AnimatedVisibility(visible = texto != null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Text(texto ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}