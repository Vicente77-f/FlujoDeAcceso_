# Flujo de Acceso

Aplicación Android interna para que el personal de una tienda departamental registre cuántas personas ingresan por cada acceso, y cuántas solo atraviesan la tienda hacia otra zona del centro comercial.

> **Proyecto académico (MVP).** Todos los datos son **ficticios**. La clasificación por categoría es visual y aproximada: la app no identifica personas, no usa reconocimiento facial y no guarda datos personales. Una transacción no equivale a un comprador.

**Asignatura:** DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

**Backend (microservicios):** https://github.com/MatiasErices/FlujoDeAcceso-backend

## Integrantes
- Matías Erices
- Vicente Fernández

## Funcionalidades

**Empleado**
- Inicio de sesión con validación por campo (correo y contraseña).
- Iniciar turno: elegir turno (AM 10:00-15:00 / PM 15:00-20:00), acceso asignado y tomar una **foto del puesto** (sin personas).
- Contador por categoría (hombre adulto, mujer adulta, menor, sin clasificar) y "solo cruzaron la tienda" (tránsito), con validación, deshacer y números animados. Cada toque se guarda en la base de datos.
- Resumen del turno con totales, composición, detalle por hora y **exportación a CSV**.
- Clima actual de Santiago al iniciar el turno (API externa Open-Meteo).

**Supervisor**
- Panel con ingresos AM vs PM, composición, tabla por acceso, ingresos vs. transacciones y lista de turnos del día.
- Abrir y exportar el resumen de cualquier turno.
- Eliminar un turno con confirmación (se borran también sus conteos).
- **Corregir los conteos** de un turno ya guardado. El servidor verifica que sea supervisor y marca el turno como *corregido*.
- El panel se actualiza desde el servidor y muestra los turnos registrados en otros teléfonos.

## Requisitos técnicos

| Requisito | Implementación |
|---|---|
| Arquitectura MVVM | `model/`, `viewmodel/`, `data/repository/`, `ui/screens/` |
| Navegación | Navigation Compose con rutas con parámetro (`contador/{turnoId}`) y transiciones animadas |
| Formularios y validación | Validaciones en `domain/validation/` (funciones que devuelven el mensaje de error), errores por campo con ícono |
| Estado | `StateFlow` en los ViewModels; los datos sobreviven al girar la pantalla |
| Animaciones | Transiciones entre pantallas, números animados, barras animadas y mensajes con `AnimatedVisibility` |
| Persistencia local | **Room** (SQLite): tablas `usuarios`, `turnos` y `conteos`, con inserción, consulta, actualización y eliminación |
| Recurso nativo 1 | **Cámara**: `ActivityResultContracts.TakePicture` + `FileProvider` (foto en almacenamiento privado) |
| Recurso nativo 2 | **Archivos**: `ActivityResultContracts.CreateDocument` para guardar el CSV donde el usuario elija |
| Seguridad | Contraseñas guardadas como hash SHA-256. Ningún recurso nativo pide permisos peligrosos: la cámara la maneja la app del sistema y el selector de documentos da acceso solo al archivo elegido |
| Consumo de API REST | **Retrofit 2.11** + Gson: `data/remote/` (un `ApiService` por servidor) y `RetrofitInstance` |
| Microservicios propios | `usuarioMS` (login y roles) y `turnoMS` (turnos, conteos y corrección), en Spring Boot + MySQL |
| API externa | **Open-Meteo**: temperatura, precipitación y estado del cielo en Santiago |
| Funcionamiento sin conexión | Room es la copia local. Cada cambio se guarda primero en el teléfono (`sincronizado = false`) y se envía a `turnoMS` cuando hay conexión, en orden: turno → conteos → cierre. El login usa la copia local si el servidor no responde |
| Pruebas unitarias | Repositorios con API y DAO falsos: login en línea y sin conexión, orden de sincronización, corrección (403 / sin conexión) y conversión de DTO |

## Formato del CSV

```
fecha,turno,acceso,hora,hombres,mujeres,menores,sin_clasificar,transito
2026-10-13,AM,A1,10,30,36,10,2,14
```
Nombre sugerido: `flujo_2026-10-13_AM_A1.csv`. Solo incluye las horas con registros.

## Tecnologías
Kotlin 2.0.21 · Jetpack Compose (BOM 2024.09.00) · Material Design 3 · Navigation Compose 2.9.5 · Room 2.6.1 con KSP · Coil 2.7.0 · AGP 8.13 · Gradle 8.13 · minSdk 24, targetSdk 36.


## Cómo ejecutar
1. **Levantar el backend** (ver el README del repositorio del backend): MySQL en XAMPP, después `usuarioMS` (8081) y `turnoMS` (8082).
2. Clonar la app: `git clone https://github.com/Vicente77-f/FlujoDeAcceso_.git`
3. Abrirla en Android Studio (Narwhal 3 Feature Drop 2025.1.3 o más reciente) y esperar la sincronización de Gradle (requiere internet la primera vez).
4. En *Settings > Build, Execution, Deployment > Build Tools > Gradle*, elegir como **Gradle JDK** un JDK **17 o 21** (por ejemplo, el JBR incluido). No aceptar el "AGP Upgrade Assistant".
5. Crear o iniciar un **emulador** (API 24 o superior). La app se conecta a `10.0.2.2`, que es el PC visto desde el emulador. Para un teléfono físico, cambiar las URL de `RetrofitInstance` por la IP del PC (los dos en la misma red Wi-Fi).
6. Ejecutar la app (▶). Al instalarse por primera vez se cargan los datos de demostración del 13-oct-2026.

Si el backend no está encendido, la app funciona igual con los datos guardados en el teléfono. Los cambios se envían cuando vuelve la conexión.

## APK firmado
El APK firmado se publica en la sección **Releases** de este repositorio. La llave de firma (`.jks`) **no** se sube al repositorio.

## Usuarios de demostración

| Rol | Correo             | Contraseña |
|---|--------------------|---|
| Empleado | camila.r@duoc.cl   | demo1234 |
| Empleado | diego.m@duoc.cl    | demo1234 |
| Supervisor | supervisor@duoc.cl | super1234 |

También existen 6 empleados ficticios más (Sofía L., Tomás V., Javiera P., Benjamín O., Martina C. e Ignacio S.), todos con la contraseña `demo1234`, que aparecen en los turnos de demostración.

## Pruebas
Pruebas unitarias en `app/src/test`: validaciones de login y turno, lógica del contador, modelos, datos de demostración, generador de CSV, ventas, **login con usuarioMS y respaldo en Room**, **sincronización y corrección de turnos con turnoMS** y **conversión de los datos de la API**. Para ejecutarlas: clic derecho en la carpeta `test` > *Run Tests*.