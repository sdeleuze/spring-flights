package io.spring.sample.flighttracker

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono

import org.springframework.http.MediaType
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.connectTcpAndAwait
import org.springframework.messaging.rsocket.retrieveAndAwait
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.stereotype.Component

@ExperimentalCoroutinesApi
@Component
class RadarService(builder: RSocketRequester.Builder) {

	private val requester: RSocketRequester = runBlocking {
		builder
				.dataMimeType(MediaType.APPLICATION_CBOR)
				.connectTcpAndAwait("localhost", 9898)
	}

	suspend fun findRadar(iata: String) = requester
			.route(String.format("find.radar.%s", iata))
			.retrieveAndAwait<AirportLocation>()


	fun findRadars(box: ViewBox, maxCount: Int) = requester
			.route("locate.radars.within")
			.data(box)
			.retrieveFlow<AirportLocation>()
			.take(maxCount)

	@FlowPreview
	fun streamAircraftSignals(radars: List<Radar>)  = radars
			.asFlow().map { listenAircraftSignals(it) }.flattenMerge()

	private fun listenAircraftSignals(radar: Radar) = requester
			.route(String.format("listen.radar.%s", radar.iata))
			.retrieveFlow<AircraftSignal>()
}
