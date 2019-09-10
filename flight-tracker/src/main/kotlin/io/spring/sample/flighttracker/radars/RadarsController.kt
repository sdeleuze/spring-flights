package io.spring.sample.flighttracker.radars

import io.spring.sample.flighttracker.AircraftSignal
import io.spring.sample.flighttracker.AirportLocation
import io.spring.sample.flighttracker.Radar
import io.spring.sample.flighttracker.RadarService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.sendAndAwait
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import java.util.concurrent.ConcurrentLinkedQueue

@ExperimentalCoroutinesApi
@Controller
class RadarsController(private val radarService: RadarService) {

	private val logger = LoggerFactory.getLogger(RadarsController::class.java)

	private val connectedClients = ConcurrentLinkedQueue<RSocketRequester>()

	@MessageMapping("locate.radars.within")
	fun radars(request: MapRequest) =
			radarService.findRadars(request.viewBox, request.maxRadars)

	@FlowPreview
	@ExperimentalCoroutinesApi
	@MessageMapping("locate.aircrafts.for")
	fun aircraftSignal(radars: List<Radar>, requester: RSocketRequester): Flow<AircraftSignal> {
		connectedClients.offer(requester)
		return radarService.streamAircraftSignals(radars).onCompletion {
			logger.info("Server error while streaming data to the client")
			connectedClients.remove(requester)
		}.catch {
			if (it is CancellationException) {
				logger.info("Connection closed by the client")
				connectedClients.remove(requester)
			}
			else throw it
		}
	}

	@PostMapping("/location/{iata}")
	suspend fun sendClientsToLocation(@PathVariable iata: String): ResponseEntity<*> {
		val radar = radarService.findRadar(iata)
		for (client in connectedClients) {
			sendRadarLocation(client, radar)
		}
		return ResponseEntity.ok("Clients sent to $iata")
	}

	suspend fun sendRadarLocation(requester: RSocketRequester, radar: AirportLocation) {
		return requester.route("send.to.location")
				.data(radar).sendAndAwait()
	}
}
