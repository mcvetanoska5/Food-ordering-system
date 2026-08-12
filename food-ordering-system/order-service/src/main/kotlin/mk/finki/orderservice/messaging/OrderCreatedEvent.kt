package mk.finki.orderservice.messaging

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Published to Kafka topic "order.created" after a successful order.
 * Consumed asynchronously by e.g. Notification Service / Payment Service —
 * decoupled from the Order Service, which doesn't need to know who's listening.
 */
data class OrderCreatedEvent(
    val orderId: UUID?,
    val customerId: UUID,
    val restaurantId: UUID,
    val totalPrice: BigDecimal,
    val occurredAt: Instant
)
