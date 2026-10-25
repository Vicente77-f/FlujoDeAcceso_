package com.example.flujodeacceso_.ui.screens.contador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.ui.components.FilaContador
import com.example.flujodeacceso_.ui.components.NumeroAnimado
import com.example.flujodeacceso_.viewmodel.ContadorViewModel

@Composable
fun ContadorScreen(
    onFinalizarTurno: () -> Unit,
    viewModel: ContadorViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var confirmarFin by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.turnoFinalizado) {
        if (estado.turnoFinalizado) onFinalizarTurno()
    }

    if (estado.cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------- Encabezado ----------
            Text(estado.titulo, style = MaterialTheme.typography.headlineSmall)
            Text(
                estado.subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (estado.fueraDeHorario) {
                Text(
                    "Fuera del horario del turno: los registros se guardan en la franja de las " +
                            "${estado.franjaActual}:00 (modo demostración).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // ---------- Total ----------
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Personas que ingresaron", style = MaterialTheme.typography.bodyLarge)
                        Text("Franja actual: ${estado.franjaActual}:00", style = MaterialTheme.typography.bodySmall)
                    }
                    NumeroAnimado(valor = estado.totalIngresos, style = MaterialTheme.typography.displaySmall)
                }
            }

            // ---------- Categorías ----------
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    CategoriaConteo.entries
                        .filter { it != CategoriaConteo.TRANSITO }
                        .forEach { categoria ->
                            FilaContador(
                                etiqueta = categoria.etiqueta,
                                descripcion = categoria.descripcion,
                                valor = estado.valor(categoria),
                                restarHabilitado = categoria in estado.restables,
                                onRestar = { viewModel.restar(categoria) },
                                onSumar = { viewModel.sumar(categoria) }
                            )
                            HorizontalDivider()
                        }
                    Text(
                        "Clasificación visual aproximada. No se registran nombres, fotos ni datos personales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // ---------- Tránsito ----------
            Card(modifier = Modifier.fillMaxWidth()) {
                val transito = CategoriaConteo.TRANSITO
                FilaContador(
                    etiqueta = transito.etiqueta,
                    descripcion = transito.descripcion,
                    valor = estado.valor(transito),
                    restarHabilitado = transito in estado.restables,
                    onRestar = { viewModel.restar(transito) },
                    onSumar = { viewModel.sumar(transito) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ---------- Error de validación (animado) ----------
            AnimatedVisibility(visible = estado.error != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(estado.error ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            // ---------- Último registro + Deshacer (animado) ----------
            AnimatedVisibility(visible = estado.mensaje != null) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(estado.mensaje ?: "", modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = viewModel::deshacer,
                            enabled = estado.puedeDeshacer,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.inversePrimary
                            )
                        ) {
                            Text("Deshacer")
                        }
                    }
                }
            }

            // ---------- Finalizar ----------
            OutlinedButton(
                onClick = { confirmarFin = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Finalizar turno")
            }
        }
    }

    // ---------- Confirmación ----------
    if (confirmarFin) {
        AlertDialog(
            onDismissRequest = { confirmarFin = false },
            title = { Text("¿Finalizar turno?") },
            text = {
                Text("Se registrarán ${estado.totalIngresos} ingresos. Después no podrás seguir contando en este turno.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarFin = false
                    viewModel.finalizarTurno()
                }) { Text("Finalizar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarFin = false }) { Text("Cancelar") }
            }
        )
    }
}