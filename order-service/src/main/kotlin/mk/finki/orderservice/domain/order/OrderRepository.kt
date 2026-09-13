package mk.finki.orderservice.domain.order

import mk.finki.orderservice.domain.order.valueobjects.OrderId
import java.util.*

interface OrderRepository {
    fun findById(id: OrderId): Optional<Order>
    fun findAll(): List<Order>
    fun save(order: Order): Order
}
