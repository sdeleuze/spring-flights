package io.spring.sample.flighttracker.radars

data class MapRequest(
		val viewBox: ViewBox,
		val maxRadars: Int)