package mk.finki.orderservice.domain.order.events

import mk.finki.orderservice.domain.order.events.external.ExternalOrderItem
import mk.finki.orderservice.domain.order.events.external.OrderPlacedExternalEvent
import mk.finki.orderservice.domain.order.valueobjects.CustomerId
import mk.finki.orderservice.domain.order.valueobjects.OrderId
import mk.finki.orderservice.domain.order.valueobjects.RestaurantId
import java.time.Instant
import java.util.UUID

data class OrderPlacedEvent(
    override val orderId: OrderId,
    val customerId: CustomerId,
    val restaurantId: RestaurantId,
    val items: List<OrderItemSnapshot>,
    val placedAt: Instant = Instant.now()
) : OrderEvent(orderId) {
    override fun toExternalEvent(): OrderPlacedExternalEvent =
        OrderPlacedExternalEvent(
            orderId = orderId.value,
            restaurantId = restaurantId.value,
            items = items.map { ExternalOrderItem(menuItemId = it.menuItemId, quantity = it.quantity) }
        )
}

data class OrderItemSnapshot(
    val menuItemId: UUID,
    val quantity: Int
)
