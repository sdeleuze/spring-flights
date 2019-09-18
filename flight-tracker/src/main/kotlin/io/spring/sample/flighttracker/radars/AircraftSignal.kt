package io.spring.sample.flighttracker.radars

import java.time.Instant

data class AircraftSignal(
		val callSign: String,
		val location: LatLng,
		val bearing: Double,
		val captureTime: Instant,
		val isSignalLost: Boolean)
