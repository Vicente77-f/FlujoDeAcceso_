package com.example.flujodeacceso_.ui.screens.panelsupervisor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flujodeacceso_.model.Turno
import com.example.flujodeacceso_.ui.components.NumeroAnimado
import com.example.flujodeacceso_.viewmodel.PanelSupervisorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelSupervisorScreen(
    onAbrirTurno: (Long) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PanelSupervisorViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Diálogo de confirmación: su estado vive en el ViewModel, así sobrevive al girar
    estado.turnoPorEliminar?.let { turno ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminar,
            title = { Text("¿Eliminar turno?") },
            text = {
                Text(
                    "Se borrará el turno ${turno.tipo.name} · ${turno.acceso.name} del ${turno.fecha} " +
                            "y todos sus conteos. Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarEliminar) { Text("Cancelar") }
            }
        )
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
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Panel del supervisor", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Ingresos registrados por el personal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCerrarSesion) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
            }
        }

        // ---------- Selector de día ----------
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = estado.esDemo, onClick = viewModel::verDemo, label = { Text("13-oct (demo)") })
            FilterChip(selected = !estado.esDemo, onClick = viewModel::verHoy, label = { Text("Hoy") })
        }

        // ---------- AM vs PM ----------
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaTurno("Turno AM", estado.totalAm, Modifier.weight(1f))
            TarjetaTurno("Turno PM", estado.totalPm, Modifier.weight(1f))
        }
        Text(
            "Total del día: ${estado.totalIngresos} ingresos · ${estado.transito} solo cruzaron la tienda",
            style = MaterialTheme.typography.bodyMedium
        )

        // ---------- Composición ----------
        Text("Composición", style = MaterialTheme.typography.titleMedium)
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                estado.composicion.forEach { (categoria, valor) ->
                    FilaComposicion(categoria.etiqueta, valor, estado.totalIngresos)
                }
            }
        }

        // ---------- Tabla por acceso ----------
        Text("Ingresos por acceso", style = MaterialTheme.typography.titleMedium)
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                FilaTabla("Acceso", "AM", "PM", "Total", destacada = true)
                estado.filas.forEach { fila ->
                    HorizontalDivider()
                    FilaTabla(fila.acceso.name, "${fila.am}", "${fila.pm}", "${fila.total}")
                }
                HorizontalDivider()
                FilaTabla("Total", "${estado.totalAm}", "${estado.totalPm}", "${estado.totalIngresos}", destacada = true)
            }
        }

        // ---------- Ingresos vs. transacciones (solo si hay datos de caja) ----------
        estado.transacciones?.let { transacciones ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Ingresos vs. transacciones", style = MaterialTheme.typography.titleMedium)
                    Text("$transacciones transacciones en caja (datos de demostración)")
                    Text("${estado.transaccionesPor100} transacciones por cada 100 ingresos")
                    Text(
                        "Estimación: una transacción no equivale a un comprador.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // ---------- Turnos del día ----------
        Text("Turnos del día", style = MaterialTheme.typography.titleMedium)
        AnimatedVisibility(visible = estado.mensaje != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(estado.mensaje ?: "", color = MaterialTheme.colorScheme.primary)
            }
        }
        if (estado.turnos.isEmpty()) {
            Text("Aún no hay turnos registrados en este día", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        estado.turnos.forEach { item ->
            OutlinedCard(onClick = { onAbrirTurno(item.turno.id) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${item.turno.tipo.name} · ${item.turno.acceso.name} · ${item.responsable}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "${item.totalIngresos} ingresos · ${estadoDelTurno(item.turno)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.turno.exportado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.pedirEliminar(item.turno) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar turno", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun estadoDelTurno(turno: Turno): String = when {
    turno.estaAbierto -> "En curso"
    turno.exportado -> "Exportado"
    else -> "Pendiente de exportar"
}

@Composable
private fun TarjetaTurno(titulo: String, valor: Int, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge)
            NumeroAnimado(valor = valor, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun FilaComposicion(etiqueta: String, valor: Int, total: Int) {
    val proporcion = if (total > 0) valor.toFloat() / total else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(etiqueta, modifier = Modifier.weight(1f))
            Text("$valor (${(proporcion * 100).toInt()}%)")
        }
        LinearProgressIndicator(
            progress = { proporcion },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun FilaTabla(c1: String, c2: String, c3: String, c4: String, destacada: Boolean = false) {
    val peso = if (destacada) FontWeight.Bold else FontWeight.Normal
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(c1, fontWeight = peso, modifier = Modifier.weight(1.4f))
        Text(c2, fontWeight = peso, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(c3, fontWeight = peso, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text(c4, fontWeight = peso, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}