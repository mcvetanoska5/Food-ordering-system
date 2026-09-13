package mk.finki.orderservice.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import mk.finki.orderservice.domain.order.events.AbstractEvent
import mk.finki.orderservice.infrastructure.messaging.EventMessagingService
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
class EventMessagingEventHandler(
    private val eventMessagingService: EventMessagingService
) {
    private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @EventHandler
    fun on(event: AbstractEvent) {
        val externalEvent = event.toExternalEvent() ?: return
        val eventJson = objectMapper.writeValueAsString(externalEvent)
        eventMessagingService.send(
            topic = event.eventTopic(),
            key = event.identifier.value.toString(),
            payload = eventJson
        )
    }
}
