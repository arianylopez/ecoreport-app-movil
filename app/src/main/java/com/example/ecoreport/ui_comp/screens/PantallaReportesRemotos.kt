package com.example.ecoreport.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecoreport.data.remote.CrearReporteDto
import com.example.ecoreport.data.remote.ReporteDto
import com.example.ecoreport.model.EstadoReporte

@Composable
fun PantallaReportesRemotos(
    reportes: List<ReporteDto>,
    cargando: Boolean,
    error: String?,
    crearReporte: (CrearReporteDto, () -> Unit) -> Unit,
    actualizarReporte: (Int, CrearReporteDto, () -> Unit) -> Unit,
    eliminarReporte: (Int, () -> Unit) -> Unit,
    recargar: () -> Unit,
    limpiarError: () -> Unit,
    volver: () -> Unit
) {
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    var idEnEdicion by rememberSaveable { mutableStateOf<Int?>(null) }
    var idPorEliminar by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Reportes remotos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = {
                limpiarError()
                idEnEdicion = null
                mostrarFormulario = true
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("NUEVO REPORTE")
        }

        if (cargando) CircularProgressIndicator()
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        OutlinedButton(
            onClick = recargar,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (error != null) "REINTENTAR CARGA" else "ACTUALIZAR LISTA")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (reportes.isEmpty() && !cargando && error == null) {
                item { Text("No existen reportes remotos.") }
            }
            items(reportes, key = { it.id }) { reporte ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(reporte.titulo, fontWeight = FontWeight.Bold)
                        Text(reporte.descripcion)
                        Text("Categoría: ${reporte.categoria}")
                        Text("Ubicación: ${reporte.ubicacion}")
                        Text("Estado: ${reporte.estado}")
                        Text("Fecha: ${reporte.fecha}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    limpiarError()
                                    idEnEdicion = reporte.id
                                    mostrarFormulario = true
                                },
                                enabled = !cargando
                            ) { Text("EDITAR") }
                            TextButton(
                                onClick = {
                                    limpiarError()
                                    idPorEliminar = reporte.id
                                },
                                enabled = !cargando
                            ) { Text("ELIMINAR") }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = volver,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) { Text("VOLVER") }
    }

    if (mostrarFormulario) {
        FormularioReporteRemoto(
            reporte = reportes.find { it.id == idEnEdicion },
            cargando = cargando,
            error = error,
            guardar = { datos ->
                val cerrar = { mostrarFormulario = false }
                val id = idEnEdicion
                if (id == null) crearReporte(datos, cerrar)
                else actualizarReporte(id, datos, cerrar)
            },
            cancelar = {
                limpiarError()
                mostrarFormulario = false
            }
        )
    }

    val reportePorEliminar = reportes.find { it.id == idPorEliminar }
    if (reportePorEliminar != null) {
        val cancelar = {
            limpiarError()
            idPorEliminar = null
        }
        AlertDialog(
            onDismissRequest = { if (!cargando) cancelar() },
            title = { Text("Eliminar reporte") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Eliminar \"${reportePorEliminar.titulo}\"? Esta acción no se puede deshacer.")
                    if (cargando) CircularProgressIndicator()
                    if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        eliminarReporte(reportePorEliminar.id) { idPorEliminar = null }
                    },
                    enabled = !cargando
                ) { Text("ELIMINAR") }
            },
            dismissButton = {
                TextButton(onClick = cancelar, enabled = !cargando) { Text("CANCELAR") }
            }
        )
    }
}

@Composable
private fun FormularioReporteRemoto(
    reporte: ReporteDto?,
    cargando: Boolean,
    error: String?,
    guardar: (CrearReporteDto) -> Unit,
    cancelar: () -> Unit
) {
    var titulo by rememberSaveable(reporte?.id) { mutableStateOf(reporte?.titulo.orEmpty()) }
    var descripcion by rememberSaveable(reporte?.id) { mutableStateOf(reporte?.descripcion.orEmpty()) }
    var categoria by rememberSaveable(reporte?.id) { mutableStateOf(reporte?.categoria.orEmpty()) }
    var ubicacion by rememberSaveable(reporte?.id) { mutableStateOf(reporte?.ubicacion.orEmpty()) }
    var fecha by rememberSaveable(reporte?.id) { mutableStateOf(reporte?.fecha.orEmpty()) }
    var estado by rememberSaveable(reporte?.id) {
        mutableStateOf(reporte?.estado ?: EstadoReporte.PENDIENTE.name)
    }
    var validar by rememberSaveable { mutableStateOf(false) }
    var mostrarEstados by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!cargando) cancelar() },
        title = { Text(if (reporte == null) "Nuevo reporte remoto" else "Editar reporte remoto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    isError = validar && titulo.isBlank(),
                    enabled = !cargando,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    isError = validar && descripcion.isBlank(),
                    enabled = !cargando,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    isError = validar && categoria.isBlank(),
                    enabled = !cargando,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    isError = validar && ubicacion.isBlank(),
                    enabled = !cargando,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha") },
                    isError = validar && fecha.isBlank(),
                    enabled = !cargando,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Column {
                    OutlinedButton(
                        onClick = { mostrarEstados = true },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Estado: $estado") }
                    DropdownMenu(
                        expanded = mostrarEstados,
                        onDismissRequest = { mostrarEstados = false }
                    ) {
                        EstadoReporte.entries.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion.name) },
                                onClick = {
                                    estado = opcion.name
                                    mostrarEstados = false
                                }
                            )
                        }
                    }
                }
                if (validar && listOf(titulo, descripcion, categoria, ubicacion, fecha).any { it.isBlank() }) {
                    Text("Completa todos los campos.", color = MaterialTheme.colorScheme.error)
                }
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
                if (cargando) CircularProgressIndicator()
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    validar = true
                    if (listOf(titulo, descripcion, categoria, ubicacion, fecha).none { it.isBlank() }) {
                        guardar(
                            CrearReporteDto(
                                titulo = titulo.trim(),
                                descripcion = descripcion.trim(),
                                categoria = categoria.trim(),
                                ubicacion = ubicacion.trim(),
                                estado = estado,
                                fecha = fecha.trim()
                            )
                        )
                    }
                },
                enabled = !cargando
            ) { Text("GUARDAR") }
        },
        dismissButton = {
            TextButton(onClick = cancelar, enabled = !cargando) { Text("CANCELAR") }
        }
    )
}
