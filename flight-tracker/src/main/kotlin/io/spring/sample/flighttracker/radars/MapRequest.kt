package io.spring.sample.flighttracker.radars

import io.spring.sample.flighttracker.ViewBox

data class MapRequest(
		val viewBox: ViewBox,
		val maxRadars: Int)