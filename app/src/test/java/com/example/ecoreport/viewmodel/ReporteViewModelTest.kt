package com.example.ecoreport.viewmodel

import com.example.ecoreport.data.ReporteDao
import com.example.ecoreport.data.ReporteEntity
import com.example.ecoreport.data.remote.CrearReporteDto
import com.example.ecoreport.data.remote.ReporteApiService
import com.example.ecoreport.data.remote.ReporteDto
import com.example.ecoreport.repository.ReporteRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReporteViewModelTest {
    private val datos = CrearReporteDto(
        "Fuga de agua", "Tubería rota", "AGUAS", "Av. Busch", "EN_PROCESO", "2026-11-04"
    )
    private val existente = ReporteDto(
        7, "Luminaria", "Poste sin luz", "LUMINARIA", "Av. Alemana", "PENDIENTE", "2026-11-03"
    )

    @Before
    fun prepararDispatcher() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun restaurarDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun crearConservaEstadoYUsaRespuestaDelServidor() = runTest {
        val api = ApiFalsa()
        val vm = crearViewModel(api)
        var exito = false

        crear(vm) { exito = true }
        assertTrue(vm.cargando.value)
        assertFalse(exito)
        advanceUntilIdle()

        assertEquals(datos, api.ultimoCuerpo)
        assertEquals(listOf(api.respuesta), vm.reportesRemotos.value)
        assertEquals("EN_PROCESO", vm.reportesRemotos.value.single().estado)
        assertEquals(listOf("POST"), api.llamadas)
        assertTrue(exito)
        assertFalse(vm.cargando.value)
        assertNull(vm.error.value)
    }

    @Test
    fun actualizarEnviaIdYCuerpoYReemplazaSoloElReporteSeleccionado() = runTest {
        val api = ApiFalsa()
        val vm = crearViewModel(api)
        vm.cargarReportesRemotos()
        advanceUntilIdle()
        var exito = false

        vm.actualizarReporteRemoto(existente.id, datos) { exito = true }
        advanceUntilIdle()

        assertEquals(existente.id, api.ultimoId)
        assertEquals(datos, api.ultimoCuerpo)
        assertEquals(listOf(api.respuesta.copy(id = existente.id), existente.copy(id = 8)), vm.reportesRemotos.value)
        assertTrue(exito)
        assertFalse(vm.cargando.value)
        assertNull(vm.error.value)
    }

    @Test
    fun eliminarQuitaSoloElReporteConfirmado() = runTest {
        val api = ApiFalsa()
        val vm = crearViewModel(api)
        vm.cargarReportesRemotos()
        advanceUntilIdle()
        var exito = false

        vm.eliminarReporteRemoto(existente.id) { exito = true }
        advanceUntilIdle()

        assertEquals(existente.id, api.ultimoId)
        assertEquals(listOf(existente.copy(id = 8)), vm.reportesRemotos.value)
        assertEquals(listOf("GET", "DELETE"), api.llamadas)
        assertTrue(exito)
        assertFalse(vm.cargando.value)
    }

    @Test
    fun errorAlCrearConservaListaYPermiteReintentar() = verificarError { vm, exito ->
        crear(vm, exito)
    }

    @Test
    fun errorAlActualizarConservaListaYPermiteReintentar() = verificarError { vm, exito ->
        vm.actualizarReporteRemoto(existente.id, datos, exito)
    }

    @Test
    fun errorAlEliminarConservaListaYPermiteReintentar() = verificarError { vm, exito ->
        vm.eliminarReporteRemoto(existente.id, exito)
    }

    @Test
    fun errorAlRecargarConservaReportesPrevios() = runTest {
        val api = ApiFalsa()
        val vm = crearViewModel(api)
        vm.cargarReportesRemotos()
        advanceUntilIdle()
        val anteriores = vm.reportesRemotos.value

        api.error = IOException("Sin conexión")
        vm.cargarReportesRemotos()
        advanceUntilIdle()

        assertEquals(anteriores, vm.reportesRemotos.value)
        assertNotNull(vm.error.value)
        assertFalse(vm.cargando.value)
        vm.limpiarErrorRemoto()
        assertNull(vm.error.value)
    }

    @Test
    fun bloqueaSolicitudesSimultaneasHastaTerminar() = runTest {
        val api = ApiFalsa()
        api.espera = CompletableDeferred()
        val vm = crearViewModel(api)

        crear(vm)
        crear(vm)
        vm.cargarReportesRemotos()
        runCurrent()

        assertTrue(vm.cargando.value)
        assertTrue(vm.reportesRemotos.value.isEmpty())
        assertEquals(listOf("POST"), api.llamadas)

        api.espera!!.complete(Unit)
        advanceUntilIdle()
        assertFalse(vm.cargando.value)
        assertEquals(1, vm.reportesRemotos.value.size)
    }

    @Test
    fun cancelacionNoSeMuestraComoErrorDeRed() = runTest {
        val api = ApiFalsa()
        api.error = CancellationException("Cancelado")
        val vm = crearViewModel(api)
        var exito = false

        crear(vm) { exito = true }
        advanceUntilIdle()

        assertFalse(exito)
        assertFalse(vm.cargando.value)
        assertNull(vm.error.value)
        assertTrue(vm.reportesRemotos.value.isEmpty())
    }

    private fun verificarError(operacion: (ReporteViewModel, () -> Unit) -> Unit) = runTest {
        val api = ApiFalsa()
        val vm = crearViewModel(api)
        vm.cargarReportesRemotos()
        advanceUntilIdle()
        val anteriores = vm.reportesRemotos.value
        var exito = false
        api.error = IOException("Sin conexión")

        operacion(vm) { exito = true }
        advanceUntilIdle()

        assertEquals(anteriores, vm.reportesRemotos.value)
        assertFalse(exito)
        assertFalse(vm.cargando.value)
        assertNotNull(vm.error.value)

        api.error = null
        operacion(vm) { exito = true }
        advanceUntilIdle()
        assertTrue(exito)
        assertFalse(vm.cargando.value)
        assertNull(vm.error.value)
    }

    private fun crear(vm: ReporteViewModel, onExito: () -> Unit = {}) {
        vm.crearNuevoReporteRemoto(
            datos.titulo, datos.descripcion, datos.categoria, datos.ubicacion,
            datos.estado, datos.fecha, onExito
        )
    }

    private fun crearViewModel(api: ReporteApiService): ReporteViewModel {
        val dao = object : ReporteDao {
            override fun obtenerTodos() = flowOf(emptyList<ReporteEntity>())
            override fun obtenerPorCategoria(categoria: String?) = obtenerTodos()
            override suspend fun insertar(reporte: ReporteEntity) = error("Operación local inesperada")
            override suspend fun actualizar(reporte: ReporteEntity) = error("Operación local inesperada")
            override suspend fun eliminar(reporte: ReporteEntity) = error("Operación local inesperada")
        }
        return ReporteViewModel(ReporteRepository(dao, api))
    }

    private inner class ApiFalsa : ReporteApiService {
        var error: Exception? = null
        var espera: CompletableDeferred<Unit>? = null
        var ultimoId: Int? = null
        var ultimoCuerpo: CrearReporteDto? = null
        val llamadas = mutableListOf<String>()
        val respuesta = ReporteDto(
            21, "Título normalizado por el servidor", datos.descripcion, datos.categoria,
            datos.ubicacion, datos.estado, datos.fecha
        )

        private suspend fun solicitar(metodo: String) {
            llamadas += metodo
            espera?.await()
            error?.let { throw it }
        }

        override suspend fun obtenerReportes(): List<ReporteDto> {
            solicitar("GET")
            return listOf(existente, existente.copy(id = 8))
        }

        override suspend fun crearReporte(reporte: CrearReporteDto): ReporteDto {
            ultimoCuerpo = reporte
            solicitar("POST")
            return respuesta
        }

        override suspend fun actualizarReporte(id: Int, reporte: CrearReporteDto): ReporteDto {
            ultimoId = id
            ultimoCuerpo = reporte
            solicitar("PUT")
            return respuesta.copy(id = id)
        }

        override suspend fun eliminarReporte(id: Int) {
            ultimoId = id
            solicitar("DELETE")
        }
    }
}
