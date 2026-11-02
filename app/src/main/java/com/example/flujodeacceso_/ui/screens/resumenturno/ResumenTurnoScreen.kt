package com.example.flujodeacceso_.ui.screens.resumenturno

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.viewmodel.ResumenTurnoViewModel
import kotlin.math.roundToInt

@Composable
fun ResumenTurnoScreen(
    onSalir: () -> Unit,
    textoSalir: String = "Cerrar sesión",
    viewModel: ResumenTurnoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Recurso nativo 2: el selector de documentos del sistema pregunta DÓNDE guardar el CSV
    val crearArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) viewModel.exportar(uri) else viewModel.onExportacionCancelada()
    }

    if (estado.cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- Encabezado ----------
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Resumen del turno", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(estado.titulo, style = MaterialTheme.typography.headlineSmall)
            Text(estado.subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Registrado por ${estado.responsable} · ${estado.horario}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FotoDelPuesto(estado.fotoUri)

        // ---------- Total ----------
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total de ingresos", style = MaterialTheme.typography.titleMedium)
                Text("${estado.totalIngresos}", style = MaterialTheme.typography.displaySmall)
                Text(
                    "De ellos, ${estado.valor(CategoriaConteo.TRANSITO)} solo cruzaron la tienda",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // ---------- Por categoría ----------
        Text("Por categoría", style = MaterialTheme.typography.titleMedium)
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoriaConteo.entries
                    .filter { it != CategoriaConteo.TRANSITO }
                    .forEach { categoria ->
                        BarraCategoria(categoria.etiqueta, estado.valor(categoria), estado.totalIngresos)
                    }
            }
        }

        // ---------- Por hora ----------
        Text("Por hora", style = MaterialTheme.typography.titleMedium)
        if (estado.porHora.isEmpty()) {
            Text("Sin registros en este turno", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    estado.porHora.forEachIndexed { indice, conteo ->
                        if (indice > 0) HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            Text("${conteo.hora}:00 - ${conteo.hora + 1}:00", modifier = Modifier.weight(1f))
                            Text("${conteo.totalIngresos} ingresos · ${conteo.transito} tránsito")
                        }
                    }
                }
            }
        }

        // ---------- Vista previa del CSV ----------
        Text("Vista previa del archivo", style = MaterialTheme.typography.titleMedium)
        Text(estado.nombreArchivo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(estado.vistaPreviaCsv, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
        }

        // ---------- Mensajes ----------
        Aviso(texto = estado.mensaje, esError = false)
        Aviso(texto = estado.error, esError = true)
        if (estado.exportado && estado.mensaje == null) {
            Aviso(texto = "Este turno ya fue exportado", esError = false)
        }

        // ---------- Botones ----------
        Button(
            onClick = {
                try {
                    crearArchivo.launch(estado.nombreArchivo)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No hay una app para guardar archivos", Toast.LENGTH_LONG).show()
                }
            },
            enabled = !estado.exportando && estado.totalIngresos > 0,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (estado.exportando) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Guardando…")
            } else {
                Icon(Icons.Filled.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (estado.exportado) "Exportar de nuevo" else "Exportar CSV")
            }
        }
        if (estado.totalIngresos == 0) {
            Text(
                "No hay registros para exportar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) {
            Text(textoSalir)
        }
    }
}

/** Foto del puesto con bordes redondeados, o un aviso si el turno no tiene foto. */
@Composable
private fun FotoDelPuesto(fotoUri: String?) {
    if (fotoUri != null) {
        AsyncImage(
            model = fotoUri,
            contentDescription = "Foto del puesto de trabajo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sin foto (dato de demostración)", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Fila con nombre, cantidad, porcentaje y una barra que crece con animación. */
@Composable
private fun BarraCategoria(etiqueta: String, valor: Int, total: Int) {
    val proporcion = if (total > 0) valor.toFloat() / total else 0f

    // Animación: al abrir la pantalla la barra parte en 0 y crece hasta su valor
    var mostrar by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mostrar = true }
    val progreso by animateFloatAsState(
        targetValue = if (mostrar) proporcion else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "barraCategoria"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(etiqueta, modifier = Modifier.weight(1f))
            Text("$valor (${(proporcion * 100).roundToInt()}%)", style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

/** Mensaje con ícono que aparece y desaparece con animación. */
@Composable
private fun Aviso(texto: String?, esError: Boolean) {
    AnimatedVisibility(visible = texto != null) {
        val color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                if (esError) Icons.Filled.Error else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(texto ?: "", color = color, style = MaterialTheme.typography.bodyMedium)
        }
    }
}