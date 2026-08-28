package mk.finki.orderservice.domain.order.events

import mk.finki.orderservice.domain.order.valueobjects.OrderId

abstract class OrderEvent(open val orderId: OrderId) : AbstractEvent(orderId)
