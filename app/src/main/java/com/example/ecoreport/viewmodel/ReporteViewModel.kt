package com.example.ecoreport.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoreport.data.ReporteEntity
import com.example.ecoreport.data.remote.CrearReporteDto
import com.example.ecoreport.data.remote.ReporteDto
import com.example.ecoreport.repository.ReporteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReporteViewModel (
    private val repository: ReporteRepository
): ViewModel() {

    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)

    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada
    val reportes:
    StateFlow<List<ReporteEntity>> =
        _categoriaSeleccionada
            .flatMapLatest {
                categoria ->
                repository.obtenerPorCategoria(categoria)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted
                    .WhileSubscribed(5000),
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

    fun seleccionarCategoria(categoria: String?) {
        _categoriaSeleccionada.value = categoria
    }

    //DATOS REMOTOS
    private val _reportesRemotos = MutableStateFlow<List<ReporteDto>>(emptyList())
    val reportesRemotos: StateFlow<List<ReporteDto>> = _reportesRemotos
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarReportesRemotos() {
        ejecutarOperacionRemota("No se pudieron cargar los reportes. Intenta nuevamente.") {
            _reportesRemotos.value = repository.obtenerReportesRemotos()
        }
    }

    fun crearNuevoReporteRemoto(
        titulo: String,
        descripcion: String,
        categoria: String,
        ubicacion: String,
        estado: String,
        fecha: String,
        onExito: () -> Unit = {}
    ) {
        ejecutarOperacionRemota("No se pudo crear el reporte. Intenta nuevamente.") {
            val creado = repository.crearReporte(
                CrearReporteDto(titulo, descripcion, categoria, ubicacion, estado, fecha)
            )
            _reportesRemotos.value = _reportesRemotos.value + creado
            onExito()
        }
    }

    fun actualizarReporteRemoto(
        id: Int,
        reporte: CrearReporteDto,
        onExito: () -> Unit = {}
    ) {
        ejecutarOperacionRemota("No se pudo actualizar el reporte. Intenta nuevamente.") {
            val actualizado = repository.editarReporte(id, reporte)
            _reportesRemotos.value = _reportesRemotos.value.map {
                if (it.id == id) actualizado else it
            }
            onExito()
        }
    }

    fun eliminarReporteRemoto(id: Int, onExito: () -> Unit = {}) {
        ejecutarOperacionRemota("No se pudo eliminar el reporte. Intenta nuevamente.") {
            repository.eliminarReporte(id)
            _reportesRemotos.value = _reportesRemotos.value.filterNot { it.id == id }
            onExito()
        }
    }

    fun limpiarErrorRemoto() {
        _error.value = null
    }

    private fun ejecutarOperacionRemota(mensajeError: String, operacion: suspend () -> Unit) {
        // Evita solicitudes simultáneas y mantiene la carga activa hasta terminar.
        if (_cargando.value) return
        _cargando.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                operacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = mensajeError
            } finally {
                _cargando.value = false
            }
        }
    }
}
