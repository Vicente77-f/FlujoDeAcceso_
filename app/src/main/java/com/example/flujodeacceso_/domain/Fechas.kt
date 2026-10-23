package com.example.flujodeacceso_.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun fechaHoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

fun horaActualTexto(): String = SimpleDateFormat("HH:mm", Locale.US).format(Date())

fun horaActual(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)