# Flujo de Acceso

Aplicación Android interna para que el personal de una tienda departamental registre cuántas personas ingresan por cada acceso, y cuántas solo atraviesan la tienda hacia otra zona del centro comercial.

> **Proyecto académico (MVP).** Todos los datos son **ficticios**. La clasificación por categoría es visual y aproximada: la app no identifica personas, no usa reconocimiento facial y no guarda datos personales. Una transacción no equivale a un comprador.

**Asignatura:** DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

## Integrantes
- Matías Erices
- Vicente Fernández

## Funcionalidades

**Empleado**
- Inicio de sesión con validación por campo (correo y contraseña).
- Iniciar turno: elegir turno (AM 10:00-15:00 / PM 15:00-20:00), acceso asignado y tomar una **foto del puesto** (sin personas).
- Contador por categoría (hombre adulto, mujer adulta, menor, sin clasificar) y "solo cruzaron la tienda" (tránsito), con validación, deshacer y números animados. Cada toque se guarda en la base de datos.
- Resumen del turno con totales, composición, detalle por hora y **exportación a CSV**.

**Supervisor**
- Panel con ingresos AM vs PM, composición, tabla por acceso, ingresos vs. transacciones y lista de turnos del día.
- Abrir y exportar el resumen de cualquier turno.
- Eliminar un turno con confirmación (se borran también sus conteos).

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

## Formato del CSV

```
fecha,turno,acceso,hora,hombres,mujeres,menores,sin_clasificar,transito
2026-10-13,AM,A1,10,30,36,10,2,14
```
Nombre sugerido: `flujo_2026-10-13_AM_A1.csv`. Solo incluye las horas con registros.

## Tecnologías
Kotlin 2.0.21 · Jetpack Compose (BOM 2024.09.00) · Material Design 3 · Navigation Compose 2.9.5 · Room 2.6.1 con KSP · Coil 2.7.0 · AGP 8.13 · Gradle 8.13 · minSdk 24, targetSdk 36.

## Cómo ejecutar
1. Clonar el repositorio: `git clone https://github.com/Vicente77-f/FlujoDeAcceso_.git`
2. Abrirlo en Android Studio (Narwhal 3 Feature Drop 2025.1.3 o más reciente) y esperar la sincronización de Gradle (requiere internet la primera vez).
3. En *Settings > Build, Execution, Deployment > Build Tools > Gradle*, elegir como **Gradle JDK** un JDK **17 o 21** (por ejemplo, el JBR incluido). Gradle 8.13 no funciona con Java 25. No aceptar el "AGP Upgrade Assistant".
4. Crear o iniciar un emulador (API 24 o superior). Para la foto, la cámara trasera del emulador debe estar en *VirtualScene* o *Emulated* (Device Manager > Edit > Camera).
5. Ejecutar la app (▶). Al instalarse por primera vez se cargan los usuarios y los datos de demostración del 13-oct-2026.

## Usuarios de demostración

| Rol | Correo | Contraseña |
|---|---|---|
| Empleado | camila.r@tienda-demo.cl | demo1234 |
| Empleado | diego.m@tienda-demo.cl | demo1234 |
| Supervisor | supervisor@tienda-demo.cl | super1234 |

También existen 6 empleados ficticios más (Sofía L., Tomás V., Javiera P., Benjamín O., Martina C. e Ignacio S.), todos con la contraseña `demo1234`, que aparecen en los turnos de demostración.

## Pruebas
Pruebas unitarias en `app/src/test`: validaciones de login y turno, lógica del contador, modelos, datos de demostración, generador de CSV y datos de ventas. Para ejecutarlas: clic derecho en la carpeta `test` > *Run Tests*.
