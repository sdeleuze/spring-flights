package io.spring.sample.flighttracker

data class AirportLocation(
		val iata: String,
		val location: LatLng,
		val name: String? = null)
