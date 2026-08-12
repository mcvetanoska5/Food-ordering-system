package mk.finki.orderservice.service

import mk.finki.orderservice.domain.OrderStatus
import mk.finki.orderservice.dto.CreateOrderRequest
import mk.finki.orderservice.dto.OrderResponse
import java.util.UUID

interface OrderService {
    fun createOrder(request: CreateOrderRequest): OrderResponse
    fun getOrder(id: UUID): OrderResponse
    fun getOrdersByCustomer(customerId: UUID): List<OrderResponse>
    fun updateStatus(id: UUID, newStatus: OrderStatus): OrderResponse
}
