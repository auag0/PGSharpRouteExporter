package io.github.auag0.pgsharprouteexporter.model

data class Route(
    val name: String,
    val points: List<LatLng>
)