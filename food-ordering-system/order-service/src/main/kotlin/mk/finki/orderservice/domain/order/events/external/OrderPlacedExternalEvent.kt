package mk.finki.orderservice.domain.order.events.external

import java.util.UUID

data class OrderPlacedExternalEvent(
    val orderId: UUID,
    val restaurantId: UUID,
    val items: List<ExternalOrderItem>
)

data class ExternalOrderItem(
    val menuItemId: UUID,
    val quantity: Int
)
