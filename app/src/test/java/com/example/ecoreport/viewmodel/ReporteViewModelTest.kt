package com.example.ecoreport.viewmodel

import androidx.lifecycle.ViewModelStore
import com.example.ecoreport.data.ReporteDao
import com.example.ecoreport.data.ReporteEntity
import com.example.ecoreport.model.EstadoReporte
import com.example.ecoreport.repository.ReporteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReporteViewModelTest {
    private val registros = listOf(
        reporte(3, "Basura", EstadoReporte.PENDIENTE),
        reporte(2, "Basura", EstadoReporte.RESUELTO),
        reporte(1, "Agua", EstadoReporte.EN_PROCESO)
    )
    private val dao = ReporteDaoFalso(registros)
    private val store = ViewModelStore()

    @Before
    fun configurarDispatcher() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun limpiar() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun sinFiltrosMuestraTodosLosReportes() = runTest {
        val viewModel = observarReportes()

        assertEquals(registros, viewModel.reportesFiltrados.value)
    }

    @Test
    fun permiteBuscarCadaEstado() = runTest {
        val viewModel = observarReportes()

        EstadoReporte.entries.forEach { estado ->
            viewModel.seleccionarEstado(estado)
            viewModel.buscar()
            runCurrent()

            assertEquals(
                registros.filter { it.estado == estado.name },
                viewModel.reportesFiltrados.value
            )
        }
    }

    @Test
    fun categoriaAceptaTextoParcialMayusculasYEspacios() = runTest {
        val viewModel = observarReportes()

        viewModel.cambiarCategoria("  BAS  ")
        viewModel.buscar()
        runCurrent()

        assertEquals(listOf(3, 2), viewModel.reportesFiltrados.value.map { it.id })
    }

    @Test
    fun combinaEstadoYCategoriaYSinCoincidenciasDevuelveListaVacia() = runTest {
        val viewModel = observarReportes()

        viewModel.seleccionarEstado(EstadoReporte.RESUELTO)
        viewModel.cambiarCategoria("basura")
        viewModel.buscar()
        runCurrent()
        assertEquals(listOf(2), viewModel.reportesFiltrados.value.map { it.id })

        viewModel.cambiarCategoria("agua")
        viewModel.buscar()
        runCurrent()
        assertTrue(viewModel.reportesFiltrados.value.isEmpty())
    }

    @Test
    fun editarFiltrosNoCambiaResultadosHastaPulsarBuscar() = runTest {
        val viewModel = observarReportes()

        viewModel.seleccionarEstado(EstadoReporte.PENDIENTE)
        viewModel.cambiarCategoria("basura")
        runCurrent()

        assertEquals(EstadoReporte.PENDIENTE, viewModel.filtros.value.estado)
        assertEquals("basura", viewModel.filtros.value.categoria)
        assertEquals(registros, viewModel.reportesFiltrados.value)

        viewModel.buscar()
        runCurrent()
        assertEquals(listOf(3), viewModel.reportesFiltrados.value.map { it.id })
    }

    @Test
    fun todosYCategoriaVaciaRestablecenLaLista() = runTest {
        val viewModel = observarReportes()
        viewModel.seleccionarEstado(EstadoReporte.RESUELTO)
        viewModel.cambiarCategoria("Basura")
        viewModel.buscar()
        runCurrent()

        viewModel.seleccionarEstado(null)
        viewModel.cambiarCategoria("   ")
        viewModel.buscar()
        runCurrent()

        assertEquals(registros, viewModel.reportesFiltrados.value)
    }

    @Test
    fun cambiosEnDatosActualizanResultadosSinFiltrarInicioYEstadisticas() = runTest {
        val viewModel = observarReportes()
        viewModel.seleccionarEstado(EstadoReporte.PENDIENTE)
        viewModel.buscar()
        runCurrent()

        dao.actualizar(registros.first().copy(estado = EstadoReporte.RESUELTO.name))
        runCurrent()
        assertTrue(viewModel.reportesFiltrados.value.isEmpty())
        assertEquals(3, viewModel.reportes.value.size)

        val nuevo = reporte(4, "Ruido", EstadoReporte.PENDIENTE)
        dao.insertar(nuevo)
        runCurrent()
        assertEquals(listOf(nuevo), viewModel.reportesFiltrados.value)
        assertEquals(4, viewModel.reportes.value.size)

        dao.eliminar(nuevo)
        runCurrent()
        assertTrue(viewModel.reportesFiltrados.value.isEmpty())
        assertEquals(3, viewModel.reportes.value.size)
    }

    private fun TestScope.observarReportes(): ReporteViewModel {
        val viewModel = ReporteViewModel(ReporteRepository(dao))
        store.put("reportes", viewModel)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.reportesFiltrados.collect {}
        }
        runCurrent()
        return viewModel
    }

    private fun reporte(id: Int, categoria: String, estado: EstadoReporte) = ReporteEntity(
        id = id,
        titulo = "Reporte $id",
        descripcion = "Descripción",
        categoria = categoria,
        ubicacion = "La Paz",
        estado = estado.name,
        fecha = "21/10/2026"
    )

    private class ReporteDaoFalso(reportes: List<ReporteEntity>) : ReporteDao {
        private val datos = MutableStateFlow(reportes)

        override fun obtenerTodos(): Flow<List<ReporteEntity>> = datos

        override fun obtenerPorCategoria(categoria: String?): Flow<List<ReporteEntity>> =
            datos.map { lista -> lista.filter { categoria == null || it.categoria == categoria } }

        override suspend fun insertar(reporte: ReporteEntity) {
            datos.value = listOf(reporte) + datos.value
        }

        override suspend fun actualizar(reporte: ReporteEntity) {
            datos.value = datos.value.map { if (it.id == reporte.id) reporte else it }
        }

        override suspend fun eliminar(reporte: ReporteEntity) {
            datos.value = datos.value.filterNot { it.id == reporte.id }
        }
    }
}
