package com.example.ecoreport.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoreport.data.ReporteEntity
import com.example.ecoreport.repository.ReporteRepository
import kotlinx.coroutines.launch

class ReporteViewModel (
    private val repository: ReporteRepository
): ViewModel() {
    val reportes = mutableListOf<ReporteEntity>()

    var filtroEstado by mutableStateOf<String?>(null)
        private set

    init {
        cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            val datos = repository.obtenerTodos()

            reportes.clear()

            reportes.addAll(datos)
        }
    }

    fun filtrarPorEstado(estado: String?) {
        filtroEstado = estado
        viewModelScope.launch {
            val datos = if (estado == null) {
                repository.obtenerTodos()
            } else {
                repository.obtenerPorEstado(estado)
            }

            reportes.clear()

            reportes.addAll(datos)
        }
    }

    fun insertar(reporte: ReporteEntity) {
        viewModelScope.launch {
            repository.insertar(reporte)
            cargarReportes()
        }
    }

    fun actualizar(reporte: ReporteEntity) {
        viewModelScope.launch {
            repository.actualizar(reporte)

            cargarReportes()
        }
    }

    fun eliminar(reporte: ReporteEntity) {
        viewModelScope.launch {
            repository.eliminar(reporte)

            cargarReportes()
        }
    }
}
