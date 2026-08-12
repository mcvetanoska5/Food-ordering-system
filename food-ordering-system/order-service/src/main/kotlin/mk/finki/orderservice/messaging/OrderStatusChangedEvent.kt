package mk.finki.orderservice.messaging

import mk.finki.orderservice.domain.OrderStatus
import java.time.Instant
import java.util.UUID

/** Published to Kafka topic "order.status.changed" on every status transition. */
data class OrderStatusChangedEvent(
    val orderId: UUID?,
    val previousStatus: OrderStatus,
    val newStatus: OrderStatus,
    val occurredAt: Instant
)
