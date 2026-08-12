package mk.finki.orderservice.repository

import mk.finki.orderservice.domain.Order
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByCustomerIdOrderByCreatedAtDesc(customerId: UUID): List<Order>
    fun findByRestaurantIdOrderByCreatedAtDesc(restaurantId: UUID): List<Order>
}
