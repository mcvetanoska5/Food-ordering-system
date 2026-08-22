package mk.finki.orderservice.domain.order.events

import mk.finki.orderservice.domain.order.valueobjects.OrderId

abstract class OrderEvent(override val orderId: OrderId) : AbstractEvent(orderId)
