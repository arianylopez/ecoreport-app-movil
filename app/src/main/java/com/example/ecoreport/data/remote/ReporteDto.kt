package com.example.ecoreport.data.remote

data class ReporteDto(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val ubicacion: String,
    val estado: String,
    val fecha: String
)

//BASE URL = https://api.ejemplo.com/
//https://api.ejemplo.com/reportes ENDPOINT
//SELECCIONAR = BASE URL + reportes

//[ JSON
//    {
//        "id": 1,
//        "titulo": "Luminaria dañada",
//        "descripcion": "Poste sin iluminación",
//        "categoria": "LUMINARIA",
//        "ubicacion": "Av. Alemana",
//        "estado": "PENDIENTE"
//    },
//    {
//        "id": 2,
//        "titulo": "Fuga de agua",
//        "descripcion": "Pérdida de agua potable",
//        "categoria": "AGUAS",
//        "ubicacion": "Av. Busch",
//        "estado": "EN_PROCESO"
//    }
//]
