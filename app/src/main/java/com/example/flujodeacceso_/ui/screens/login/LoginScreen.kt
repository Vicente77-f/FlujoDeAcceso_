package com.example.flujodeacceso_.ui.screens.login
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flujodeacceso_.model.Rol
import com.example.flujodeacceso_.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: (Rol) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    // Estado solo visual: rememberSaveable lo conserva al girar
    var mostrarClave by rememberSaveable { mutableStateOf(false) }

    // Cuando hay usuario ingresado, se navega UNA vez y se limpia el formulario
    LaunchedEffect(estado.usuarioIngresado) {
        estado.usuarioIngresado?.let { usuario ->
            onLoginExitoso(usuario.rol)
            viewModel.onNavegacionRealizada()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text(text = "Flujo de Acceso", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Uso interno del personal de tienda · versión de demostración",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        // ---------- Correo ----------
        OutlinedTextField(
            value = estado.correo,
            onValueChange = viewModel::onCorreoChange,
            label = { Text("Correo") },
            singleLine = true,
            isError = estado.errorCorreo != null,
            trailingIcon = {
                if (estado.errorCorreo != null) {
                    Icon(Icons.Filled.Error, contentDescription = "Error en el correo")
                }
            },
            supportingText = { estado.errorCorreo?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- Contraseña ----------
        OutlinedTextField(
            value = estado.clave,
            onValueChange = viewModel::onClaveChange,
            label = { Text("Contraseña") },
            singleLine = true,
            isError = estado.errorClave != null,
            visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (estado.errorClave != null) {
                        Icon(Icons.Filled.Error, contentDescription = "Error en la contraseña")
                    }
                    IconButton(onClick = { mostrarClave = !mostrarClave }) {
                        Icon(
                            imageVector = if (mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            },
            supportingText = { estado.errorClave?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.iniciarSesion() }),
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- Error general, con animación ----------
        AnimatedVisibility(
            visible = estado.errorGeneral != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = estado.errorGeneral ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ---------- Botón con indicador de carga ----------
        Button(
            onClick = viewModel::iniciarSesion,
            enabled = !estado.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Verificando…")
            } else {
                Text("Ingresar")
            }
        }

        Text(
            text = "Las cuentas las crea el supervisor de la tienda.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}