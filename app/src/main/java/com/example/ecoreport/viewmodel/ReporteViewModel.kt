package com.example.ecoreport.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoreport.data.ReporteEntity
import com.example.ecoreport.model.EstadoReporte
import com.example.ecoreport.model.FiltrosReporte
import com.example.ecoreport.repository.ReporteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReporteViewModel (
    private val repository: ReporteRepository
): ViewModel() {

    private val _filtros = MutableStateFlow(FiltrosReporte())
    val filtros: StateFlow<FiltrosReporte> = _filtros.asStateFlow()

    // Los campos se editan sin cambiar los resultados hasta pulsar Buscar.
    private val _filtrosAplicados = MutableStateFlow(FiltrosReporte())

    val reportes: StateFlow<List<ReporteEntity>> =
        repository.obtenerTodos()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted
                    .WhileSubscribed(5000),
                initialValue = emptyList()
            )
    val reportesFiltrados: StateFlow<List<ReporteEntity>> =
        combine(reportes, _filtrosAplicados) { lista, filtros ->
            lista.filter { reporte ->
                (filtros.estado == null || reporte.estado == filtros.estado.name) &&
                    reporte.categoria.contains(filtros.categoria, ignoreCase = true)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertar(reporte: ReporteEntity) {
        viewModelScope.launch {
            repository.insertar(reporte)
        }
    }

    fun eliminar(reporte: ReporteEntity) {
        viewModelScope.launch {
            repository.eliminar(reporte)
        }
    }

    fun actualizar(reporte: ReporteEntity) {
        viewModelScope.launch {
            repository.actualizar(reporte)
        }
    }

    fun seleccionarEstado(estado: EstadoReporte?) {
        _filtros.update { it.copy(estado = estado) }
    }

    fun cambiarCategoria(categoria: String) {
        _filtros.update { it.copy(categoria = categoria) }
    }

    fun buscar() {
        _filtrosAplicados.value = _filtros.value.copy(
            categoria = _filtros.value.categoria.trim()
        )
    }
}
