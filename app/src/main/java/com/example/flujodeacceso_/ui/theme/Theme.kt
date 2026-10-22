package com.example.flujodeacceso_.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val EsquemaClaro = lightColorScheme(
    primary = VerdeAzulado,
    onPrimary = Color.White,
    primaryContainer = VerdeAzuladoClaro,
    onPrimaryContainer =  VerdeAzuladoOscuro,
    secondary = GrisVerdoso,
    onSecondary = Color.White,
    secondaryContainer = GrisVerdosoClaro,
    onSecondaryContainer = VerdeAzuladoOscuro,
    tertiary = Ambar,
    onTertiary = Color.White,
    tertiaryContainer = AmbarClaro,
    onTertiaryContainer = AmbarOscuro,
    background = FondoClaro,
    onBackground = TextoPrincipal,
    surface = FondoClaro,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,
    outline = Borde,
    error = RojoError,
    onError = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeAzuladoNoche,
    onPrimary = VerdeAzuladoOscuro,
    primaryContainer = VerdeAzuladoNocheContenedor,
    onPrimaryContainer = VerdeAzuladoClaro,
    tertiary = AmbarNoche,
    tertiaryContainer = AmbarOscuro,
    onTertiaryContainer = AmbarClaro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = FondoOscuro,
    onSurface = TextoClaro,
    surfaceVariant = TextoSecundario,
    onSurfaceVariant = TextoClaroSecundario,
    outline = Borde,
    error = RojoErrorNoche
)

@Composable
fun FlujoDeAcceso_Theme (
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamiColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamiColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> EsquemaOscuro
        else -> EsquemaClaro
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography =  Typography,
        content = content
    )
}
