package com.example.flujodeacceso_.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Número que se desliza hacia arriba al aumentar y hacia abajo al disminuir. */
@Composable
fun NumeroAnimado(valor: Int, style: TextStyle, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = valor,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { altura -> altura } + fadeIn()) togetherWith
                        (slideOutVertically { altura -> -altura } + fadeOut())
            } else {
                (slideInVertically { altura -> -altura } + fadeIn()) togetherWith
                        (slideOutVertically { altura -> altura } + fadeOut())
            }
        },
        contentAlignment = Alignment.Center,
        label = "numero",
        modifier = modifier
    ) { numero ->
        Text(text = numero.toString(), style = style, textAlign = TextAlign.Center)
    }
}

/** Fila: etiqueta, botón −, número animado y botón +. */
@Composable
fun FilaContador(
    etiqueta: String,
    descripcion: String,
    valor: Int,
    restarHabilitado: Boolean,
    onRestar: () -> Unit,
    onSumar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(etiqueta, style = MaterialTheme.typography.titleMedium)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedIconButton(
            onClick = onRestar,
            enabled = restarHabilitado,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Restar 1 a $etiqueta")
        }
        NumeroAnimado(
            valor = valor,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.width(56.dp)
        )
        FilledIconButton(onClick = onSumar, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Sumar 1 a $etiqueta")
        }
    }
}