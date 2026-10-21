package com.example.ecoreport.ui_comp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ecoreport.data.ReporteEntity
import com.example.ecoreport.model.EstadoReporte
import com.example.ecoreport.model.FiltrosReporte

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReportes(
    reportes: List<ReporteEntity>,
    hayReportes: Boolean,
    filtros: FiltrosReporte,
    seleccionarEstado: (EstadoReporte?) -> Unit,
    cambiarCategoria: (String) -> Unit,
    buscar: () -> Unit,
    seleccionarReporte: (ReporteEntity) -> Unit,
    volver: () -> Unit
) {
    var estadoExpandido by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val aplicarBusqueda = {
        focusManager.clearFocus()
        buscar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Reportes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            ExposedDropdownMenuBox(
                expanded = estadoExpandido,
                onExpandedChange = { estadoExpandido = it },
                modifier = Modifier.weight(1.2f)
            ) {
                OutlinedButton(
                    onClick = {},
                    shape = MaterialTheme.shapes.extraSmall,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estado", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = filtros.estado?.etiqueta ?: "Todos",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpandido)
                }

                ExposedDropdownMenu(
                    expanded = estadoExpandido,
                    onDismissRequest = { estadoExpandido = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos") },
                        onClick = {
                            seleccionarEstado(null)
                            estadoExpandido = false
                        }
                    )
                    EstadoReporte.entries.forEach { estado ->
                        DropdownMenuItem(
                            text = { Text(estado.etiqueta) },
                            onClick = {
                                seleccionarEstado(estado)
                                estadoExpandido = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = filtros.categoria,
                onValueChange = cambiarCategoria,
                label = { Text("Categoría", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { aplicarBusqueda() }),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = aplicarBusqueda,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Buscar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reportes.isEmpty()) {

            Text(
                text = if (hayReportes) {
                    "No se encontraron reportes con los filtros seleccionados."
                } else {
                    "No existen reportes registrados."
                }
            )

            Spacer(modifier = Modifier.weight(1f))

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(
                    items = reportes,
                    key = { reporte ->
                        reporte.id
                    }
                ) { reporte ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                seleccionarReporte(reporte)
                            },

                        shape = RoundedCornerShape(16.dp),

                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {

                            Text(
                                text = reporte.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Categoría: ${reporte.categoria}"
                            )

                            Text(
                                text = "Ubicación: ${reporte.ubicacion}"
                            )

                            Text(
                                text = "Estado: ${reporte.estado}"
                            )

                            Text(
                                text = "Fecha: ${reporte.fecha}"
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Presiona para ver el detalle",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(
            onClick = volver,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "VOLVER"
            )
        }
    }
}
