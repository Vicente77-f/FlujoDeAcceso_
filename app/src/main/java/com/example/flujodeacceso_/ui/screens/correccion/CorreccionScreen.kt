package com.example.flujodeacceso_.ui.screens.correccion

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flujodeacceso_.model.CategoriaConteo
import com.example.flujodeacceso_.viewmodel.CorreccionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorreccionScreen(
    onVolver: () -> Unit,
    viewModel: CorreccionViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

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
        Text("Corregir conteo", style = MaterialTheme.typography.headlineSmall)
        Text(estado.titulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // ---------- 1. Hora ----------
        Text("1. Elige la hora", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            estado.horas.forEach { hora ->
                FilterChip(
                    selected = estado.horaSeleccionada == hora,
                    onClick = { viewModel.seleccionarHora(hora) },
                    label = { Text("$hora:00") }
                )
            }
        }

        // ---------- 2. Valores ----------
        Text("2. Ajusta los valores", style = MaterialTheme.typography.titleMedium)
        CategoriaConteo.entries.forEach { categoria ->
            OutlinedTextField(
                value = estado.valores[categoria] ?: "",
                onValueChange = { viewModel.onValorChange(categoria, it) },
                label = { Text(categoria.etiqueta) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ---------- Mensajes ----------
        estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        // ---------- Botones ----------
        Button(
            onClick = viewModel::guardar,
            enabled = !estado.guardando && estado.horaSeleccionada != null,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (estado.guardando) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            } else {
                Text("Guardar corrección")
            }
        }
        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al resumen")
        }
        Text(
            "La corrección necesita conexión: el servidor verifica que seas supervisor.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}