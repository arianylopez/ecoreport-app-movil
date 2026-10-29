package com.example.ecoreport.ui_comp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ecoreport.data.remote.ReporteDto

@Composable
fun PantallaReportesRemotos(
    reportes: List<ReporteDto>,
    cargando: Boolean,
    error: String?,
    consultar: () -> Unit,
    volver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Reportes remotos",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = consultar,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (error == null) "ACTUALIZAR" else "REINTENTAR")
        }

        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Consultando reportes…")
        }

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        if (!cargando && error == null && reportes.isEmpty()) {
            Text("La API no tiene reportes registrados.")
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(reportes, key = { it.id }) { reporte ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = reporte.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(reporte.descripcion)
                        Text("Categoría: ${reporte.categoria}")
                        Text("Ubicación: ${reporte.ubicacion}")
                        Text("Estado: ${reporte.estado}")
                        Text("Fecha: ${reporte.fecha}")
                    }
                }
            }
        }

        OutlinedButton(
            onClick = volver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("VOLVER")
        }
    }
}
