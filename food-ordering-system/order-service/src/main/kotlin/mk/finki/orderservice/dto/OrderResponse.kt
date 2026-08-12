package mk.finki.orderservice.dto

import mk.finki.orderservice.domain.Order
import mk.finki.orderservice.domain.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID?,
    val customerId: UUID,
    val restaurantId: UUID,
    val items: List<OrderItemView>,
    val status: OrderStatus,
    val totalPrice: BigDecimal,
    val deliveryAddress: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?
) {

    data class OrderItemView(
        val menuItemId: UUID,
        val itemName: String,
        val unitPrice: BigDecimal,
        val quantity: Int
    )

    companion object {
        fun from(order: Order): OrderResponse =
            OrderResponse(
                id = order.id,
                customerId = order.customerId,
                restaurantId = order.restaurantId,
                items = order.items.map {
                    OrderItemView(it.menuItemId, it.itemName, it.unitPrice, it.quantity)
                },
                status = order.status,
                totalPrice = order.totalPrice,
                deliveryAddress = order.deliveryAddress,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
    }
}
