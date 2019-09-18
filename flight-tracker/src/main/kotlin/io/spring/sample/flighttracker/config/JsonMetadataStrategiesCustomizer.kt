package io.spring.sample.flighttracker.config

import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer
import org.springframework.core.ParameterizedTypeReference
import org.springframework.messaging.rsocket.MetadataExtractorRegistry
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.stereotype.Component
import org.springframework.util.MimeType

/**
 * [RSocketStrategiesCustomizer] that adds a custom extractor to the
 * [org.springframework.messaging.rsocket.MetadataExtractorRegistry].
 * We're using here an alternate metadata format that uses a JSON object
 * to hold multiple metadata entries with a custom JSON media type.
 *
 * This configuration won't be necessary when the rsocket-js
 * library will support the RSocket composite metadata extension.
 */
@Component
class JsonMetadataStrategiesCustomizer : RSocketStrategiesCustomizer {

	private val metadataMimeType: MimeType = MimeType.valueOf("application/vnd.spring.rsocket.metadata+json")
	private val metadataType: ParameterizedTypeReference<Map<String, String>> = object : ParameterizedTypeReference<Map<String, String>>() {}

	override fun customize(strategies: RSocketStrategies.Builder) {
		strategies.metadataExtractors { registry: MetadataExtractorRegistry -> registry.metadataToExtract(metadataMimeType, metadataType) { `in`: Map<String, String>, map: MutableMap<String, Any> -> map.putAll(`in`) } }
	}
}