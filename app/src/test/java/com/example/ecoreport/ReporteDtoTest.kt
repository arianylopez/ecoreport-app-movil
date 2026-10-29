package com.example.ecoreport

import com.example.ecoreport.data.remote.ReporteDto
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReporteDtoTest {
    @Test
    fun interpretaLaListaDeMockApiConIdentificadoresDeTexto() {
        val respuesta = """
            [
              {
                "id": "1",
                "titulo": "Fuga de agua",
                "descripcion": "Tubería rota con pérdida de agua en la calle",
                "categoria": "AGUAS",
                "ubicacion": "Av. Busch, La Paz",
                "estado": "PENDIENTE",
                "fecha": "29/10/2026"
              }
            ]
        """.trimIndent()

        val reportes = Gson().fromJson(respuesta, Array<ReporteDto>::class.java)

        assertEquals(1, reportes.size)
        val reporte = reportes.single()
        assertEquals("1", reporte.id)
        assertEquals("Fuga de agua", reporte.titulo)
        assertEquals("Tubería rota con pérdida de agua en la calle", reporte.descripcion)
        assertEquals("AGUAS", reporte.categoria)
        assertEquals("Av. Busch, La Paz", reporte.ubicacion)
        assertEquals("PENDIENTE", reporte.estado)
        assertEquals("29/10/2026", reporte.fecha)
    }

    @Test
    fun interpretaUnaApiSinReportesComoListaVacia() {
        val reportes = Gson().fromJson("[]", Array<ReporteDto>::class.java)

        assertTrue(reportes.isEmpty())
    }
}
