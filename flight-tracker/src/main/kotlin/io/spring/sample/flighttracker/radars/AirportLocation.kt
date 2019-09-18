package io.spring.sample.flighttracker.radars

data class AirportLocation(
		val iata: String,
		val location: LatLng,
		val name: String? = null)
