package mk.finki.restaurantservice.domain.restaurant.events

import java.time.Instant
import java.util.UUID

abstract class AbstractEvent(
    val aggregateId: Any,
    val occurredAt: Instant = Instant.now(),
    val eventId: UUID = UUID.randomUUID()
) {
    fun eventType(): String = this::class.simpleName ?: "UnknownEvent"
    
    fun eventTopic(): String = eventType()
        .replace(Regex("([a-z])([A-Z])"), "$1.$2")
        .lowercase()

    open fun toExternalEvent(): Any? = null
}
