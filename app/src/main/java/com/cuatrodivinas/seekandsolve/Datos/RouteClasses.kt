package com.cuatrodivinas.seekandsolve.Datos

data class RouteResponse (
    val routes: List<Route>
)

data class Route(
    val geometry: String
)