package mk.finki.orderservice.infrastructure.persistence

import mk.finki.orderservice.domain.order.Order
import mk.finki.orderservice.domain.order.OrderRepository
import mk.finki.orderservice.domain.order.valueobjects.OrderId
import org.springframework.stereotype.Component
import java.util.*

@Component
class OrderRepositoryAdapter(
    private val jpaRepository: OrderJpaRepository
) : OrderRepository {
    override fun findById(id: OrderId): Optional<Order> = jpaRepository.findById(id)
    override fun findAll(): List<Order> = jpaRepository.findAll()
    override fun save(order: Order): Order = jpaRepository.save(order)
}
